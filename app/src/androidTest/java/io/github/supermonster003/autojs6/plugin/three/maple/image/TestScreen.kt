package io.github.supermonster003.autojs6.plugin.three.maple.image

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import java.util.regex.Pattern

/**
 * The screen for tests that need it visible: a system confirmation only shows on a lit, unlocked screen, and Glide
 * pauses its requests while the activity is stopped. A CI emulator may turn its screen off part way through the run.
 */
internal object TestScreen {

    private val device get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    fun awake() {
        device.wakeUp()
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("wm dismiss-keyguard").close()
    }

    /** What the screen shows, for the message of a failed wait: the foreground package and some visible texts. */
    fun summary(): String = runCatching {
        val texts = device.findObjects(By.text(Pattern.compile(".+"))).take(MAX_TEXTS).joinToString(" | ") { "${it.applicationPackage}: ${it.text}" }
        "screen on ${device.isScreenOn}, foreground ${device.currentPackageName}, texts [$texts]"
    }.getOrElse { "no screen summary (${it.javaClass.simpleName})" }

    private const val MAX_TEXTS = 12
}
