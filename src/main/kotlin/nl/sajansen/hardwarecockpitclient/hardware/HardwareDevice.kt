package nl.sajansen.hardwarecockpitclient.hardware

import com.fazecast.jSerialComm.SerialPort
import nl.sajansen.hardwarecockpitclient.hardware.components.Component
import nl.sajansen.hardwarecockpitclient.hardware.serial.SerialOperationMode

interface HardwareDevice {
    val components: List<Component>

    var operationMode: SerialOperationMode

    fun getComPort(): SerialPort?

    fun connect(deviceName: String, baudRate: Int): Boolean
    fun disconnect() {}

    fun getUpdateData(): ByteArray = ByteArray(0)
}