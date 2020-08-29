package nl.sajansen.hardwarecockpitclient

import com.fazecast.jSerialComm.SerialPort
import nl.sajansen.hardwarecockpitclient.config.Config
import nl.sajansen.hardwarecockpitclient.connectors.ConnectorRegister
import nl.sajansen.hardwarecockpitclient.connectors.HardwareDeviceEmulatorConnector
import nl.sajansen.hardwarecockpitclient.connectors.JoystickConnector
import nl.sajansen.hardwarecockpitclient.connectors.KeyboardConnector
import nl.sajansen.hardwarecockpitclient.gui.MyTrayIcon
import nl.sajansen.hardwarecockpitclient.gui.emulator.HardwareEmulatorFrame
import nl.sajansen.hardwarecockpitclient.hardware.CockpitDevice
import nl.sajansen.hardwarecockpitclient.utils.getCurrentJarDirectory
import java.awt.EventQueue
import java.util.logging.Logger

val logger: Logger = Logger.getLogger("Application")

fun main(args: Array<String>) {
    if (args.contains("--help")) {
        println("""
            Usage: 
               --list-devices   Show all serial devices
               --help           Show this message
               --headless       Start application without a GUI (system tray icon)
               --disconnect     Don't connect with the hardware
               -v               Verbose logging
               --disable-logging Disable logging completely
               
               Connectors:
               --joystick       Enable joystick connector
               --keyboard       Enable keyboard connector
               --emulator       Enable emulator connector. Also shows emulator window.
        """.trimIndent())
        return
    }

    if (args.contains("--list-devices")) {
        listSerialPorts()
        return
    }

    attachExitCatcher()

    logger.info("Executing JAR directory: " + getCurrentJarDirectory(Config).absolutePath)

    Config.enableWriteToFile(true)
    Config.load()
    setupLogging(args)
    Config.hardwareDeviceEmulatorConnectorEnabled = false   // Don't start with it as true
    Config.save()

    if (args.contains("--emulator")) {
        EventQueue.invokeLater {
            HardwareEmulatorFrame.createAndShow()
        }
    }

    if (!args.contains("--headless")) {
        EventQueue.invokeLater {
            MyTrayIcon().show()
        }
        return
    }

    loadConnectors(args)

    val connection = connectWithHardware(args)

    @Suppress("ControlFlowWithEmptyBody")
    while (connection) {
    }

    CockpitDevice.disconnect()
}

fun loadConnectors(args: Array<String> = emptyArray()) {
    ConnectorRegister.disableAll()

    if (args.contains("--emulator") || Config.hardwareDeviceEmulatorConnectorEnabled) {
        HardwareDeviceEmulatorConnector().enable()
    }

    if (args.contains("--keyboard") || Config.keyboardConnectorEnabled) {
        KeyboardConnector().enable()
    }

    if (args.contains("--joystick") || Config.joystickConnectorEnabled) {
        JoystickConnector().enable()
    }
}

fun connectWithHardware(args: Array<String> = emptyArray()): Boolean {
    if (!Config.hardwareDeviceConnect || args.contains("--disconnect")) {
        logger.info("Hardware connection is turned off")
        return true
    }

    return CockpitDevice.connect(Config.hardwareDeviceComName, Config.hardwareDeviceComBaudRate)
}

fun listSerialPorts() {
    SerialPort.getCommPorts().forEach {
        println("- ${it.descriptivePortName} \t[${it.systemPortName}]")
        logger.info("- ${it.descriptivePortName} \t[${it.systemPortName}]")
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
    try {
        LogService.setup(args)
    } catch (e: Exception) {
        logger.severe("Failed to initiate logging: $e")
        e.printStackTrace()
    }
}

fun exitApplication() {
    logger.info("Exiting application...")

    CockpitDevice.disconnect()
    ConnectorRegister.disableAll()

    logger.info("Shutdown finished")
}