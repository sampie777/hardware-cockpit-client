package nl.sajansen.hardwarecockpitclient.hardware.serial


import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import nl.sajansen.hardwarecockpitclient.config.Config
import nl.sajansen.hardwarecockpitclient.hardware.CockpitDeviceFeedbackData
import nl.sajansen.hardwarecockpitclient.hardware.HardwareDevice
import nl.sajansen.hardwarecockpitclient.hardware.components.Component
import java.lang.Integer.min
import java.nio.ByteBuffer
import java.util.logging.Logger

@ExperimentalUnsignedTypes
class SerialListener(private val hardwareDevice: HardwareDevice) : SerialPortDataListener {
    private val logger = Logger.getLogger(SerialListener::class.java.name)

    var receivedData = arrayListOf<Byte>()
    var metaByte: Int? = null
    var dataLength: Int = 0

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
        logger.fine("Processing received serial data: ${receivedData.map { Integer.toHexString(it.toInt()) }}")

        if (metaByte == null) {
            if (!validateMetaForData(receivedData)) {
                return false
            } else {
                metaByte = receivedData[0].toInt()
                dataLength = getDataLengthForMetaByte(metaByte!!)
            }
        }

        val updateType = getUpdateTypeForData() ?: return false
        if (updateType == SerialDataType.UPDATE_REQUEST) {
            sendSerialDeviceUpdate()
            clearSerialBufferForCurrentMessage()
            return true
        }

        val component = getComponentForData(receivedData)
        val value = getValueForData(receivedData) ?: return true

        clearSerialBufferForCurrentMessage()

        if (component == null) {
            return true
        }

        setComponentWithData(component, value)
        return true
    }

    fun validateMetaForData(receivedData: ArrayList<Byte>): Boolean {
        val metaByte = receivedData[0].toInt()

        val metaBits = metaByte.shr(5).and(0x07)

        if (metaBits != Config.serialMetaBitsValue) {
            logger.warning("Meta data '$metaBits' is invalid or this isn't the meta data byte")
            return false
        }
        return true
    }

    fun getDataLengthForMetaByte(metaByte: Int) = metaByte.and(0x03)

    fun getUpdateTypeForData(): SerialDataType? {
        val updateTypeInt = metaByte!!.shr(2).and(0x07)

        return try {
            SerialDataType.fromInt(updateTypeInt)
        } catch (e: Exception) {
            logger.warning("Update type $updateTypeInt is invalid: ${e.localizedMessage}")
            metaByte = null
            null
        }
    }

    fun getComponentForData(receivedData: ArrayList<Byte>): Component? {
        val id = receivedData[1]

        val component = hardwareDevice.components.find {
            it.id.toByte() == id
        }

        if (component == null) {
            logger.fine("Component for id '${id}' not found")
        }
        return component
    }

    fun getValueForData(receivedData: ArrayList<Byte>): Int? {
        if (dataLength == 0) {
            return 1
        }

        if (2 + dataLength > receivedData.size) {
            logger.fine("Received incorrect data size: ${receivedData.size - 2} != $dataLength")
            return null
        }

        // Copy data bytes from array and pad them with zeros until it's a four-sized array which then can be converted to an Int
        val bytes = ArrayList(receivedData.subList(2, 2 + dataLength))
        while (bytes.size < 4) {
            bytes.add(0, 0)
        }

        return ByteBuffer.wrap(bytes.toByteArray()).int
    }

    private fun setComponentWithData(component: Component, value: Int) {
        logger.fine("Calling set() for component: $component")

        try {
            if (Config.asynchronousUpdates) {
                Thread { component.set(value) }.start()
            } else {
                component.set(value)
            }
        } catch (e: Exception) {
            logger.severe("Failed to set value: $value, for component: $component")
            e.printStackTrace()
        }
    }

    @ExperimentalUnsignedTypes
    private fun sendSerialDeviceUpdate() {
        logger.fine("Sending heartbeat to serial device")
        if (hardwareDevice.getComPort() == null) {
            logger.warning("Serial device unconnected, cannot send heartbeat")
            return
        }

        val updateData = CockpitDeviceFeedbackData(
            hardwareDevice.operationMode.id.toUByte(),
            0u,
            0u,
            80u,
            0u,
            1u,
            0,
            16383,
            16383,
            16383,
            3u,
            0u,
            0u,
            0,
            0,
            0u
        )

        val updateBytes = updateData.toByteArray()
        hardwareDevice.getComPort()?.writeBytes(updateBytes, updateBytes.size.toLong())
    }

    private fun clearSerialBufferForCurrentMessage() {
        removeFromData(2 + dataLength)
        metaByte = null
    }

    private fun removeFromData(amount: Int) {
        for (i in 0 until min(amount, receivedData.size)) {
            logger.fine("Cleaning up serial data with amount: $amount")
            receivedData.removeAt(0)
        }
        logger.fine("Serial data left: ${receivedData.size}")
    }

    fun clear() {
        logger.fine("Clearing serial data buffer")
        receivedData.clear()
        metaByte = null
    }
}