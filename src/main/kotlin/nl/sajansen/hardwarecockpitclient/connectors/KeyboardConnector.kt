package nl.sajansen.hardwarecockpitclient.connectors


import nl.sajansen.hardwarecockpitclient.hardware.CockpitDevice
import java.awt.Robot
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.util.logging.Logger
import javax.swing.KeyStroke

class KeyboardConnector : Connector {
    private val logger = Logger.getLogger(KeyboardConnector::class.java.name)

    override fun valueUpdate(name: String, value: Any?) {
        println("I must perform an action for '$name' with value: $value")

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
            CockpitDevice.NAME_BUTTON_B -> {
                // Reset Simulation Rate
                for (i in 0..10) {
                    keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), KeyEvent.VK_MINUS)
                }
                keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), KeyEvent.VK_PLUS)
                keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), KeyEvent.VK_PLUS)
            }
            CockpitDevice.NAME_BUTTON_D -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK))

            CockpitDevice.NAME_SWITCH_LAND -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK))
            CockpitDevice.NAME_SLIDER_FLAPS -> {
                when (value) {
                    0 -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_0, 0))
                    1 -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0))
                    2 -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0))
                    else -> keyPress(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0))
                }
            }
        }
    }

    private fun keyPress(keyEvent: KeyStroke) {
        logger.info("Performing keyPress for keyEvent: ${keyEvent.readableString()}")
        try {
            val robot = Robot()
            if (keyEvent.modifiers.and(KeyEvent.CTRL_MASK) != 0) {
                robot.keyPress(KeyEvent.VK_CONTROL)
            }
            if (keyEvent.modifiers.and(KeyEvent.SHIFT_MASK) != 0) {
                robot.keyPress(KeyEvent.VK_SHIFT)
            }
            if (keyEvent.modifiers.and(KeyEvent.ALT_MASK) != 0) {
                robot.keyPress(KeyEvent.VK_ALT)
            }

            robot.keyPress(keyEvent.keyCode)
            robot.delay(50)
            robot.keyRelease(keyEvent.keyCode)

            if (keyEvent.modifiers.and(KeyEvent.ALT_MASK) != 0) {
                robot.keyRelease(KeyEvent.VK_ALT)
            }
            if (keyEvent.modifiers.and(KeyEvent.SHIFT_MASK) != 0) {
                robot.keyRelease(KeyEvent.VK_SHIFT)
            }
            if (keyEvent.modifiers.and(KeyEvent.CTRL_MASK) != 0) {
                robot.keyRelease(KeyEvent.VK_CONTROL)
            }
        } catch (e: Exception) {
            logger.warning("Failed to execute key stroke ${keyEvent.readableString()}")
            e.printStackTrace()
        } catch (e: Error) {
            logger.warning("Failed to execute key stroke ${keyEvent.readableString()}")
            e.printStackTrace()
        }
    }

    private fun keyPress(keyEvent1: KeyStroke, keyCode2: Int) {
        logger.info("Performing keyPress for keyEvent: ${keyEvent1.readableString()}")
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
            robot.keyPress(keyCode2)
            robot.delay(50)
            robot.keyRelease(keyCode2)
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
        } catch (e: Exception) {
            logger.warning("Failed to execute key stroke ${keyEvent1.readableString()}")
            e.printStackTrace()
        } catch (e: Error) {
            logger.warning("Failed to execute key stroke ${keyEvent1.readableString()}")
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