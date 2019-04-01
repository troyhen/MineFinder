package com.troy.mine.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.troy.mine.R
import kotlinx.android.synthetic.main.fragment_setup.*
import org.koin.android.ext.android.inject

class SetupFragment : Fragment() {

    private val gameEngine: GameEngine by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(R.layout.fragment_setup, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSpinners()
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        resumeButton.isVisible = gameEngine.state == GameState.PLAY
    }

    private fun setupListeners() {
        exitButton.setOnClickListener { activity?.finish() }
        resumeButton.setOnClickListener { showGame() }
        startButton.setOnClickListener { startGame() }
    }

    private fun setupSpinners() {
        difficultySpinner.adapter = ArrayAdapter.createFromResource(requireContext(), R.array.difficulty, R.layout.item_selected).also { adapter ->
            adapter.setDropDownViewResource(R.layout.item_spinner)
        }
        difficultySpinner.setSelection(gameEngine.difficulty.ordinal)
        sizeSpinner.adapter = ArrayAdapter.createFromResource(requireContext(), R.array.fieldSize, R.layout.item_selected).also { adapter ->
            adapter.setDropDownViewResource(R.layout.item_spinner)
        }
        sizeSpinner.setSelection(gameEngine.fieldSize.ordinal)
    }

    private fun showGame() {
        findNavController().navigate(R.id.action_startupFragment_to_gameFragment)
    }

    private fun startGame() {
        val difficulty = Difficulty.values()[difficultySpinner.selectedItemPosition]
        val fieldSize = FieldSize.values()[sizeSpinner.selectedItemPosition]
        gameEngine.startGame(difficulty, fieldSize)
        showGame()
    }
}
