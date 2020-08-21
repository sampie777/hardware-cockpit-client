package nl.sajansen.hardwarecockpitclient.hardware.serial


enum class SerialDataType(val id: Int) {
    TOGGLE(0),
    BOOLEAN(1),
    ABSOLUTE_VALUE(2),
    RELATIVE_VALUE(3),
    UPDATE_REQUEST(4);

    companion object {
        fun fromInt(value: Int) = values().first { it.id == value }
    }
}