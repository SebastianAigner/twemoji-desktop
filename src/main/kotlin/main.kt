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
import androidx.compose.ui.graphics.asImageAsset
import androidx.compose.ui.unit.dp
import io.ktor.client.features.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Toolkit
import java.awt.datatransfer.*
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URL
import java.util.ArrayList

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
    var text by remember { mutableStateOf("Hello, World!") }
    val unicodePoints = remember { Twemoji.getAll() }
    val svgThing = remember { svgAsset("rails.svg") }
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

val twemojiAssets = mutableMapOf<Twemoji, ImageAsset>()
suspend fun getTwemojiImage(twemoji: Twemoji): ImageAsset {
    return twemojiAssets.getOrPut(twemoji) { obtainTwemojiImage(twemoji) }
}

suspend fun obtainTwemojiImage(twemoji: Twemoji): ImageAsset {
    val byteArray = client.get<ByteArray>(pngUrl(twemoji.codepoint))
    val asset = withContext(Dispatchers.IO) {
        org.jetbrains.skija.Image.makeFromEncoded(byteArray).asImageAsset()
    }
    return asset
}

@Composable
fun EmojiCell(twemoji: Twemoji) {
    var imageFile by remember(twemoji.codepoint) { mutableStateOf<ImageAsset?>(null) }

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
                Image(asset = imageFile!!, modifier = Modifier.padding(10.dp).width(36.dp).height(36.dp))
            } else {
                Text("...", modifier = Modifier.padding(10.dp).width(36.dp).height(36.dp))
            }
            Text(twemoji.name, modifier = Modifier.align(Alignment.CenterVertically))
        }
    }
}

suspend fun copyEmoji(twemoji: Twemoji) {
    val svgString = client.get<String>(svgUrl(twemoji.codepoint))
    val sysClip = Toolkit.getDefaultToolkit().systemClipboard;
    val svgClip = SvgClip(svgString)
    sysClip.setContents(svgClip, null)
    println("put $twemoji in the clipboard!")
}

class SvgClip(val svgString: String) : Transferable {
    val svgFlavor = DataFlavor("image/svg+xml; class=java.io.InputStream", "Scalable Vector Graphic")
    val supportedFlavors = arrayOf(svgFlavor)

    init {
        (SystemFlavorMap.getDefaultFlavorMap() as SystemFlavorMap)
            .addUnencodedNativeForFlavor(svgFlavor, "image/svg+xml")
    }

    override fun getTransferDataFlavors(): Array<DataFlavor> {
        return supportedFlavors
    }

    override fun isDataFlavorSupported(flavor: DataFlavor?) = true

    override fun getTransferData(flavor: DataFlavor?): Any {
        return svgString
    }

}

fun copyFile() {
    val file = File("eyebrow.svg")
    val listOfFiles = mutableListOf<File>()
    listOfFiles.add(file)
    val ft = FileTransferable(listOfFiles)
    Toolkit.getDefaultToolkit().systemClipboard.setContents(ft) { clipboard, contents -> println("Lost ownership") }
}


fun pngUrl(codepoint: String): String {
    return "https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/$codepoint.png"
}

fun svgUrl(codepoint: String): String {
    return "https://raw.githubusercontent.com/twitter/twemoji/master/assets/svg/$codepoint.svg"
}