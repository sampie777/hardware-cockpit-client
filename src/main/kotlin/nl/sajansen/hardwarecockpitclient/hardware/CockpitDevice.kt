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

    const val NAME_BUTTON_PAUSE = "PAUSE"
    const val NAME_BUTTON_ATC = "ATC"
    const val NAME_BUTTON_1 = "1"
    const val NAME_BUTTON_2 = "2"
    const val NAME_BUTTON_3 = "3"
    const val NAME_BUTTON_4 = "4"
    const val NAME_BUTTON_5 = "5"
    const val NAME_BUTTON_6 = "6"
    const val NAME_BUTTON_7 = "7"
    const val NAME_BUTTON_8 = "8"
    const val NAME_BUTTON_9 = "9"
    const val NAME_BUTTON_A = "A"
    const val NAME_BUTTON_B = "B"
    const val NAME_BUTTON_C = "C"
    const val NAME_BUTTON_D = "D"
    const val NAME_SWITCH_BCN = "BCN"
    const val NAME_SWITCH_LAND = "LAND"
    const val NAME_SWITCH_TAXI = "TAXI"
    const val NAME_SWITCH_NAV = "NAV"
    const val NAME_SWITCH_STROBE = "STROBE"
    const val NAME_SWITCH_CABIN = "CABIN"
    const val NAME_SWITCH_G = "G"
    const val NAME_SWITCH_PARKING_BRAKE = "PARKING BRAKE"
    const val NAME_SWITCH_MASTER = "MASTER"
    const val NAME_SWITCH_LANDING_GEAR = "LANDING GEAR"
    const val NAME_SLIDER_FLAPS = "FLAPS"
    const val NAME_SLIDER_SPOILER = "SPOILER"
    const val NAME_SLIDER_F = "F"
    const val NAME_SLIDER_FEET_PEDAL_LEFT = "FEET PEDAL LEFT"
    const val NAME_SLIDER_FEET_PEDAL_RIGHT = "FEET PEDAL RIGHT"

    override val components = listOf<Component>(
        Button(160, NAME_BUTTON_PAUSE),
        Button(150, NAME_BUTTON_ATC),
        Button(151, NAME_BUTTON_1),
        Button(152, NAME_BUTTON_2),
        Button(153, NAME_BUTTON_3),
        Button(154, NAME_BUTTON_4),
        Button(155, NAME_BUTTON_5),
        Button(156, NAME_BUTTON_6),
        Button(157, NAME_BUTTON_7),
        Button(158, NAME_BUTTON_8),
        Button(159, NAME_BUTTON_9),
        Button(18, NAME_BUTTON_A),
        Button(19, NAME_BUTTON_B),
        Button(20, NAME_BUTTON_C),
        Button(21, NAME_BUTTON_D),

        Switch(2, NAME_SWITCH_BCN),
        Switch(3, NAME_SWITCH_LAND),
        Switch(4, NAME_SWITCH_TAXI),
        Switch(1, NAME_SWITCH_NAV),
        Switch(5, NAME_SWITCH_STROBE),
        Switch(10, NAME_SWITCH_CABIN),
        Switch(23, NAME_SWITCH_G),
        Switch(25, NAME_SWITCH_PARKING_BRAKE),
        Switch(24, NAME_SWITCH_MASTER),
        Switch(26, NAME_SWITCH_LANDING_GEAR),

        Slider(100, NAME_SLIDER_FLAPS),
        Slider(104, NAME_SLIDER_SPOILER, NumberMap(0, 16383, 1, 9, mode = NumberMapMode.LINEAR)),
        Slider(105, NAME_SLIDER_F)
//        Slider(106, NAME_SLIDER_FEET_PEDAL_LEFT),
//        Slider(107, NAME_SLIDER_FEET_PEDAL_RIGHT)
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