package nl.sajansen.hardwarecockpitclient.hardware.serial

enum class SerialOperationMode(val id: Byte) {
    OPERATION_MODE_UNCONNECTED(0),
    OPERATION_MODE_SIMULATOR(1),
    OPERATION_MODE_CALIBRATION(2);

    companion object {
        fun fromInt(value: Int) = values().first { it.id == value.toByte() }
    }
}