package nl.sajansen.hardwarecockpitclient.hardware.components


import nl.sajansen.hardwarecockpitclient.connectors.ConnectorRegister
import java.util.logging.Logger

class Switch(override val id: Int, override val name: String) : Component {
    private val logger = Logger.getLogger(Switch::class.java.name)

    private var value: Boolean = false

    override fun value(): Any? {
        return value
    }

    override fun set(newRawValue: Int) {
        value = newRawValue != 0

        logger.info("Setting switch '$name' value to: $value")
        sendValueUpdate()
    }

    override fun reset() {
        logger.info("Resetting switch '$name' value")
        value = false
    }

    override fun sendValueUpdate() {
        ConnectorRegister.valueUpdate(name, value)
    }

    override fun toString(): String {
        return "Switch[$id, $name]"
    }
}