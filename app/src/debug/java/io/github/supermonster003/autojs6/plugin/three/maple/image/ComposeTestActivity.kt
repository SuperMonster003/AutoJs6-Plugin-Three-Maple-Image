package io.github.supermonster003.autojs6.plugin.three.maple.image

import androidx.activity.ComponentActivity

/**
 * The empty host of the Compose tests that set their own content (`createAndroidComposeRule<ComposeTestActivity>()`, or
 * `ActivityScenario.launch(ComposeTestActivity::class.java)` for the tests that only need an activity). It stands in for
 * the activity of `androidx.compose.ui:ui-test-manifest`, which is exported and would add an exported
 * `androidx.activity.ComponentActivity` to the debug APK; this one is not exported and keeps the same theme. Debug
 * builds only.
 */
class ComposeTestActivity : ComponentActivity()
