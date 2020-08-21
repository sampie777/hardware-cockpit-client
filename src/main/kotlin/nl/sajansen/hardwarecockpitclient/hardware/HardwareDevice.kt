package nl.sajansen.hardwarecockpitclient.hardware

import com.fazecast.jSerialComm.SerialPort
import nl.sajansen.hardwarecockpitclient.hardware.components.Component

interface HardwareDevice {
    val components: List<Component>

    fun getComPort(): SerialPort?

    fun connect(deviceName: String, baudRate: Int): Boolean
    fun disconnect() {}
}