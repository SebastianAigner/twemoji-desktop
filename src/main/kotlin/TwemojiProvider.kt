import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import java.io.File

val client = HttpClient()
val gson = Gson()

object TwemojiProvider {
    class Response(val tree: Array<RepositoryFile>)
    data class RepositoryFile(val path: String, val type: String, val url: String)

    private fun getRepositoryFiles(url: String): Array<RepositoryFile> {
        val response = runBlocking {
            client.get<String>(url)
        }
        return gson.fromJson(response, Response::class.java).tree
    }

    fun getTwemojiCodePoints(): List<String> {
        // https://api.github.com/repos/twitter/twemoji/contents/assets/svg
        val masterTree = getRepositoryFiles("https://api.github.com/repos/twitter/twemoji/git/trees/master")
        val assetsTree = getRepositoryFiles(masterTree.first { it.path == "assets" }.url)
        val svgTree = getRepositoryFiles(assetsTree.first { it.path == "svg" }.url)
        return svgTree.map { it.path.removeSuffix(".svg") }
    }

    fun getMockCodePoints(): List<String> {
        return File("twemojiCodePoints.txt").readLines()
    }


}