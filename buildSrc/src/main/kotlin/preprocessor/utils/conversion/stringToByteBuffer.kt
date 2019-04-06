package preprocessor.utils.conversion

import java.nio.ByteBuffer

/**
 * converts a [String] into a [ByteBuffer]
 * @return the resulting conversion
 * @see fileToByteBuffer
 */
fun stringToByteBuffer(f : String) : ByteBuffer {
    return ByteBuffer.wrap(f.toByteArray())
}
