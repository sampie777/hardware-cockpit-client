package nl.sajansen.hardwarecockpitclient

import com.fazecast.jSerialComm.SerialPort
import nl.sajansen.hardwarecockpitclient.config.Config
import nl.sajansen.hardwarecockpitclient.connectors.ConnectorRegister
import nl.sajansen.hardwarecockpitclient.connectors.JoystickConnector
import nl.sajansen.hardwarecockpitclient.connectors.KeyboardConnector
import nl.sajansen.hardwarecockpitclient.gui.MyTrayIcon
import nl.sajansen.hardwarecockpitclient.hardware.CockpitDevice
import nl.sajansen.hardwarecockpitclient.utils.getCurrentJarDirectory
import java.awt.EventQueue
import java.util.logging.Logger

fun main(args: Array<String>) {
    if (args.contains("--help")) {
        println("""
            Usage: 
               --list-devices   Show all serial devices
               --help           Show this message
               --gui            Start application with a GUI (system tray icon)
               
               Connectors:
               --joystick       Enable joystick connector
               --keyboard       Enable keyboard connector
        """.trimIndent())
        return
    }

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

    if (args.contains("--gui")) {
        EventQueue.invokeLater {
            MyTrayIcon().show()
        }
        return
    }

    val connection = connectWithHardware()

    @Suppress("ControlFlowWithEmptyBody")
    while (connection) {
    }

    CockpitDevice.disconnect()
}

fun loadConnectors(args: Array<String> = emptyArray()) {
    ConnectorRegister.disableAll()

    if (args.contains("--keyboard") || Config.keyboardConnectorEnabled) {
        KeyboardConnector().enable()
    }

    if (args.contains("--joystick") || Config.joystickConnectorEnabled) {
        JoystickConnector().enable()
    }
}

fun connectWithHardware(): Boolean {
    return CockpitDevice.connect(Config.hardwareDeviceComName, Config.hardwareDeviceComBaudRate)
}

fun listSerialPorts() {
    SerialPort.getCommPorts().forEach {
        println("- ${it.descriptivePortName} \t[${it.systemPortName}]")
    }
}

fun attachExitCatcher() {
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            exitApplication()
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

fun exitApplication() {
    println("Exiting application...")

    CockpitDevice.disconnect()
    ConnectorRegister.disableAll()

    println("Shutdown finished")
}