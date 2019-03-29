package com.troy.mine.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.troy.mine.R
import kotlinx.android.synthetic.main.fragment_game.*
import org.koin.android.ext.android.inject
import kotlin.math.roundToInt


class GameFragment : Fragment() {

    private val gameEngine: GameEngine by inject()
    private var windowX: Int = 0
    private var windowY: Int = 0

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
        dragHandle.setOnTouchListener { v, event ->
            val x = event.rawX.roundToInt()
            val y = event.rawY.roundToInt()
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    val layoutParams = infoPanel.layoutParams as FrameLayout.LayoutParams
                    windowX = x - layoutParams.leftMargin
                    windowY = y - layoutParams.topMargin
                }
                MotionEvent.ACTION_MOVE -> {
                    val layoutParams = infoPanel.layoutParams as FrameLayout.LayoutParams
                    layoutParams.leftMargin = x - windowX
                    layoutParams.topMargin = y - windowY
                    infoPanel.layoutParams = layoutParams
                }
                else -> {
                }
            }
            return@setOnTouchListener true
        }
    }

    private fun setupObservers() {
        gameEngine.updateEvent.observe(this, Observer { gameView.invalidate() })
    }
}
