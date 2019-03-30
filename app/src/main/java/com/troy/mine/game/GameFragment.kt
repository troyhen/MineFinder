package com.troy.mine.game

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.troy.mine.R
import kotlinx.android.synthetic.main.fragment_game.*
import org.koin.android.ext.android.inject


class GameFragment : Fragment() {

    private val gameEngine: GameEngine by inject()
    private var windowX: Float = 0f
    private var windowY: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            gameEngine.load()
            gameView.currentViewport = gameEngine.viewport
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(R.layout.fragment_game, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupObservers()
    }

    override fun onStop() {
        super.onStop()
        gameEngine.viewport = gameView.currentViewport
        gameEngine.save()
    }

    private fun setupListeners() {
        var xOffset = 0f
        var yOffset = 0f
        var xRange = 0f..0f
        var yRange = 0f..0f

        dragHandle.setOnTouchListener { _, event ->
            return@setOnTouchListener when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    xOffset = infoPanel.translationX - event.rawX
                    yOffset = infoPanel.translationY - event.rawY
                    xRange = -infoPanel.left.toFloat()..(view!!.width - infoPanel.width - infoPanel.left).toFloat()
                    yRange = -infoPanel.top.toFloat()..(view!!.height - infoPanel.height - infoPanel.top).toFloat()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    infoPanel.translationX = (event.rawX + xOffset).coerceIn(xRange)
                    infoPanel.translationY = (event.rawY + yOffset).coerceIn(yRange)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    windowX = infoPanel.translationX
                    windowY = infoPanel.translationY
                    true
                }
                else -> false
            }
        }
        modeIndicator.setOnClickListener { gameEngine.toggleMode() }
        closeButton.setOnClickListener { activity?.onBackPressed() }
    }

    private fun setupObservers() = gameEngine.apply {
        modeLive.observe(this@GameFragment, Observer {
            val background = when (it) {
                ClickMode.MARK -> Color.YELLOW
                else -> Color.WHITE
            }
            modeIndicator.setBackgroundColor(background)
        })
        updateEvent.observe(this@GameFragment, Observer { gameView.invalidate() })
    }
}
