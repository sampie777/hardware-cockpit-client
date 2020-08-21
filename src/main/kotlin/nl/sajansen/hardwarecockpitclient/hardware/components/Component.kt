package nl.sajansen.hardwarecockpitclient.hardware.components

interface Component {
    val id: Int
    val name: String

    fun value(): Any?
    fun set(serialValue: ByteArray)
    fun reset()
    fun sendValueUpdate()
}