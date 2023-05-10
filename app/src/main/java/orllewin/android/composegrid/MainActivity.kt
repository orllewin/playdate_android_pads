package orllewin.android.composegrid

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import orllewin.android.composegrid.ui.theme.ComposeGridTheme

class MainActivity : ComponentActivity(){

    data class Pad(val title: String, val column: Int, val row: Int)

    private val viewModel:SerialViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.usbScan(this){ success ->
            when {
                success -> {
                    Toast.makeText(this, "Playdate found", Toast.LENGTH_SHORT).show()
                    viewModel.initialise(this){ error ->
                        status(error)
                    }
                }
                else -> Toast.makeText(this, "Playdate not found", Toast.LENGTH_SHORT).show()
            }
       }

        val cells = listOf(
            Pad("1x1", 1, 1),
            Pad("2x1", 2, 1),
            Pad("3x1", 3, 1),
            Pad("4x1", 4, 1),
            Pad("1x2", 1, 2),
            Pad("2x2", 2, 2),
            Pad("3x2", 3, 2),
            Pad("4x2", 4, 2))

        val eigengrau = getColor("#16161D")

        setContent {
            ComposeGridTheme {
                Surface(
                    Modifier.fillMaxSize(),
                    color = eigengrau
                ) {
                    LazyHorizontalGrid(
                        //columns = GridCells.Adaptive(minSize = 128.dp)
                        rows = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        /*
                            LazyHorizontalGrid builds these in slightly unintuitive order:
                            _________________
                            | 0 | 2 | 4 | 6 |
                            | 1 | 3 | 5 | 7 |
                            -----------------
                         */
                        items(cells.size) { index ->
                            when (index) {
                                0 -> ProductCell(cells[0], 0)
                                1 -> ProductCell(cells[4], 4)
                                2 -> ProductCell(cells[1], 1)
                                3 -> ProductCell(cells[5], 5)
                                4 -> ProductCell(cells[2], 2)
                                5 -> ProductCell(cells[6], 6)
                                6 -> ProductCell(cells[3], 3)
                                7 -> ProductCell(cells[7], 7)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ProductCell(pad: Pad, index: Int){
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp.value - 12.dp.value
        Card(
            modifier = Modifier
                .width(Dp(screenWidth / 4f))
                .padding(2.dp)
                .clickable {
                    viewModel.serialSend(index)
                },
            border = BorderStroke(2.dp, getColor("#feab06")),
            colors = CardDefaults.cardColors(
                containerColor = getColor("#feab06"),
                contentColor = Color.White
            ),
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = pad.title)
            }
        }
    }

    private fun getColor(colorString: String): Color {
        return Color(android.graphics.Color.parseColor(colorString))
    }

    private fun status(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}