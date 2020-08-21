package nl.sajansen.hardwarecockpitclient.mocks


import com.fazecast.jSerialComm.SerialPort
import nl.sajansen.hardwarecockpitclient.hardware.HardwareDevice
import nl.sajansen.hardwarecockpitclient.hardware.components.Button
import nl.sajansen.hardwarecockpitclient.hardware.components.Component
import java.util.logging.Logger

class TestHardwareDevice : HardwareDevice {
    private val logger = Logger.getLogger(TestHardwareDevice::class.java.name)

    val NAME_BUTTON_1 = "BTN1"
    val NAME_BUTTON_2 = "BTN2"

    override val components: List<Component> = listOf(
        Button(1, NAME_BUTTON_1),
        Button(2, NAME_BUTTON_2)
    )

    override fun getComPort(): SerialPort? = null

    override fun connect(deviceName: String, baudRate: Int): Boolean {
        return true
    }
}