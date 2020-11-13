import java.awt.datatransfer.Transferable
import java.awt.datatransfer.DataFlavor
import kotlin.Throws
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException

class FileTransferable(private val listOfFiles: List<*>) : Transferable {
    override fun getTransferDataFlavors(): Array<DataFlavor> {
        return arrayOf(DataFlavor.javaFileListFlavor)
    }

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean {
        return DataFlavor.javaFileListFlavor.equals(flavor)
    }

    @Throws(UnsupportedFlavorException::class, IOException::class)
    override fun getTransferData(flavor: DataFlavor): Any {
        return listOfFiles
    }
}