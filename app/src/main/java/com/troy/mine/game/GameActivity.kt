/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.troy.mine.game

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.troy.mine.R
import kotlinx.android.synthetic.main.activity_zoom.*

class GameActivity : Activity() {
//    private lateinit var mGraphView: InteractiveLineGraphView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zoom)
//        mGraphView = chart
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_zoom, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_zoom_in -> {
                chart.zoomIn()
                return true
            }
            R.id.action_zoom_out -> {
                chart.zoomOut()
                return true
            }
            R.id.action_pan_left -> {
                chart.panLeft()
                return true
            }
            R.id.action_pan_right -> {
                chart.panRight()
                return true
            }
            R.id.action_pan_up -> {
                chart.panUp()
                return true
            }
            R.id.action_pan_down -> {
                chart.panDown()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, GameActivity::class.java)
            context.startActivity(intent)
        }
    }
}
