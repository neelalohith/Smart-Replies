package com.example.smart_replies

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.nl.smartreply.SmartReply
import com.google.mlkit.nl.smartreply.SmartReplySuggestion
import com.google.mlkit.nl.smartreply.TextMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _messageList = MutableLiveData<List<ConversationMsg>>(emptyList())
    val messageList: LiveData<List<ConversationMsg>> = _messageList

    private val _text = MutableLiveData(TextFieldValue())
    val text: LiveData<TextFieldValue> = _text

    private val _suggestions = MutableLiveData<List<SmartReplySuggestion>>(emptyList())
    val suggestions: LiveData<List<SmartReplySuggestion>> = _suggestions

    private val smartReplyGenerator = SmartReply.getClient()
    private var typingJob: Job? = null

    fun updateText(newText: TextFieldValue) {
        _text.value = newText
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            delay(300) // Debounce typing
            generateTypingSuggestions(newText.text)
        }
    }

    private fun generateTypingSuggestions(typedText: String) {
        viewModelScope.launch {
            val recentMessages = _messageList.value.orEmpty().takeLast(5).map { message ->
                if (message.who == "me") {
                    TextMessage.createForLocalUser(message.msg, System.currentTimeMillis())
                } else {
                    TextMessage.createForRemoteUser(message.msg, System.currentTimeMillis(), message.who)
                }
            }

            // Add the currently typed text as a "local" message
            val currentTyping = TextMessage.createForLocalUser(typedText, System.currentTimeMillis())
            val conversationWithTyping = recentMessages + currentTyping

            smartReplyGenerator.suggestReplies(conversationWithTyping)
                .addOnSuccessListener { result ->
                    _suggestions.value = result.suggestions
                }
                .addOnFailureListener { it.printStackTrace() }
        }
    }

    fun sendMessage() {
        val messageText = _text.value?.text ?: return
        if (messageText.isNotEmpty()) {
            addMessage(ConversationMsg(messageText, "me"))
            updateText(TextFieldValue("")) // Clear the input field after sending
        }
    }

    private fun addMessage(message: ConversationMsg) {
        val currentList = _messageList.value.orEmpty().toMutableList()
        currentList.add(message)
        _messageList.value = currentList
        generateReplySuggestions()
    }

    private fun generateReplySuggestions() {
        viewModelScope.launch {
            val conversation = _messageList.value.orEmpty().takeLast(10).map { message ->
                if (message.who == "me") {
                    TextMessage.createForLocalUser(message.msg, System.currentTimeMillis())
                } else {
                    TextMessage.createForRemoteUser(message.msg, System.currentTimeMillis(), message.who)
                }
            }

            smartReplyGenerator.suggestReplies(conversation)
                .addOnSuccessListener { result ->
                    _suggestions.value = result.suggestions
                }
                .addOnFailureListener { it.printStackTrace() }
        }
    }

    data class ConversationMsg(val msg: String, val who: String)
}