package nl.sajansen.hardwarecockpitclient.hardware.components


import nl.sajansen.hardwarecockpitclient.config.Config
import nl.sajansen.hardwarecockpitclient.connectors.ConnectorRegister
import java.util.logging.Logger

class Rotary(override val id: Int, override val name: String) : Component {
    private val logger = Logger.getLogger(Rotary::class.java.name)

    @Volatile
    private var value: Int = 0
    @Volatile
    private var updating: Boolean = false

    override fun value(): Any? {
        val temp = value
        reset()
        return temp
    }

    fun rawValue(): Int {
        return value
    }

    override fun set(newRawValue: Int) {
        value += convertUnsignedToSigned(newRawValue)  // Make signed
        logger.info("Increasing rotary '$name' value to: $value, with: $newRawValue")

        sendValueUpdate()
    }

    fun convertUnsignedToSigned(newRawValue: Int) = newRawValue.toUInt().toByte()

    override fun reset() {
        logger.info("Resetting rotary '$name' value")
        value = 0
    }

    override fun sendValueUpdate() {
        if (updating) { // Wait till updating is finished before doing new stuff
            logger.info("Connectors still updating, leaving this update alone")
            return
        }

        updating = true
        val tempValue = value
        reset()

        ConnectorRegister.valueUpdate(name, tempValue)
        Thread.sleep(Config.rotaryMinUpdateInterval)   // Prevent too much updates at once

        updating = false

        // Check for value changes happened during update
        if (value == 0) {
            return
        }

        logger.info("Value changed to '$value' during update, re-updating connectors")
        sendValueUpdate()
    }

    override fun toString(): String {
        return "Rotary[$id, $name]"
    }
}