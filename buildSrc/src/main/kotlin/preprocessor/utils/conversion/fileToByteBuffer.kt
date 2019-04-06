package preprocessor.utils.conversion

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer

/**
 * converts a [File] into a [ByteBuffer]
 * @return the resulting conversion
 * @see stringToByteBuffer
 */
fun fileToByteBuffer(f : File) : ByteBuffer {
    val file = RandomAccessFile(f, "r")
    val fileChannel = file.getChannel()

    var i = 0
    var buffer = ByteBuffer.allocate(fileChannel.size().toInt())
    fileChannel.read(buffer)
    buffer.flip()
    return buffer
}
