package com.example.smart_replies

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.smart_replies.ui.theme.SmartRepliesTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartRepliesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun ChatScreen(viewModel: MainViewModel) {
    val messages by viewModel.messageList.observeAsState(initial = emptyList())
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            reverseLayout = true
        ) {
            items(messages.asReversed()) { message ->
                ChatMessage(message)
            }
        }

        SuggestionList(viewModel)
        InputField(viewModel)
    }

    LaunchedEffect(messages.size) {
        listState.animateScrollToItem(0)
    }
}

@Composable
fun ChatMessage(message: MainViewModel.ConversationMsg) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = if (message.who == "me") Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 340.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.who == "me")
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(
                text = message.msg,
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun SuggestionList(viewModel: MainViewModel) {
    val suggestions by viewModel.suggestions.observeAsState(initial = emptyList())

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        items(suggestions) { suggestion ->
            Button(
                onClick = { viewModel.updateText(TextFieldValue(suggestion.text)) },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(suggestion.text)
            }
        }
    }
}

@Composable
fun InputField(viewModel: MainViewModel) {
    val text by viewModel.text.observeAsState(TextFieldValue())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = { viewModel.updateText(it) },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message") }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = { viewModel.sendMessage() }) {
            Text("Send")
        }
    }
}