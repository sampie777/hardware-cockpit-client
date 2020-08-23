package nl.sajansen.hardwarecockpitclient.hardware

import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals


@ExperimentalUnsignedTypes
class CockpitDeviceFeedbackDataTest {

    @Test
    fun testByteArrayHasCorrectSize() {
        val updateData = CockpitDeviceFeedbackData(
            0u,
            0u,
            0u,
            0u,
            0u,
            0u,
            0,
            0,
            0,
            0,
            0u,
            0u,
            0u,
            0,
            0,
            0u
        )

        // When
        val bytes = updateData.toByteArray()

        assertEquals(26, bytes.size)
    }

    @Test
    fun testByteArrayHasCorrectValues() {
        val updateData = CockpitDeviceFeedbackData(
            255u,
            0u,
            0u,
            0u,
            UInt.MAX_VALUE,
            0u,
            0,
            1000,
            Short.MAX_VALUE,
            0,
            0u,
            0u,
            65534u,
            126,
            0,
            0u
        )

        // When
        val bytes = updateData.toByteArray()

        assertEquals(255u, bytes[0].toUByte())
        assertEquals(UInt.MAX_VALUE, ByteBuffer.wrap(bytes.sliceArray(4..7)).int.toUInt())
        assertEquals(1000, ByteBuffer.wrap(bytes.sliceArray(13..14)).short)
        assertEquals(Short.MAX_VALUE, ByteBuffer.wrap(bytes.sliceArray(15..16)).short)
        assertEquals(65534u, ByteBuffer.wrap(bytes.sliceArray(21..22)).short.toUShort())
        assertEquals(126, bytes[23])
        assertEquals(0, bytes[24])
    }

}