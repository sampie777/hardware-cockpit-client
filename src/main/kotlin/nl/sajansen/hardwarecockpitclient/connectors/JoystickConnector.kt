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

    var joystick1: Joystick? = null
    var joystick2: Joystick? = null

    override fun enable() {
        if (!Config.ignoreWindowsPlatformCheck && !isWindowsPlatform()) {
            logger.warning("${javaClass.name} is not supported on non Windows platforms. If you really want to do this, turn on the configuration property: ignoreWindowsPlatformCheck")
            return
        }
        super.enable()

        joystick1 = try {
            Joystick(1)
        } catch (e: Throwable) {
            logger.severe("Failed to create joystick 1")
            e.printStackTrace()
            null
        }

        joystick2 = try {
            Joystick(2)
        } catch (e: Throwable) {
            logger.severe("Failed to create joystick 2")
            e.printStackTrace()
            null
        }

        if (joystick1 == null || joystick2 == null) {
            logger.warning("No joysticks successfully created, disabling controller")
            disable()
        }
    }

    override fun disable() {
        super.disable()

        try {
            joystick1?.close()
        } catch (e: Exception) {
            logger.severe("Failed to create joystick 1")
            e.printStackTrace()
        }

        try {
            joystick2?.close()
        } catch (e: Exception) {
            logger.severe("Failed to create joystick 2")
            e.printStackTrace()
        }
    }

    var elevatorTrimPosition: Int = 0
    private var aileronTrimPosition: Int = 0
    private var rudderTrimPosition: Int = 0

    override fun valueUpdate(name: String, value: Any) {
        if (joystick1 == null || joystick2 == null) {
            logger.severe("Joystick(s) never initialized!")
            return
        }

        when (name) {
            CockpitDevice.NAME_BUTTON_1 -> toggleButton(joystick1!!, 0)
            CockpitDevice.NAME_BUTTON_2 -> toggleButton(joystick1!!, 1)
            CockpitDevice.NAME_BUTTON_3 -> toggleButton(joystick1!!, 2)
            CockpitDevice.NAME_BUTTON_4 -> toggleButton(joystick1!!, 3)
            CockpitDevice.NAME_BUTTON_5 -> toggleButton(joystick1!!, 4)
            CockpitDevice.NAME_BUTTON_6 -> toggleButton(joystick1!!, 5)
            CockpitDevice.NAME_BUTTON_7 -> toggleButton(joystick1!!, 6)
            CockpitDevice.NAME_BUTTON_8 -> toggleButton(joystick1!!, 7)
            CockpitDevice.NAME_BUTTON_9 -> toggleButton(joystick1!!, 8)
            CockpitDevice.NAME_BUTTON_ATC -> toggleButton(joystick1!!, 9)
            CockpitDevice.NAME_BUTTON_A -> toggleButton(joystick1!!, 10)
            CockpitDevice.NAME_BUTTON_B -> {
                for (i in 0..6) {
                    toggleButton(joystick1!!, 12, async = false)
                }
                for (i in 0..1) {
                    toggleButton(joystick1!!, 13, async = false)
                }
            }
            CockpitDevice.NAME_BUTTON_C -> toggleButton(joystick1!!, 14)
            CockpitDevice.NAME_BUTTON_D -> toggleButton(joystick1!!, 15)
            CockpitDevice.NAME_BUTTON_PAUSE -> toggleButton(joystick1!!, 16)

            CockpitDevice.NAME_SWITCH_BCN -> toggleButtons(joystick1!!, 17, 18, value as Boolean)
            CockpitDevice.NAME_SWITCH_LAND -> toggleButtons(joystick1!!, 19, 10, value as Boolean)
            CockpitDevice.NAME_SWITCH_TAXI -> toggleButtons(joystick1!!, 21, 22, value as Boolean)
            CockpitDevice.NAME_SWITCH_NAV -> toggleButtons(joystick1!!, 23, 24, value as Boolean)
            CockpitDevice.NAME_SWITCH_STROBE -> toggleButtons(joystick1!!, 25, 26, value as Boolean)
            CockpitDevice.NAME_SWITCH_CABIN -> toggleButtons(joystick1!!, 27, 28, value as Boolean)
            CockpitDevice.NAME_SWITCH_G -> toggleButtons(joystick2!!, 0, 1, value as Boolean)
            CockpitDevice.NAME_SWITCH_PARKING_BRAKE -> toggleButtons(joystick2!!, 2, 3, value as Boolean)
            CockpitDevice.NAME_SWITCH_MASTER -> toggleButtons(joystick2!!, 4, 5, value as Boolean)
            CockpitDevice.NAME_SWITCH_LANDING_GEAR -> {
                toggleButtons(joystick2!!, 6, 7, value as Boolean)

                joystick2?.analog?.set(
                    Joystick.ANALOG_SLIDER,
                    if (value) Joystick.ANALOG_MAX else Joystick.ANALOG_MIN
                )
            }

            CockpitDevice.NAME_SLIDER_FLAPS -> {
                val map = NumberMap(Joystick.ANALOG_MIN, Joystick.ANALOG_MAX, 1, 9)
                joystick1?.analog?.set(Joystick.ANALOG_AXIS_X, map.map(value as Int))
            }
            CockpitDevice.NAME_SLIDER_SPOILER -> {
                val map = NumberMap(Joystick.ANALOG_MIN, Joystick.ANALOG_MAX, 0, 16383)
                joystick1?.analog?.set(Joystick.ANALOG_AXIS_Y, map.map(value as Int))
            }
            CockpitDevice.NAME_SLIDER_F -> {
                val map = NumberMap(Joystick.ANALOG_MIN, Joystick.ANALOG_MAX, 0, 1556)
                joystick1?.analog?.set(Joystick.ANALOG_AXIS_Z, map.map(value as Int))
            }

            CockpitDevice.NAME_ROTARY_TRIM_ELEVATOR -> {
                toggleButtons(joystick2!!, 8, 9, (value as Int) != 0, async = Config.joystickConnectorTrimAsync)
                logger.info("Value for trim elevator: $value")

                elevatorTrimPosition += value
                val map = NumberMap(
                    Joystick.ANALOG_MIN,
                    Joystick.ANALOG_MAX,
                    -Config.joystickConnectorMaxTrim,
                    Config.joystickConnectorMaxTrim
                )
                logger.info("Analog X axis value for trim elevator: ${map.map(elevatorTrimPosition)}")
                joystick2?.analog?.set(Joystick.ANALOG_ROTATION_X, map.map(elevatorTrimPosition))
            }
            CockpitDevice.NAME_ROTARY_TRIM_AILERONS -> {
                toggleButtons(joystick2!!, 10, 11, (value as Int) != 0, async = Config.joystickConnectorTrimAsync)

                aileronTrimPosition += value
                val map = NumberMap(
                    Joystick.ANALOG_MIN,
                    Joystick.ANALOG_MAX,
                    -Config.joystickConnectorMaxTrim,
                    Config.joystickConnectorMaxTrim
                )
                joystick2?.analog?.set(Joystick.ANALOG_ROTATION_Y, map.map(aileronTrimPosition))
            }
            CockpitDevice.NAME_ROTARY_TRIM_RUDDER -> {
                toggleButtons(joystick2!!, 12, 13, (value as Int) != 0, async = Config.joystickConnectorTrimAsync)

                rudderTrimPosition += value
                val map = NumberMap(
                    Joystick.ANALOG_MIN,
                    Joystick.ANALOG_MAX,
                    -Config.joystickConnectorMaxTrim,
                    Config.joystickConnectorMaxTrim
                )
                joystick2?.analog?.set(Joystick.ANALOG_ROTATION_Z, map.map(rudderTrimPosition))
            }
            CockpitDevice.NAME_ROTARY_AP_SPEED -> toggleButtons(joystick2!!, 14, 15, (value as Int) != 0, async = Config.joystickConnectorTrimAsync)
            CockpitDevice.NAME_ROTARY_AP_HEADING -> toggleButtons(joystick2!!, 16, 17, (value as Int) != 0, async = Config.joystickConnectorTrimAsync)
            CockpitDevice.NAME_ROTARY_AP_ALTITUDE -> toggleButtons(joystick2!!, 18, 19, (value as Int) != 0, async = Config.joystickConnectorTrimAsync)
            CockpitDevice.NAME_ROTARY_AP_VSPEED -> toggleButtons(joystick2!!, 20, 21, (value as Int) != 0, async = Config.joystickConnectorTrimAsync)
            CockpitDevice.NAME_ROTARY_E -> toggleButtons(joystick2!!, 22, 23, (value as Int) != 0, async = Config.joystickConnectorTrimAsync)
            else -> return
        }
    }

    fun toggleButtons(
        joystick: Joystick,
        buttonOnIndex: Int,
        buttonOffIndex: Int,
        value: Boolean,
        duration: Long = Config.joystickConnectorButtonToggleDuration,
        async: Boolean = true
    ) {
        val buttonIndex = if (value) buttonOnIndex else buttonOffIndex
        toggleButton(joystick, buttonIndex, duration, async = async)
    }

    fun toggleButton(
        joystick: Joystick,
        buttonIndex: Int,
        duration: Long = Config.joystickConnectorButtonToggleDuration,
        async: Boolean = true
    ) {
        joystick.digital[buttonIndex] = Joystick.DIGITAL_ON
        joystick.flush()

        if (async) {
            clearButtonStateAfterTimeout(joystick, buttonIndex, duration)
        } else {
            Thread.sleep(duration)
            joystick.digital[buttonIndex] = Joystick.DIGITAL_OFF
            joystick.flush()
        }
    }

    private fun clearButtonStateAfterTimeout(
        joystick: Joystick,
        buttonIndex: Int,
        duration: Long = 50
    ) {
        Timer().schedule(ClearButtonStateTimerTask(joystick, buttonIndex), duration)
    }
}

class ClearButtonStateTimerTask(
    private val joystick: Joystick,
    private val buttonIndex: Int
) : TimerTask() {
    private val logger = Logger.getLogger(ClearButtonStateTimerTask::class.java.name)

    override fun run() {
        logger.info("Clearing button state for index: $buttonIndex")
        joystick.digital[buttonIndex] = Joystick.DIGITAL_OFF
        joystick.flush()
    }
}