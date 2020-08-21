package nl.sajansen.hardwarecockpitclient.hardware


import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import nl.sajansen.hardwarecockpitclient.hardware.components.Component
import java.util.logging.Logger

class SerialListener(private val hardwareDevice: HardwareDevice) : SerialPortDataListener {
    private val logger = Logger.getLogger(SerialListener::class.java.name)

    private val SERIAL_DATA_DELIMITER = '\n'.toByte()

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

            if (it != SERIAL_DATA_DELIMITER) {
                return@forEach
            }

            processData(receivedData)
            receivedData.clear()
        }
    }

    private fun processData(receivedData: ArrayList<Byte>) {
        logger.info("Processing received serial data: ${receivedData.map { it.toChar() }}")

        if (!validateMetaForData(receivedData)) {
            return
        }

        val component = getComponentForData(receivedData) ?: return

        val value = getValueForData(receivedData) ?: return

        logger.info("Calling set() for component: $component")

        try {
            component.set(value)
        } catch (e: Exception) {
            logger.severe("Failed to set value: $value, for component: $component")
            e.printStackTrace()
        }
    }

    private fun validateMetaForData(receivedData: java.util.ArrayList<Byte>): Boolean {
        // todo
        return true
    }

    private fun getComponentForData(receivedData: ArrayList<Byte>): Component? {
        val id = receivedData[1].toInt()
        // todo

        val component = hardwareDevice.components.find {
            it.id == id
        }
        if (component == null) {
            logger.warning("Component for id '$id' not found")
        }
        return component
    }

    private fun getValueForData(receivedData: ArrayList<Byte>): ByteArray? {
        // todo
        return receivedData.toByteArray()
    }
}