package com.troy.mine.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.troy.mine.R
import kotlinx.android.synthetic.main.fragment_game.*
import org.koin.android.ext.android.inject

class GameFragment : Fragment() {

    private val gameEngine: GameEngine by inject()

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
        setupObservers()
    }

    override fun onStop() {
        super.onStop()
        gameEngine.viewport = gameView.currentViewport
        gameEngine.save()
    }

    private fun setupObservers() {
        gameEngine.updateEvent.observe(this, Observer { gameView.invalidate() })
    }
}
