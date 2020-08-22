package nl.sajansen.hardwarecockpitclient.mocks


import com.fazecast.jSerialComm.SerialPort
import nl.sajansen.hardwarecockpitclient.hardware.HardwareDevice
import nl.sajansen.hardwarecockpitclient.hardware.components.Button
import nl.sajansen.hardwarecockpitclient.hardware.components.Component
import nl.sajansen.hardwarecockpitclient.hardware.components.Slider
import nl.sajansen.hardwarecockpitclient.hardware.components.Switch
import nl.sajansen.hardwarecockpitclient.hardware.serial.SerialOperationMode
import java.util.logging.Logger

class HardwareDeviceMock : HardwareDevice {
    private val logger = Logger.getLogger(HardwareDeviceMock::class.java.name)

    val NAME_BUTTON_1 = "BTN1"
    val NAME_SWITCH_2 = "SWITCH2"
    val NAME_SLIDER_3 = "SLIDER3"
    val NAME_SLIDER_4 = "SLIDER4"
    val NAME_BUTTON_240 = "BTN240"

    override val components: List<Component> = listOf(
        Button(1, NAME_BUTTON_1),
        Switch(2, NAME_SWITCH_2),
        Slider(3, NAME_SLIDER_3),
        Slider(4, NAME_SLIDER_4),
        Button(240, NAME_BUTTON_240)
    )
    override var operationMode = SerialOperationMode.OPERATION_MODE_UNCONNECTED

    var serialPort: SerialPort? = null
    override fun getComPort(): SerialPort? = serialPort

    override fun connect(deviceName: String, baudRate: Int): Boolean {
        return true
    }
}