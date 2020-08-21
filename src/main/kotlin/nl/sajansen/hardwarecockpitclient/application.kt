package nl.sajansen.hardwarecockpitclient

import com.fazecast.jSerialComm.SerialPort
import nl.sajansen.hardwarecockpitclient.config.Config
import nl.sajansen.hardwarecockpitclient.connectors.KeyboardConnector
import nl.sajansen.hardwarecockpitclient.hardware.CockpitDevice
import nl.sajansen.hardwarecockpitclient.utils.getCurrentJarDirectory
import java.util.logging.Logger


fun main(args: Array<String>) {
    val logger = Logger.getLogger("Application")
    logger.info("Executing JAR directory: " + getCurrentJarDirectory(Config).absolutePath)

    Config.enableWriteToFile(true)
    Config.load()
    Config.save()

    KeyboardConnector().enable()

    CockpitDevice.connect(Config.hardwareDeviceComName, Config.hardwareDeviceComBaudRate)
    Thread.sleep(10000)
    CockpitDevice.disconnect()
}

fun listSerialPorts() {
    SerialPort.getCommPorts().forEach {
        println("- $it")
    }
}