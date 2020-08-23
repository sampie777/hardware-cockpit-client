package nl.sajansen.hardwarecockpitclient.hardware

import java.nio.ByteBuffer


@ExperimentalUnsignedTypes
data class CockpitDeviceFeedbackData(
    var OPERATION_MODE: UByte,
    var LED_CUSTOM_A: UByte,
    var LED_CUSTOM_B: UByte,
    var BACKLIGHT: UByte,
    var MAX_AIRSPEED: UInt,
    var AUTO_PILOT_AVAILABLE: UByte,
    var INDICATED_AIR_SPEED: Int,
    var GEAR_ACTUAL_LEFT: Short,
    var GEAR_ACTUAL_FRONT: Short,
    var GEAR_ACTUAL_RIGHT: Short,
    var FLAPS_POSITIONS: UByte,
    var PLANE_ON_GROUND: UByte,
    var CLOUD_TURBULENCE: UShort,
    var CRASHED: Byte,
    var PAUSE_FLAG: Byte,
    var OVERSPEED: UByte
) {

    fun toByteArray(): ByteArray {
        val buffer = ByteBuffer.allocate(26)
        OPERATION_MODE.addToByteBuffer(buffer)
        LED_CUSTOM_A.addToByteBuffer(buffer)
        LED_CUSTOM_B.addToByteBuffer(buffer)
        BACKLIGHT.addToByteBuffer(buffer)
        MAX_AIRSPEED.addToByteBuffer(buffer)
        AUTO_PILOT_AVAILABLE.addToByteBuffer(buffer)
        INDICATED_AIR_SPEED.addToByteBuffer(buffer)
        GEAR_ACTUAL_LEFT.addToByteBuffer(buffer)
        GEAR_ACTUAL_FRONT.addToByteBuffer(buffer)
        GEAR_ACTUAL_RIGHT.addToByteBuffer(buffer)
        FLAPS_POSITIONS.addToByteBuffer(buffer)
        PLANE_ON_GROUND.addToByteBuffer(buffer)
        CLOUD_TURBULENCE.addToByteBuffer(buffer)
        CRASHED.addToByteBuffer(buffer)
        PAUSE_FLAG.addToByteBuffer(buffer)
        OVERSPEED.addToByteBuffer(buffer)
        return buffer.array()
    }

    private fun Int.addToByteBuffer(buffer: ByteBuffer) = buffer.putInt(this)
    private fun UInt.addToByteBuffer(buffer: ByteBuffer) = buffer.putInt(this.toInt())
    private fun Short.addToByteBuffer(buffer: ByteBuffer) = buffer.putShort(this)
    private fun UShort.addToByteBuffer(buffer: ByteBuffer) = buffer.putShort(this.toShort())
    private fun Byte.addToByteBuffer(buffer: ByteBuffer) = buffer.put(this)
    private fun UByte.addToByteBuffer(buffer: ByteBuffer) = buffer.put(this.toByte())
}