package com.troy.mine.game

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.troy.mine.R
import kotlinx.android.synthetic.main.activity_startup.*
import org.koin.android.ext.android.inject
import kotlin.math.roundToInt

class StartupActivity : AppCompatActivity() {

    private val gameEngine: GameEngine by inject()
    private val hideHandler = Handler()
    private val hidePart2Runnable = Runnable {
        contentLayout.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
//        fullscreen_content_controls.visibility = View.VISIBLE
    }
    private var isSystemVisible: Boolean = false
    private val hideRunnable = Runnable { hide() }
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val hideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_startup)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isSystemVisible = true
        difficultySpinner.adapter = ArrayAdapter.createFromResource(this, R.array.difficulty, android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        sizeSpinner.adapter = ArrayAdapter.createFromResource(this, R.array.fieldSize, android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        resumeButton.setOnClickListener { resumeGame() }
        startButton.setOnClickListener { startGame() }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun resumeGame() {
        GameActivity.startActivity(this)
    }

    private fun startGame() {
        val difficulty = Difficulty.values()[difficultySpinner.selectedItemPosition]
        val fieldSSize = FieldSize.values()[sizeSpinner.selectedItemPosition]
        val mines = (Math.sqrt(fieldSSize.columns.toDouble() * fieldSSize.rows) * difficulty.scale).roundToInt()
        gameEngine.reset(fieldSSize.columns, fieldSSize.rows, mines)
        GameActivity.startActivity(this)
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
//        fullscreen_content_controls.visibility = View.GONE
        isSystemVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        contentLayout.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        isSystemVisible = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }

    enum class Difficulty(val scale: Float) {
        EASY(2f), MEDIUM(3f), HARD(4f), CRAZY(5f)
    }

    enum class FieldSize(val columns: Int, val rows: Int) {
        SMALL(6, 15), MEDIUM(12, 30), LARGE(18, 45), HUGE(24, 60)
    }
}
