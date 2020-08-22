package nl.sajansen.hardwarecockpitclient.hardware


import com.fazecast.jSerialComm.SerialPort
import nl.sajansen.hardwarecockpitclient.hardware.components.Button
import nl.sajansen.hardwarecockpitclient.hardware.components.Component
import nl.sajansen.hardwarecockpitclient.hardware.components.Slider
import nl.sajansen.hardwarecockpitclient.hardware.components.Switch
import nl.sajansen.hardwarecockpitclient.hardware.serial.SerialListener
import nl.sajansen.hardwarecockpitclient.hardware.serial.SerialOperationMode
import nl.sajansen.hardwarecockpitclient.utils.NumberMap
import nl.sajansen.hardwarecockpitclient.utils.NumberMapMode
import java.util.logging.Logger

@Suppress("MemberVisibilityCanBePrivate")
object CockpitDevice : HardwareDevice {

    private val logger = Logger.getLogger(CockpitDevice::class.java.name)

    const val NAME_BUTTON_ATC = "ATC"
    const val NAME_BUTTON_1 = "1"
    const val NAME_BUTTON_LAND = "LAND"
    const val NAME_SLIDER_FLAPS = "FLAPS"
    const val NAME_SLIDER_SPOILER = "SPOILER"

    override val components = listOf<Component>(
        Button(150, NAME_BUTTON_ATC),
        Button(151, NAME_BUTTON_1),
        Switch(3, NAME_BUTTON_LAND),
        Slider(100, NAME_SLIDER_FLAPS),
        Slider(104, NAME_SLIDER_SPOILER, NumberMap(0, 16383, 1, 9, mode = NumberMapMode.LINEAR))
    )

    override var operationMode: SerialOperationMode = SerialOperationMode.OPERATION_MODE_SIMULATOR
    private var comPort: SerialPort? = null
    override fun getComPort() = comPort

    override fun connect(deviceName: String, baudRate: Int): Boolean {
        comPort = SerialPort.getCommPorts().find { it.descriptivePortName == deviceName }
        if (comPort == null) {
            logger.severe("Serial device '$deviceName' not found")
            return false
        }

        comPort!!.baudRate = baudRate
        val connected = comPort!!.openPort()

        if (!connected) {
            logger.severe("Could not connect to hardware device '$deviceName'")
            return false
        }

        logger.info("Connected to hardware device '$deviceName'")
        clearComPort()
        comPort!!.addDataListener(SerialListener(this))

        return true
    }

    override fun disconnect() {
        comPort?.closePort()
        logger.info("Hardware device disconnected")
    }

    private fun clearComPort() {
        logger.info("Clearing com port buffer")
        while (comPort!!.bytesAvailable() > 0) {
            val byteBuffer = ByteArray(comPort!!.bytesAvailable())
            comPort?.readBytes(byteBuffer, byteBuffer.size.toLong())
        }
    }
}