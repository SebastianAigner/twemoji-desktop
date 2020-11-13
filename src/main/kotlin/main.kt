import androidx.compose.desktop.Window
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageAsset
import androidx.compose.ui.graphics.asImageAsset
import androidx.compose.ui.unit.dp
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL

data class Twemoji(val name: String, val codepoint: String) {
    companion object {
        fun getAll(): List<Twemoji> =
            File("twemoji-amazing.cfd").readLines().map {
                val (name, codepoint) = it.split(":")
//                println(name + " = " + codepoint)
                Twemoji(name, codepoint)
            }
    }
}

fun main() = Window {
    println(TwemojiProvider.getTwemojiCodePoints())
    var text by remember { mutableStateOf("Hello, World!") }
    val unicodePoints = remember { Twemoji.getAll() }
    val svgThing = remember { svgAsset("rails.svg") }
    var search by remember { mutableStateOf("") }

    MaterialTheme {
        Column {
            TextField(value = search, onValueChange = { search = it })
            Button(onClick = {
                text = "Hello, Desktop!"
            }) {
                Text(text)
            }
            LazyColumnFor(unicodePoints.filter {
                it.name.contains(search) || search.isBlank()
            }.chunked(5)) {
                emojiRow(it)
            }
        }
    }
}

@Composable
fun EmojiCell(twemoji: Twemoji) {
    var imageFile by remember(twemoji.codepoint) { mutableStateOf<ImageAsset?>(null) }
    onCommit(twemoji.codepoint) {
        println("composing emoji $twemoji")
        val job = CoroutineScope(Dispatchers.Main.immediate).launch {
            // Start loading the image and await the result.
            val asset = org.jetbrains.skija.Image.makeFromEncoded(client.get<ByteArray>(pngUrl(twemoji.codepoint))).asImageAsset()
            println("obtained asset for ${twemoji.codepoint}")
            imageFile = asset
        }
    }
    Column {
        if(imageFile != null) {
            Image(asset = imageFile!!, modifier = Modifier.width(36.dp).height(36.dp))
        }
        else {
            Text("...", modifier = Modifier.width(36.dp).height(36.dp))
        }
    }
}

@Composable
fun emojiRow(emojis: List<Twemoji>) {
    Row {
        for(emoji in emojis) {
            EmojiCell(emoji)
        }
    }
}

@Composable
fun image(url: String) = remember(url) {
    org.jetbrains.skija.Image.makeFromEncoded(URL(url).readBytes()).asImageAsset()
}

fun pngUrl(codepoint: String): String {
    return "https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/$codepoint.png"
}