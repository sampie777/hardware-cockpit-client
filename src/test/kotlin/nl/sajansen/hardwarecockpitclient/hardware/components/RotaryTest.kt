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
        Thread.sleep(50) // Wait for ConnecterMock2 to complete update task of 20 ms

        assertTrue(testConnector.valueUpdated)
        assertEquals(2, testConnector.valueUpdatedWithValues.size)
        assertEquals(10, testConnector.valueUpdatedWithValues[0])
        assertEquals(11 - 3, testConnector.valueUpdatedWithValues[1])
        assertEquals(component.name, testConnector.valueUpdatedWithKey)
    }
}