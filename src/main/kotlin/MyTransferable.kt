import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.File

class MyTransferable(val file: File): Transferable {
    override fun getTransferDataFlavors(): Array<DataFlavor> {
        return arrayOf(DataFlavor.javaFileListFlavor)
    }

    override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean {
        if(flavor == null) return false
        return flavor.equals(DataFlavor.javaFileListFlavor)
    }

    override fun getTransferData(flavor: DataFlavor?): Any {
        if(flavor?.equals(DataFlavor.javaFileListFlavor) == true) {
            return listOf(file)
        }
        throw UnsupportedFlavorException(flavor)
    }
}