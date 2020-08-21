package nl.sajansen.hardwarecockpitclient.hardware.components


import nl.sajansen.hardwarecockpitclient.connectors.ConnectorRegister
import java.util.logging.Logger

class Button(override val id: Int, override val name: String) : Component {
    private val logger = Logger.getLogger(Button::class.java.name)

    private var toggled: Boolean = false

    override fun value(): Any? {
        val temp = toggled
        reset()
        return temp
    }

    override fun set(serialValue: ByteArray) {
        if (toggled) {
            logger.info("Button value can only be set to TRUE. To turn button off, use reset().")
            return
        }

        toggled = serialValue[0].toInt() != 0

        logger.info("Setting button '$name' value to: $toggled")
        sendValueUpdate()
        reset()
    }

    override fun reset() {
        logger.info("Resetting button '$name' value")
        toggled = false
    }

    override fun sendValueUpdate() {
        ConnectorRegister.valueUpdate(name, toggled)
    }

    override fun toString(): String {
        return "Button[$id, $name]"
    }
}