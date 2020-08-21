package nl.sajansen.hardwarecockpitclient

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import java.util.logging.Logger


const val DEVICE_COM_NAME = "USB-to-Serial Port (ch341-uart)"
const val SERIAL_DATA_DELIMITER = '\n'.toByte()

fun main(args: Array<String>) {

    val comPort = SerialPort.getCommPorts().find { it.descriptivePortName == DEVICE_COM_NAME }
    if (comPort == null) {
        println("[WARNING] Serial device '$DEVICE_COM_NAME' not found")
        return
    }

    comPort.addDataListener(MySerialPortDataListener())

    comPort.openPort()

    println("Listening...")
    Thread.sleep(8000)

    comPort.closePort()
}

class MySerialPortDataListener : SerialPortDataListener {

    private val logger = Logger.getLogger(javaClass.canonicalName)

    private var receivedData: String = ""

    override fun getListeningEvents(): Int {
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED
    }

    override fun serialEvent(event: SerialPortEvent) {
        if (event.eventType != SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
            logger.warning("Got event type: ${event.eventType}")
            return
        }

        event.receivedData.forEach {
            receivedData += it.toChar()

            if (it == SERIAL_DATA_DELIMITER) {
                logger.info(receivedData)
                receivedData = ""
            }
        }
    }
}