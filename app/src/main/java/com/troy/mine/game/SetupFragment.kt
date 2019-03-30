package com.troy.mine.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.troy.mine.R
import kotlinx.android.synthetic.main.fragment_setup.*
import org.koin.android.ext.android.inject
import kotlin.math.roundToInt

class SetupFragment : Fragment() {

    private val gameEngine: GameEngine by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(R.layout.fragment_setup, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        difficultySpinner.adapter = ArrayAdapter.createFromResource(requireContext(), R.array.difficulty, R.layout.item_selected).also { adapter ->
            adapter.setDropDownViewResource(R.layout.item_spinner)
        }
        sizeSpinner.adapter = ArrayAdapter.createFromResource(requireContext(), R.array.fieldSize, R.layout.item_selected).also { adapter ->
            adapter.setDropDownViewResource(R.layout.item_spinner)
        }
        resumeButton.setOnClickListener { resumeGame() }
        startButton.setOnClickListener { startGame() }
    }

    private fun resumeGame() {
        findNavController().navigate(R.id.action_startupFragment_to_gameFragment)
    }

    private fun startGame() {
        val difficulty = Difficulty.values()[difficultySpinner.selectedItemPosition]
        val fieldSSize = FieldSize.values()[sizeSpinner.selectedItemPosition]
        val mines = (Math.sqrt(fieldSSize.columns.toDouble() * fieldSSize.rows) * difficulty.scale).roundToInt()
        gameEngine.reset(fieldSSize.columns, fieldSSize.rows, mines)
        resumeGame()
    }

    enum class Difficulty(val scale: Float) {
        EASY(2f), MEDIUM(3f), HARD(4f), CRAZY(5f)
    }

    enum class FieldSize(val columns: Int, val rows: Int) {
        SMALL(6, 15), MEDIUM(12, 30), LARGE(18, 45), HUGE(24, 60)
    }
}
