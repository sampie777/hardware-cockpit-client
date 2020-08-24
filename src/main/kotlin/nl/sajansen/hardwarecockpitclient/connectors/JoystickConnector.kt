package nl.sajansen.hardwarecockpitclient.connectors


import nl.sajansen.hardwarecockpitclient.config.Config
import nl.sajansen.hardwarecockpitclient.hardware.CockpitDevice
import nl.sajansen.hardwarecockpitclient.utils.NumberMap
import nl.sajansen.hardwarecockpitclient.utils.isWindowsPlatform
import org.flypad.joystick.Joystick
import java.util.*
import java.util.logging.Logger

class JoystickConnector : Connector {
    private val logger = Logger.getLogger(JoystickConnector::class.java.name)

    var joystick: Joystick? = null

    override fun enable() {
        if (!Config.ignoreWindowsPlatformCheck && !isWindowsPlatform()) {
            logger.warning("${javaClass.name} is not supported on non Windows platforms. If you really want to do this, turn on the configuration property: ignoreWindowsPlatformCheck")
            return
        }
        super.enable()

        try {
            joystick = Joystick()
        } catch (e: Exception) {
            logger.severe("Failed to create joystick")
            e.printStackTrace()
            disable()
        } catch (e: Error) {
            logger.severe("Failed to create joystick")
            e.printStackTrace()
            disable()
        }
    }

    override fun disable() {
        super.disable()

        try {
            joystick?.close()
        } catch (e: Exception) {
            logger.severe("Failed to create joystick")
            e.printStackTrace()
        }
    }

    private var elevatorTrimPosition: Int = 0
    private var aileronTrimPosition: Int = 0
    private var rudderTrimPosition: Int = 0

    override fun valueUpdate(name: String, value: Any) {
        if (joystick == null) {
            logger.severe("Joystick never initialized!")
            return
        }

        when (name) {
            CockpitDevice.NAME_BUTTON_1 -> toggleButton(0)
            CockpitDevice.NAME_BUTTON_2 -> toggleButton(1)
            CockpitDevice.NAME_BUTTON_3 -> toggleButton(2)
            CockpitDevice.NAME_BUTTON_4 -> toggleButton(3)
            CockpitDevice.NAME_BUTTON_5 -> toggleButton(4)
            CockpitDevice.NAME_BUTTON_6 -> toggleButton(5)
            CockpitDevice.NAME_BUTTON_7 -> toggleButton(6)
            CockpitDevice.NAME_BUTTON_8 -> toggleButton(7)
            CockpitDevice.NAME_BUTTON_9 -> toggleButton(8)
            CockpitDevice.NAME_BUTTON_ATC -> toggleButton(9)
            CockpitDevice.NAME_BUTTON_A -> toggleButtons(10, 11, value as Boolean)
            CockpitDevice.NAME_BUTTON_D -> toggleButton(12)
            CockpitDevice.NAME_BUTTON_PAUSE -> toggleButton(13)

            CockpitDevice.NAME_SWITCH_BCN -> toggleButton(14)
            CockpitDevice.NAME_SWITCH_LAND -> toggleButton(15)
            CockpitDevice.NAME_SWITCH_TAXI -> toggleButton(16)
            CockpitDevice.NAME_SWITCH_NAV -> toggleButton(17)
            CockpitDevice.NAME_SWITCH_STROBE -> toggleButton(18)
            CockpitDevice.NAME_SWITCH_CABIN -> toggleButton(19)
            CockpitDevice.NAME_SWITCH_PARKING_BRAKE -> toggleButton(20)
            CockpitDevice.NAME_SWITCH_MASTER -> toggleButton(21)
            CockpitDevice.NAME_SWITCH_LANDING_GEAR -> {
                toggleButtons(22, 23, value as Boolean)

                joystick?.analog?.set(
                    Joystick.ANALOG_SLIDER,
                    if (value) Joystick.ANALOG_MAX else Joystick.ANALOG_MIN
                )
            }

            CockpitDevice.NAME_SLIDER_FLAPS -> {
                val map = NumberMap(Joystick.ANALOG_MIN, Joystick.ANALOG_MAX, 1, 9)
                joystick?.analog?.set(Joystick.ANALOG_AXIS_X, map.map(value as Int))
            }
            CockpitDevice.NAME_SLIDER_SPOILER -> {
                val map = NumberMap(Joystick.ANALOG_MIN, Joystick.ANALOG_MAX, 0, 16383)
                joystick?.analog?.set(Joystick.ANALOG_AXIS_Y, map.map(value as Int))
            }
            CockpitDevice.NAME_SLIDER_F -> {
                val map = NumberMap(Joystick.ANALOG_MIN, Joystick.ANALOG_MAX, 0, 1556)
                joystick?.analog?.set(Joystick.ANALOG_AXIS_Z, map.map(value as Int))
            }
            CockpitDevice.NAME_ROTARY_TRIM_ELEVATOR -> {
                elevatorTrimPosition += value as Int
                val map = NumberMap(Joystick.ANALOG_MIN, Joystick.ANALOG_MAX, -Config.joystickConnectorMaxTrim, Config.joystickConnectorMaxTrim)
                joystick?.analog?.set(Joystick.ANALOG_ROTATION_X, map.map(elevatorTrimPosition))
            }
            CockpitDevice.NAME_ROTARY_TRIM_AILERONS -> {
                aileronTrimPosition += value as Int
                val map = NumberMap(Joystick.ANALOG_MIN, Joystick.ANALOG_MAX, -Config.joystickConnectorMaxTrim, Config.joystickConnectorMaxTrim)
                joystick?.analog?.set(Joystick.ANALOG_ROTATION_Y, map.map(aileronTrimPosition))
            }
            CockpitDevice.NAME_ROTARY_TRIM_RUDDER -> {
                rudderTrimPosition += value as Int
                val map = NumberMap(Joystick.ANALOG_MIN, Joystick.ANALOG_MAX, -Config.joystickConnectorMaxTrim, Config.joystickConnectorMaxTrim)
                joystick?.analog?.set(Joystick.ANALOG_ROTATION_Z, map.map(rudderTrimPosition))
            }
            else -> return
        }

        flush()
    }

