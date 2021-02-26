import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import org.jetbrains.skija.Image
import java.io.ByteArrayOutputStream
import java.io.File

fun svgAsset(path: String): ImageBitmap {
    val f = File(path)
    val buffer = ByteArrayOutputStream()
    val transcoderOutput = TranscoderOutput(buffer)

    // Convert SVG to PNG
    val pngTranscoder = PNGTranscoder()
    pngTranscoder.transcode(TranscoderInput(f.reader()), transcoderOutput)

    val ba = buffer.toByteArray()

    // Clean Up
    buffer.flush()
    buffer.close()

    return org.jetbrains.skija.Image.makeFromEncoded(ba).asImageBitmap()
}