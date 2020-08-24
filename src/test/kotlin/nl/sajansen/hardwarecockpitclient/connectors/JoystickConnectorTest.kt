package nl.sajansen.hardwarecockpitclient.connectors

import nl.sajansen.hardwarecockpitclient.config.Config
import nl.sajansen.hardwarecockpitclient.hardware.CockpitDevice
import nl.sajansen.hardwarecockpitclient.mocks.JoystickMock
import org.flypad.joystick.Joystick
import kotlin.test.*


class JoystickConnectorTest {

    @BeforeTest
    fun before() {
        Config.joystickConnectorMaxTrim = 100
        Config.joystickConnectorButtonToggleDuration = 15
    }

    @Test
    fun testJoystickClosesOnConnectorDisable() {
        val joystick = JoystickMock()
        val joystick2 = JoystickMock()
        val connector = JoystickConnector()
        connector.joystick1 = joystick
        connector.joystick2 = joystick2

        // When
        connector.disable()

        assertTrue(joystick.isClosed)
        assertTrue(joystick2.isClosed)
    }

    @Test
    fun testToggleButtonGetsCleared() {
        val joystick = JoystickMock()
        val connector = JoystickConnector()
        connector.joystick1 = joystick

        // When
        connector.toggleButton(joystick, 0, duration = 20)

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
        connector.joystick1 = joystick

        // When
        connector.toggleButtons(joystick, 0, 1, true, duration = 50)

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
        val timerTask = ClearButtonStateTimerTask(joystick, 0)
        joystick.digital[0] = Joystick.DIGITAL_ON

        // When
        timerTask.run()

        assertEquals(Joystick.DIGITAL_OFF, joystick.digital[0])
        assertTrue(joystick.isSend)
    }

    @Test
    fun testTrim() {
        val joystick = JoystickMock()
        val connector = JoystickConnector()
        connector.joystick1 = JoystickMock()
        connector.joystick2 = joystick
        assertEquals(16384, joystick.analog[Joystick.ANALOG_ROTATION_X])

        // When
        connector.valueUpdate(CockpitDevice.NAME_ROTARY_TRIM_ELEVATOR, 0)

        assertTrue(joystick.isSend)
        assertEquals(0, joystick.digital[8])   // Test the non-async waiting for releasing the button
        assertEquals(0, connector.elevatorTrimPosition)
        assertEquals(16384, joystick.analog[Joystick.ANALOG_ROTATION_X])

        // When
        joystick.isSend = false
        connector.valueUpdate(CockpitDevice.NAME_ROTARY_TRIM_ELEVATOR, 1)

        assertTrue(joystick.isSend)
        assertEquals(1, connector.elevatorTrimPosition)
        assertEquals(16547, joystick.analog[Joystick.ANALOG_ROTATION_X])

        // When
        joystick.isSend = false
        connector.valueUpdate(CockpitDevice.NAME_ROTARY_TRIM_ELEVATOR, 10)

        assertTrue(joystick.isSend)
        assertEquals(11, connector.elevatorTrimPosition)
        assertEquals(18186, joystick.analog[Joystick.ANALOG_ROTATION_X])

        // When
        joystick.isSend = false
        connector.valueUpdate(CockpitDevice.NAME_ROTARY_TRIM_ELEVATOR, -15)

        assertTrue(joystick.isSend)
        assertEquals(-4, connector.elevatorTrimPosition)
        assertEquals(15728, joystick.analog[Joystick.ANALOG_ROTATION_X])
    }
}