package nl.sajansen.hardwarecockpitclient.hardware.serial


import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import nl.sajansen.hardwarecockpitclient.config.Config
import nl.sajansen.hardwarecockpitclient.hardware.HardwareDevice
import nl.sajansen.hardwarecockpitclient.hardware.components.Component
import java.lang.Integer.min
import java.nio.ByteBuffer
import java.util.logging.Logger

class SerialListener(private val hardwareDevice: HardwareDevice) : SerialPortDataListener {
    private val logger = Logger.getLogger(SerialListener::class.java.name)

    private var receivedData = arrayListOf<Byte>()

    override fun getListeningEvents(): Int {
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED
    }

    override fun serialEvent(event: SerialPortEvent) {
        if (event.eventType != SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
            logger.warning("Got invalid event type: ${event.eventType}")
            return
        }

        event.receivedData.forEach {
            receivedData.add(it)

            if (receivedData.size < 2) {
                return@forEach
            }

            if (processData(receivedData)) {
                return@forEach
            }

            removeFromData(1)
        }
    }

    private fun processData(receivedData: ArrayList<Byte>): Boolean {
        logger.info("Processing received serial data: ${receivedData.map { Integer.toHexString(it.toInt()) }}")

        if (!validateMetaForData(receivedData)) {
            return false
        }

        val updateType = getUpdateTypeForData(receivedData) ?: return false

        if (updateType == SerialDataType.UPDATE_REQUEST) {
            sendSerialDeviceUpdate()
            removeFromData(2)
            return true
        }

        val component = getComponentForData(receivedData)
        val value = getValueForData(receivedData) ?: return true

        if (component == null) {
            return true
        }

        logger.info("Calling set() for component: $component")

        try {
            println("FOUND SOMETHING")
            component.set(value)
        } catch (e: Exception) {
            logger.severe("Failed to set value: $value, for component: $component")
            e.printStackTrace()
        }

        return true
    }

    fun validateMetaForData(receivedData: ArrayList<Byte>): Boolean {
        val metaByte = receivedData[0].toInt()

        val metaBits = metaByte.shr(5).and(0x07)

        if (metaBits != Config.serialMetaBits) {
            logger.warning("Meta data '$metaBits' is invalid or this isn't the meta data byte")
            return false
        }
        return true
    }

    fun getUpdateTypeForData(receivedData: ArrayList<Byte>): SerialDataType? {
        val metaByte = receivedData[0].toInt()
        val updateTypeInt = metaByte.shr(2).and(0x07)

        return try {
            SerialDataType.fromInt(updateTypeInt)
        } catch (e: Exception) {
            logger.warning("Update type $updateTypeInt is invalid: ${e.localizedMessage}")
            return null
        }
    }

    fun getComponentForData(receivedData: ArrayList<Byte>): Component? {
        val id = receivedData[1].toInt()

        val component = hardwareDevice.components.find {
            println(it)
            it.id == id
        }

        if (component == null) {
            logger.warning("Component for id '$id' not found")
        }
        return component
    }

    fun getValueForData(receivedData: ArrayList<Byte>): Int? {
        val metaByte = receivedData[0].toInt()
        val dataLength = metaByte.and(0x03)

        if (dataLength == 0) {
            removeFromData(2 + dataLength)
            return 1
        }

        if (dataLength > receivedData.size - 2) {
            logger.warning("Received incorrect data size: ${receivedData.size - 2} != $dataLength")
            return null
        }

        val bytes = receivedData.subList(2, 2 + dataLength)
        while (bytes.size < 4) {
            bytes.add(0, 0)
        }

        val value = ByteBuffer.wrap(bytes.toByteArray()).int

        removeFromData(2 + dataLength)
        return value
    }

    private fun sendSerialDeviceUpdate() {
        logger.info("Sending heartbeat to serial device")
        if (hardwareDevice.getComPort() == null) {
            logger.info("Serial device unconnected, cannot send heartbeat")
            return
        }

        val updateBytes = ByteArray(26)
        hardwareDevice.getComPort()?.writeBytes(updateBytes, updateBytes.size.toLong())
    }

    private fun removeFromData(amount: Int) {
        for (i in 0 until min(amount, receivedData.size)) {
            logger.info("Cleaning up serial data with amount: $amount")
            receivedData.removeAt(0)
        }
        logger.info("Serial data left: ${receivedData.size}")
    }
}