package nl.sajansen.hardwarecockpitclient.hardware.components

import nl.sajansen.hardwarecockpitclient.config.Config
import nl.sajansen.hardwarecockpitclient.connectors.ConnectorRegister
import nl.sajansen.hardwarecockpitclient.mocks.ConnectorMockWithDelay
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RotaryTest {

    @BeforeTest
    fun before() {
        ConnectorRegister.disableAll()
        ConnectorRegister.unregisterAll()
        Config.rotaryMinUpdateInterval = 5
    }

    @Test
    fun testRotaryAsynchronousUpdate() {
        val testConnector = ConnectorMockWithDelay()
        testConnector.enable()
        val component = Rotary(0, "component")

        Thread { component.set(10) }.start()
        Thread.sleep(5)
        Thread { component.set(11) }.start()
        Thread.sleep(5)
        component.set(-3)

        // Wait for ConnecterMock2 to complete update task
        val startTime = System.currentTimeMillis()
        while (testConnector.valueUpdatedWithValues.size < 2 && (System.currentTimeMillis() - 1000) < startTime) {
        }

        assertTrue(testConnector.valueUpdated)
        assertEquals(2, testConnector.valueUpdatedWithValues.size)
        assertEquals(10, testConnector.valueUpdatedWithValues[0])
        assertEquals(11 - 3, testConnector.valueUpdatedWithValues[1])
        assertEquals(component.name, testConnector.valueUpdatedWithKey)
    }

    @Test
    fun testRotaryResetsAfterValueUpdate() {
        val component = Rotary(0, "component")

        component.set(1)

        assertEquals(0, component.value())
    }

    @Test
    fun testRotaryConvertsUnsignedToSigned() {
        val component = Rotary(0, "component")

        assertEquals(-1, component.convertUnsignedToSigned(255))
        assertEquals(1, component.convertUnsignedToSigned(1))
    }
}