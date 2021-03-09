/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androiddevchallenge.ui.theme.MyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ceil

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                MyApp()
            }
        }
    }
}

// Start building your app here!
@Composable
fun MyApp() {
    var count by remember { mutableStateOf(0) }

    LaunchedEffect(
        key1 = count > 0,
        block = {
            launch {
                while (count > 0) {
                    delay(100)
                    count--
                }
            }
        }
    )

    Surface(color = MaterialTheme.colors.background) {
        if (count > 0) {
            Countdown(count = count)
        } else {
            TimeSet() {
                count = it
            }
        }
    }
}

@Preview
@Composable
fun TimeSet(onTimeSet: (seconds: Int) -> Unit) {
    var hours by remember { mutableStateOf(0) }
    var minutes by remember { mutableStateOf(0) }
    var seconds by remember { mutableStateOf(0) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            NumberStepper(value = hours, onValueChange = { hours = it })
            Text(" : ", style = MaterialTheme.typography.h1)
            NumberStepper(value = minutes, onValueChange = { minutes = it })
            Text(" : ", style = MaterialTheme.typography.h1)
            NumberStepper(value = seconds, onValueChange = { seconds = it })
        }
        Spacer(modifier = Modifier.size(32.dp))
        Button(onClick = { onTimeSet(hours * 60 * 60 + minutes * 60 + seconds) }) {
            Text(text = "GO!")
        }
    }
}

@Composable
fun NumberStepper(modifier: Modifier = Modifier, value: Int, onValueChange: (Int) -> Unit) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { onValueChange((value + 1).coerceAtMost(59)) }) {
            Text("+", style = MaterialTheme.typography.h4)
        }
        Text(String.format("%02d", value), modifier = Modifier, style = MaterialTheme.typography.h3, textAlign = TextAlign.Center)
        Button(onClick = { onValueChange((value - 1).coerceAtLeast(0)) }) {
            Text("-", style = MaterialTheme.typography.h4)
        }
    }
}

@Composable
fun Countdown(count: Int) {
    val hours = count / 60 / 60
    val hoursStr = String.format("%02d", hours)
    val mins = (count - hours * 60 * 60) / 60
    val minsStr = String.format("%02d", mins)
    val secs = count % 60
    val secsStr = String.format("%02d", secs)

    Column(
        Modifier
            .fillMaxSize()
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(text = "$hoursStr : $minsStr : $secsStr", style = MaterialTheme.typography.h2)
        }
        Spacer(modifier = Modifier.height(32.dp))
        CountGrid(color = Color.Green, count = hours, modifier = Modifier.fillMaxWidth())
        CountGrid(color = Color.Blue, count = mins, modifier = Modifier.fillMaxWidth())
        CountGrid(color = Color.Red, count = secs, modifier = Modifier.fillMaxWidth())
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CountGrid(color: Color, count: Int, modifier: Modifier = Modifier) {
    SquareGrid(squaresPerRow = 10, modifier = modifier) {
        (0 until 60).forEach {
            AnimatedVisibility(visible = it < count) {
                Square(color)
            }
        }
    }
}

@Composable
fun Square(color: Color) {
    val modifier = Modifier
        .background(
            Brush.verticalGradient(
                colors = listOf(
                    color,
                    color.copy(alpha = 0.2f)
                )
            )
        )
        .border(1.dp, Color.Black)
    Box(modifier)
}

@Composable
fun SquareGrid(squaresPerRow: Int, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        check(constraints.hasBoundedWidth) {
            "Unbounded width not supported"
        }

        val columnsWidth = constraints.maxWidth / squaresPerRow
        val itemConstraints = constraints.copy(
            minWidth = columnsWidth,
            maxWidth = columnsWidth,
            minHeight = columnsWidth,
            maxHeight = columnsWidth
        )
        val placeables = measurables.map {
            it.measure(itemConstraints)
        }

        val height =
            (ceil(placeables.count().toFloat() / squaresPerRow.toFloat()) * columnsWidth).toInt()
        layout(width = constraints.maxWidth, height = height) {
            var x = 0
            var y = 0
            placeables.forEachIndexed { index, placeable ->
                placeable.place(x * columnsWidth, y)
                x++
                if (x >= squaresPerRow) {
                    x = 0
                    y += columnsWidth
                }
            }
        }
    }
}
