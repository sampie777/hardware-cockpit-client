package nl.sajansen.hardwarecockpitclient

import nl.sajansen.hardwarecockpitclient.connectors.KeyboardConnector
import nl.sajansen.hardwarecockpitclient.hardware.HardwareDevice


const val DEVICE_COM_NAME = "USB-to-Serial Port (ch341-uart)"

fun main(args: Array<String>) {
    KeyboardConnector().enable()

    HardwareDevice.connect(DEVICE_COM_NAME)
    Thread.sleep(2000)
    HardwareDevice.disconnect()
}
