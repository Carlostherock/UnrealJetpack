// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // kotlin.compose plugin is bundled with kotlin.android for Kotlin 1.9.x
    // alias(libs.plugins.kotlin.compose) apply false
}
