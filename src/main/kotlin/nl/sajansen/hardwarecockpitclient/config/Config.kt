package nl.sajansen.hardwarecockpitclient.config

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.logging.Logger

object Config {
    private val logger = Logger.getLogger(Config.toString())

    var hardwareDeviceConnect: Boolean = true
    var hardwareDeviceComName: String = "USB-to-Serial Port (ch341-uart)"
    var hardwareDeviceComBaudRate: Int = 115200
    var serialMetaBitsValue: Int = 0x05

    var asynchronousUpdates: Boolean = true
    var rotaryMinUpdateInterval: Long = 100
    var ignoreWindowsPlatformCheck: Boolean = false

    // KeyboardConnector
    var keyboardConnectorEnabled: Boolean = false
    var KeyboardConnectorKeyPressDownDuration: Int = 80
    var KeyboardConnectorKeyPressUpDuration: Int = 20

    // JoystickConnector
    var joystickConnectorEnabled: Boolean = true
    var joystickConnectorMaxTrim: Int = 100
    var joystickConnectorTrimAsync: Boolean = false
    var joystickConnectorButtonToggleDuration: Long = 50

    // HardwareDeviceEmulatorConnector
    var hardwareDeviceEmulatorConnectorEnabled: Boolean = false
    var hardwareDeviceEmulatorConnectorHighlightDuration: Long = 200L

    // Logging
    var enableApplicationLoggingToFile: Boolean = true
    var maxLogFileSize: Int = 1024 * 1024    // 1 MB

    fun load() {
        try {
            PropertyLoader.load()
            PropertyLoader.loadConfig(this::class.java)
        } catch (e: Exception) {
            logger.severe("Failed to load Config")
            e.printStackTrace()
        }
    }

    fun save() {
        logger.info("Saving config")
        try {
            if (PropertyLoader.saveConfig(this::class.java)) {
                PropertyLoader.save()
            }
        } catch (e: Exception) {
            logger.severe("Failed to save Config")
            e.printStackTrace()
        }
    }

    fun get(key: String): Any? {
        try {
            return javaClass.getDeclaredField(key).get(this)
        } catch (e: Exception) {
            logger.severe("Could not get config key $key")
            e.printStackTrace()
        }
        return null
    }

    fun set(key: String, value: Any?) {
        try {
            javaClass.getDeclaredField(key).set(this, value)
        } catch (e: Exception) {
            logger.severe("Could not set config key $key")
            e.printStackTrace()
        }
    }

    fun enableWriteToFile(value: Boolean) {
        PropertyLoader.writeToFile = value
    }

    fun fields(): List<Field> {
        val fields = javaClass.declaredFields.filter {
            it.name != "INSTANCE" && it.name != "logger"
                    && Modifier.isStatic(it.modifiers)
        }
        fields.forEach { it.isAccessible = true }
        return fields
    }
}