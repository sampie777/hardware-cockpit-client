package nl.sajansen.hardwarecockpitclient

import com.fazecast.jSerialComm.SerialPort
import nl.sajansen.hardwarecockpitclient.config.Config
import nl.sajansen.hardwarecockpitclient.connectors.KeyboardConnector
import nl.sajansen.hardwarecockpitclient.hardware.CockpitDevice
import nl.sajansen.hardwarecockpitclient.utils.getCurrentJarDirectory
import java.util.logging.Logger

fun main(args: Array<String>) {
    if (args.contains("--list-devices")) {
        listSerialPorts()
        return
    }

    attachExitCatcher()

    val logger = Logger.getLogger("Application")
    logger.info("Executing JAR directory: " + getCurrentJarDirectory(Config).absolutePath)

    Config.enableWriteToFile(true)
    Config.load()
    setupLogging(args)
    Config.save()

    KeyboardConnector().enable()

    val connection = CockpitDevice.connect(Config.hardwareDeviceComName, Config.hardwareDeviceComBaudRate)

    @Suppress("ControlFlowWithEmptyBody")
    while (connection) {}

    CockpitDevice.disconnect()
}

fun listSerialPorts() {
    SerialPort.getCommPorts().forEach {
        println("- ${it.descriptivePortName} [${it.systemPortName}]")
    }
}

fun attachExitCatcher() {
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            CockpitDevice.disconnect()
            println("Application will be terminated")
        }
    })
}

private fun setupLogging(args: Array<String>) {
    val logger = Logger.getLogger("Application")
    try {
        LogService.setup(args)
    } catch (e: Exception) {
        logger.severe("Failed to initiate logging: $e")
        e.printStackTrace()
    }
}