package nl.sajansen.hardwarecockpitclient.connectors


import nl.sajansen.hardwarecockpitclient.config.Config
import nl.sajansen.hardwarecockpitclient.hardware.CockpitDevice
import java.awt.Robot
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.util.logging.Logger
import javax.swing.KeyStroke

class KeyboardConnector : Connector {
    private val logger = Logger.getLogger(KeyboardConnector::class.java.name)

    private var customGOn: Boolean = false
    private var spoilerOn: Boolean = false

    override fun valueUpdate(name: String, value: Any) {
        if (name == CockpitDevice.NAME_SWITCH_G) {
            customGOn = value as Boolean
        }

        // Disable control through put when G is off
        if (!customGOn) {
            logger.info("G is off, inputs wont' be processed")
            return
        }

        when (name) {
            CockpitDevice.NAME_BUTTON_PAUSE -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_PAUSE, 0))
            CockpitDevice.NAME_BUTTON_ATC -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_SCROLL_LOCK, 0))
            CockpitDevice.NAME_BUTTON_1 -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_MASK))
            CockpitDevice.NAME_BUTTON_2 -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_MASK))
            CockpitDevice.NAME_BUTTON_3 -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_MASK))
            CockpitDevice.NAME_BUTTON_4 -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_MASK))
            CockpitDevice.NAME_BUTTON_5 -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.CTRL_MASK))
            CockpitDevice.NAME_BUTTON_6 -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_6, InputEvent.CTRL_MASK))
            CockpitDevice.NAME_BUTTON_7 -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_7, InputEvent.CTRL_MASK))
            CockpitDevice.NAME_BUTTON_8 -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_8, InputEvent.CTRL_MASK))
            CockpitDevice.NAME_BUTTON_9 -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.CTRL_MASK))
            CockpitDevice.NAME_BUTTON_A -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0))
            CockpitDevice.NAME_BUTTON_B -> {
                // Reset Simulation Rate
                for (i in 0..6) {
                    keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), KeyEvent.VK_MINUS)
                }
                keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), KeyEvent.VK_EQUALS)
                keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), KeyEvent.VK_EQUALS)
            }
            CockpitDevice.NAME_BUTTON_D -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK))

            CockpitDevice.NAME_SWITCH_BCN -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.ALT_MASK))
            CockpitDevice.NAME_SWITCH_LAND -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK))
            CockpitDevice.NAME_SWITCH_TAXI -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.ALT_MASK))
            CockpitDevice.NAME_SWITCH_NAV -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_MASK))
            CockpitDevice.NAME_SWITCH_STROBE -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_O, 0))
            CockpitDevice.NAME_SWITCH_CABIN -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.ALT_MASK))
            CockpitDevice.NAME_SWITCH_PARKING_BRAKE -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_DECIMAL, InputEvent.CTRL_MASK))
            CockpitDevice.NAME_SWITCH_MASTER -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.SHIFT_MASK))
            CockpitDevice.NAME_SWITCH_LANDING_GEAR -> {
                when (value as Boolean) {
                    false -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK))
                    else -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_G, 0))
                }
            }

            CockpitDevice.NAME_SLIDER_FLAPS -> {
                val index = value as Int
                when {
                    index < 2 -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0))
                    index < 5 -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0), KeyEvent.VK_1)
                    index < 8 -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0), KeyEvent.VK_2)
                    else -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0), KeyEvent.VK_3)
                }
            }
            CockpitDevice.NAME_SLIDER_SPOILER -> {
                val newValue = value as Int
                if (!(newValue > 8000 && !spoilerOn) && !(newValue < 8000 && spoilerOn)) {
                    return
                }
                spoilerOn = newValue > 8000
                keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_DIVIDE, 0))
            }
        }
    }

    private fun keyPress(keyEvent1: KeyStroke, keyCode2: Int? = null) {
        logger.info("Performing keyPress for keyEvent: ${keyEvent1.readableString()} (keyCode2: $keyCode2)")
        try {
            val robot = Robot()
            if (keyEvent1.modifiers.and(KeyEvent.CTRL_MASK) != 0) {
                robot.keyPress(KeyEvent.VK_CONTROL)
            }
            if (keyEvent1.modifiers.and(KeyEvent.SHIFT_MASK) != 0) {
                robot.keyPress(KeyEvent.VK_SHIFT)
            }
            if (keyEvent1.modifiers.and(KeyEvent.ALT_MASK) != 0) {
                robot.keyPress(KeyEvent.VK_ALT)
            }

            robot.keyPress(keyEvent1.keyCode)
            if (keyCode2 != null) {
                robot.keyPress(keyCode2)
            }

            robot.delay(Config.keyPressDownDuration)

            if (keyCode2 != null) {
                robot.keyRelease(keyCode2)
            }
            robot.keyRelease(keyEvent1.keyCode)

            if (keyEvent1.modifiers.and(KeyEvent.ALT_MASK) != 0) {
                robot.keyRelease(KeyEvent.VK_ALT)
            }
            if (keyEvent1.modifiers.and(KeyEvent.SHIFT_MASK) != 0) {
                robot.keyRelease(KeyEvent.VK_SHIFT)
            }
            if (keyEvent1.modifiers.and(KeyEvent.CTRL_MASK) != 0) {
                robot.keyRelease(KeyEvent.VK_CONTROL)
            }
            robot.delay(Config.keyPressUpDuration)
        } catch (e: Exception) {
            logger.warning("Failed to execute key stroke ${keyEvent1.readableString()} (keyCode2: $keyCode2)")
            e.printStackTrace()
        } catch (e: Error) {
            logger.warning("Failed to execute key stroke ${keyEvent1.readableString()} (keyCode2: $keyCode2)")
            e.printStackTrace()
        }
    }

    private fun KeyStroke.readableString(): String {
        return listOf(
            KeyEvent.getKeyModifiersText(this.modifiers),
            KeyEvent.getKeyText(this.keyCode)
        )
            .filter { !it.isBlank() }
            .joinToString("+")
    }
}