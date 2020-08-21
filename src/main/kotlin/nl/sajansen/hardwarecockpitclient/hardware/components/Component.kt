package nl.sajansen.hardwarecockpitclient.hardware.components

interface Component {
    val id: Int
    val name: String

    fun value(): Any?
    fun set(newRawValue: Int)
    fun reset()
    fun sendValueUpdate()
}