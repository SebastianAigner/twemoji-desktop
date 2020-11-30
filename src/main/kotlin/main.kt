import androidx.compose.desktop.Window
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageAsset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import io.ktor.client.features.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Toolkit
import java.awt.datatransfer.*
import java.io.File

data class Twemoji(val name: String, val codepoint: String) {
    companion object {
        fun getAll(): List<Twemoji> =
            File("twemoji-amazing.cfd").readLines().map {
                val (name, codepoint) = it.split(":")
                Twemoji(name, codepoint)
            }
    }
}

fun main() = Window {
    var text by remember { mutableStateOf("Hello, World!") }
    val unicodePoints = remember { Twemoji.getAll() }
    var search by remember { mutableStateOf("") }



    MaterialTheme {
        Column {
            TextField(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                value = search,
                onValueChange = { search = it })
            LazyColumnFor(unicodePoints.filter {
                it.name.contains(search) || search.isBlank()
            }) {
                EmojiCell(it)
            }
        }
    }
}

val twemojiAssets = mutableMapOf<Twemoji, ImageBitmap>()
suspend fun getTwemojiImage(twemoji: Twemoji): ImageBitmap {
    return twemojiAssets.getOrPut(twemoji) { obtainTwemojiImage(twemoji) }
}

suspend fun obtainTwemojiImage(twemoji: Twemoji): ImageBitmap {
    val byteArray = client.get<ByteArray>(pngUrl(twemoji.codepoint))
    val asset = withContext(Dispatchers.IO) {
        org.jetbrains.skija.Image.makeFromEncoded(byteArray).asImageBitmap()
    }
    return asset
}

@Composable
fun EmojiCell(twemoji: Twemoji) {
    var imageFile by remember(twemoji.codepoint) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(twemoji) {
        imageFile = getTwemojiImage(twemoji)
    }
    Card(shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).clickable {
            GlobalScope.launch {
                copyEmoji(twemoji)
            }
        }.fillMaxSize())
    {
        Row {
            if (imageFile != null) {
                Image(imageFile!!, modifier = Modifier.padding(10.dp).width(36.dp).height(36.dp))
            } else {
                Text("...", modifier = Modifier.padding(10.dp).width(36.dp).height(36.dp))
            }
            Text(twemoji.name, modifier = Modifier.align(Alignment.CenterVertically))
        }
    }
}

suspend fun copyEmoji(twemoji: Twemoji) {
    val svgString = client.get<String>(svgUrl(twemoji.codepoint))
    val file = File.createTempFile("tmp", ".svg").apply { writeText(svgString) }
    val sysClip = Toolkit.getDefaultToolkit().systemClipboard;
    sysClip.setContents(MyTransferable(file), null)
    println("put $twemoji in the clipboard!")
}

fun pngUrl(codepoint: String): String {
    return "https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/$codepoint.png"
}

fun svgUrl(codepoint: String): String {
    return "https://raw.githubusercontent.com/twitter/twemoji/master/assets/svg/$codepoint.svg"
}