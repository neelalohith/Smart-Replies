// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
}


buildscript {

    repositories {
        google()
        mavenCentral()
    }
}

extra.apply {
    set("compose_version", "1.5.1") // Make sure this matches your kotlinCompilerExtensionVersion
    set("activity_compose_version", "1.7.2") // Update to latest stable version
    set("viewmodel_compose_version", "2.6.1") // Update to latest stable version
}