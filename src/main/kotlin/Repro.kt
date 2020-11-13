import androidx.compose.desktop.Window
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageAsset
import java.net.URL

fun main() {
    println(TwemojiProvider.getTwemojiCodePoints().joinToString("\n"))
}