    fun toggleButton(buttonIndex: Int, duration: Long = Config.joystickConnectorButtonToggleDuration) {
        joystick?.digital?.set(buttonIndex, Joystick.DIGITAL_ON)
        clearButtonStateAfterTimeout(buttonIndex, duration)
    }

    fun toggleButtons(
        buttonOnIndex: Int,
        buttonOffIndex: Int,
        value: Boolean,
        duration: Long = Config.joystickConnectorButtonToggleDuration
    ) {
        val buttonIndex = if (value) buttonOnIndex else buttonOffIndex
        joystick?.digital?.set(buttonIndex, Joystick.DIGITAL_ON)
        clearButtonStateAfterTimeout(buttonIndex, duration)
    }

    private fun clearButtonStateAfterTimeout(buttonIndex: Int, duration: Long = 50) {
        Timer().schedule(ClearButtonStateTimerTask(this, buttonIndex), duration)
    }

    fun flush() {
        logger.fine("Sending joystick data")
        try {
            joystick?.send()
        } catch (e: Exception) {
            logger.severe("Failed to set joystick data")
            e.printStackTrace()
        } catch (e: Error) {
            logger.severe("Failed to set joystick data")
            e.printStackTrace()
        }
    }
}

class ClearButtonStateTimerTask(
    private val connector: JoystickConnector,
    private val buttonIndex: Int
) : TimerTask() {
    private val logger = Logger.getLogger(ClearButtonStateTimerTask::class.java.name)

    override fun run() {
        logger.info("Clearing button state for index: $buttonIndex")
        if (connector.joystick == null) {
            logger.warning("Can't clear button state: joystick is null")
            return
        }

        connector.joystick?.digital?.set(buttonIndex, Joystick.DIGITAL_OFF)
        connector.flush()
    }
}