package nl.sajansen.hardwarecockpitclient.hardware


import com.fazecast.jSerialComm.SerialPort
import nl.sajansen.hardwarecockpitclient.hardware.components.Button
import nl.sajansen.hardwarecockpitclient.hardware.components.Component
import nl.sajansen.hardwarecockpitclient.hardware.components.Slider
import nl.sajansen.hardwarecockpitclient.utils.NumberMap
import java.util.logging.Logger

@Suppress("MemberVisibilityCanBePrivate")
object HardwareDevice {

    private val logger = Logger.getLogger(HardwareDevice::class.java.name)

    const val NAME_BUTTON_ATC = "ATC"
    const val NAME_BUTTON_LAND = "LAND"
    const val NAME_SLIDER_FLAPS = "FLAPS"
    const val NAME_SLIDER_SPOILER = "SPOILER"

    val components = listOf<Component>(
        Button(40, NAME_BUTTON_ATC),
        Button(41, NAME_BUTTON_LAND),
        Slider(80, NAME_SLIDER_FLAPS),
        Slider(83, NAME_SLIDER_SPOILER, NumberMap(0, 5000, 0, 100))
    )

    private var comPort: SerialPort? = null

    fun connect(deviceName: String): Boolean {
        comPort = SerialPort.getCommPorts().find { it.descriptivePortName == deviceName }
        if (comPort == null) {
            logger.severe("Serial device '$deviceName' not found")
            return false
        }

        comPort!!.addDataListener(SerialListener(this))
        val connected = comPort!!.openPort()

        if (connected) {
            logger.info("Connected to hardware device '$deviceName'")
        } else {
            logger.severe("Could not connect to hardware device '$deviceName'")
        }
        return connected
    }

    fun disconnect() {
        comPort?.closePort()
        logger.info("Hardware device disconnected")
    }
}