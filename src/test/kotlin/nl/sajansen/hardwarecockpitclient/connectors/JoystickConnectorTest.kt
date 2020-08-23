package nl.sajansen.hardwarecockpitclient.connectors

import nl.sajansen.hardwarecockpitclient.mocks.JoystickMock
import org.flypad.joystick.Joystick
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class JoystickConnectorTest {

    @Test
    fun testJoystickClosesOnConnectorDisable() {
        val joystick = JoystickMock()
        val connector = JoystickConnector()
        connector.joystick = joystick

        // When
        connector.disable()

        assertTrue(joystick.isClosed)
    }

    @Test
    fun testConnectorFlushSendsJoystickData() {
        val joystick = JoystickMock()
        val connector = JoystickConnector()
        connector.joystick = joystick

        // When
        connector.flush()

        assertTrue(joystick.isSend)
        assertFalse(joystick.isClosed)
    }

    @Test
    fun testToggleButtonGetsCleared() {
        val joystick = JoystickMock()
        val connector = JoystickConnector()
        connector.joystick = joystick

        // When
        connector.toggleButton(0, duration = 20)

        assertFalse(joystick.isSend)
        assertEquals(Joystick.DIGITAL_ON, joystick.digital[0])

        // When wait
        val startTime = System.currentTimeMillis()
        while (!joystick.isSend && (System.currentTimeMillis() - 1000) < startTime) {}

        assertEquals(Joystick.DIGITAL_OFF, joystick.digital[0])
        assertTrue(joystick.isSend)
    }

    @Test
    fun testToggleButtonsGetsCleared() {
        val joystick = JoystickMock()
        val connector = JoystickConnector()
        connector.joystick = joystick

        // When
        connector.toggleButtons(0, 1, true, duration = 50)

        assertFalse(joystick.isSend)
        assertEquals(Joystick.DIGITAL_ON, joystick.digital[0])
        assertEquals(Joystick.DIGITAL_OFF, joystick.digital[1])

        // When wait
        val startTime = System.currentTimeMillis()
        while (!joystick.isSend && (System.currentTimeMillis() - 1000) < startTime) {}

        assertEquals(Joystick.DIGITAL_OFF, joystick.digital[0])
        assertEquals(Joystick.DIGITAL_OFF, joystick.digital[1])
        assertTrue(joystick.isSend)
    }

    @Test
    fun testClearButtonStateTimerTaskClearsButton() {
        val joystick = JoystickMock()
        val connector = JoystickConnector()
        connector.joystick = joystick
        val timerTask = ClearButtonStateTimerTask(connector, 0)
        joystick.digital[0] = Joystick.DIGITAL_ON

        // When
        timerTask.run()

        assertEquals(Joystick.DIGITAL_OFF, joystick.digital[0])
        assertTrue(joystick.isSend)
    }

    @Test
    fun testClearButtonStateTimerTaskCancelsWhenJoystickIsNull() {
        val joystick = JoystickMock()
        val connector = JoystickConnector()
        connector.joystick = joystick
        val timerTask = ClearButtonStateTimerTask(connector, 0)
        joystick.digital[0] = Joystick.DIGITAL_ON

        // When
        connector.joystick = null
        timerTask.run()

        assertEquals(Joystick.DIGITAL_ON, joystick.digital[0])
        assertFalse(joystick.isSend)
    }
}