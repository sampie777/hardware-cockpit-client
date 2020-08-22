package nl.sajansen.hardwarecockpitclient.mocks


import com.fazecast.jSerialComm.SerialPort
import nl.sajansen.hardwarecockpitclient.hardware.HardwareDevice
import nl.sajansen.hardwarecockpitclient.hardware.components.Button
import nl.sajansen.hardwarecockpitclient.hardware.components.Component
import nl.sajansen.hardwarecockpitclient.hardware.components.Slider
import java.util.logging.Logger

class TestHardwareDevice : HardwareDevice {
    private val logger = Logger.getLogger(TestHardwareDevice::class.java.name)

    val NAME_BUTTON_1 = "BTN1"
    val NAME_BUTTON_2 = "BTN2"
    val NAME_SLIDER_3 = "SLIDER3"

    override val components: List<Component> = listOf(
        Button(1, NAME_BUTTON_1),
        Button(2, NAME_BUTTON_2),
        Slider(3, NAME_SLIDER_3)
    )

    override fun getComPort(): SerialPort? = null

    override fun connect(deviceName: String, baudRate: Int): Boolean {
        return true
    }
}