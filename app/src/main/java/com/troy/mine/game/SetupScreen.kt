package com.troy.mine.game

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.troy.mine.R

@Composable
fun SetupScreen(
    gameEngine: GameEngine,
    onResume: () -> Unit,
    onStart: () -> Unit,
    onExit: () -> Unit,
) {
    Surface(color = MaterialTheme.colors.background) {
        SetupContents(
            gameState = gameEngine.state,
            fieldSize = gameEngine.fieldSize,
            difficulty = gameEngine.difficulty,
            onSizeChange = { gameEngine.fieldSize = it },
            onDifficultyChange = { gameEngine.difficulty = it },
            onResume = onResume,
            onStart = onStart,
            onExit = onExit,
        )
    }
}

@Composable
private fun SetupContents(
    gameState: GameState,
    fieldSize: FieldSize,
    difficulty: Difficulty,
    onSizeChange: (FieldSize) -> Unit,
    onDifficultyChange: (Difficulty) -> Unit,
    onResume: () -> Unit,
    onStart: () -> Unit,
    onExit: () -> Unit,
) {
    Box {
        Image(painterResource(R.drawable.pattern1), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Column(
            Modifier.fillMaxSize(),
            Arrangement.Center,
            Alignment.CenterHorizontally,
        ) {
            val padding = Modifier.padding(16.dp)
            Text(stringResource(R.string.app_name), padding, textAlign = TextAlign.Center, style = MaterialTheme.typography.h2)
            if (gameState == GameState.PLAY) ResumeButton(padding, onResume)
            SizeField(fieldSize, padding, onSizeChange)
            DifficultyField(difficulty, padding, onDifficultyChange)
            StartButton(padding, onStart)
            ExitButton(padding, onExit)
        }
    }
}

@Composable
private fun SetupButton(
    @StringRes textId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(onClick = onClick, modifier) {
        Text(stringResource(textId), style = MaterialTheme.typography.h6)
    }
}

@Composable
private fun SizeField(
    size: FieldSize,
    modifier: Modifier = Modifier,
    onChange: (FieldSize) -> Unit
) {
    val resources = LocalContext.current.resources
    val choices = resources.getTextArray(R.array.fieldSize).map { it.toString() }
    DropDownField(choices[size.ordinal], choices, modifier) { onChange(FieldSize.values()[it]) }
}

@Composable
private fun DifficultyField(
    difficulty: Difficulty,
    modifier: Modifier = Modifier,
    onChange: (Difficulty) -> Unit
) {
    val resources = LocalContext.current.resources
    val choices = resources.getTextArray(R.array.difficulty).map { it.toString() }
    DropDownField(
        choices[difficulty.ordinal],
        choices,
        modifier
    ) { onChange(Difficulty.values()[it]) }
}

@Composable
private fun DropDownField(
    current: String,
    choices: List<String>,
    modifier: Modifier = Modifier,
    onChange: (Int) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    Box(modifier.clickable { isExpanded = true }) {
        Row {
            Text(current, style = MaterialTheme.typography.h6)
            Spacer(Modifier.width(10.dp))
            Icon(Icons.Filled.ArrowDropDown, current)
        }
        DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            choices.forEachIndexed { index, choice ->
                DropdownMenuItem(onClick = { onChange(index) }) {
                    Text(choice, style = MaterialTheme.typography.h6)
                }
            }
        }
    }
}

@Composable
private fun ResumeButton(modifier: Modifier = Modifier, onResume: () -> Unit) = SetupButton(R.string.resumeGame, modifier, onResume)

@Composable
private fun StartButton(modifier: Modifier, onStart: () -> Unit) = SetupButton(R.string.startGame, modifier, onClick = onStart)

@Composable
private fun ExitButton(modifier: Modifier, onExit: () -> Unit) = SetupButton(R.string.exitGame, modifier, onClick = onExit)

@Preview(showBackground = true)
@Composable
private fun PreviewSetup() {
    SetupContents(
        gameState = GameState.PLAY,
        fieldSize = FieldSize.LARGE,
        difficulty = Difficulty.HARD,
        onSizeChange = {},
        onDifficultyChange = {},
        onResume = {},
        onStart = {},
        onExit = {}
    )
}
