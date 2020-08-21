package nl.sajansen.hardwarecockpitclient.hardware.components


import nl.sajansen.hardwarecockpitclient.connectors.ConnectorRegister
import nl.sajansen.hardwarecockpitclient.utils.NumberMap
import java.util.logging.Logger

class Slider(override val id: Int, override val name: String, private val numberMap: NumberMap? = null) : Component {
    private val logger = Logger.getLogger(Slider::class.java.name)

    private var value: Int = 0

    override fun value(): Any? {
        val temp = value
        reset()
        return temp
    }

    override fun set(newRawValue: Int) {
        value = mapValue(newRawValue)

        logger.info("Setting slider '$name' value to: $value")
        sendValueUpdate()
    }

    override fun reset() {
        logger.info("Resetting slider '$name' value")
        value = 0
    }

    override fun sendValueUpdate() {
        ConnectorRegister.valueUpdate(name, value)
    }

    override fun toString(): String {
        return "Slider[$id, $name]"
    }

    private fun mapValue(value: Int): Int {
        logger.info("Mapping raw value: $value")
        if (numberMap == null) {
            return value
        }
        return numberMap.map(value)
    }
}