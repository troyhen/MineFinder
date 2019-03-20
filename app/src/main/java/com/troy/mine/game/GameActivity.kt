package com.troy.mine.game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.troy.mine.R
import kotlinx.android.synthetic.main.activity_game.*
import org.koin.android.ext.android.inject

class GameActivity : AppCompatActivity() {

    private val gameEngine: GameEngine by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        if (savedInstanceState != null) {
            gameEngine.load()
            gameView.currentViewport = gameEngine.viewport
        }
        setupObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_zoom, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_zoom_in -> {
                gameView.zoomIn()
                return true
            }
            R.id.action_zoom_out -> {
                gameView.zoomOut()
                return true
            }
            R.id.action_pan_left -> {
                gameView.panLeft()
                return true
            }
            R.id.action_pan_right -> {
                gameView.panRight()
                return true
            }
            R.id.action_pan_up -> {
                gameView.panUp()
                return true
            }
            R.id.action_pan_down -> {
                gameView.panDown()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        gameEngine.viewport = gameView.currentViewport
        gameEngine.save()
    }

    private fun setupObservers() {
        gameEngine.updateEvent.observe(this, Observer { gameView.invalidate() })
    }

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, GameActivity::class.java)
            context.startActivity(intent)
        }
    }
}
