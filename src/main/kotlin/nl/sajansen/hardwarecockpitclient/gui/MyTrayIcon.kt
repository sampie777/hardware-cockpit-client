package nl.sajansen.hardwarecockpitclient.gui


import nl.sajansen.hardwarecockpitclient.ApplicationInfo
import nl.sajansen.hardwarecockpitclient.config.Config
import nl.sajansen.hardwarecockpitclient.connectWithHardware
import nl.sajansen.hardwarecockpitclient.connectors.ConnectorRegister
import nl.sajansen.hardwarecockpitclient.connectors.HardwareDeviceEmulatorConnector
import nl.sajansen.hardwarecockpitclient.gui.emulator.HardwareEmulatorFrame
import nl.sajansen.hardwarecockpitclient.loadConnectors
import java.awt.*
import java.util.logging.Logger
import javax.swing.JOptionPane
import kotlin.system.exitProcess


class MyTrayIcon {
    private val logger = Logger.getLogger(MyTrayIcon::class.java.name)

    private var trayIcon: TrayIcon? = null
    private val startItem = MenuItem("Start")

    fun show() {
        //Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            logger.severe("SystemTray is not supported")
            return
        }

        // Create a pop-up menu components
        startItem.also {
            it.addActionListener {
                startConnection()
            }
        }
        val emulatorItem = MenuItem("Emulator").also {
            it.addActionListener { _ ->
                logger.info("Enabling hardwareDeviceEmulatorConnectorEnabled")
                Config.hardwareDeviceEmulatorConnectorEnabled = true

                EventQueue.invokeLater { HardwareEmulatorFrame.createAndShow() }

                // Live reload, if the hardware is running
                ConnectorRegister.connectors
                    .filterIsInstance<HardwareDeviceEmulatorConnector>()
                    .toTypedArray()
                    .forEach { connector -> connector.disable() }

                HardwareDeviceEmulatorConnector().enable()
            }
        }
        val infoItem = MenuItem("Info").also {
            it.addActionListener {
                JOptionPane.showMessageDialog(
                    null,
                    """
                        ${ApplicationInfo.name}
                        
                        Author: ${ApplicationInfo.author}
                        Version: ${ApplicationInfo.version}
                        URL: ${ApplicationInfo.url}
                    """.trimIndent(),
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE
                )
            }
        }
        val exitItem = MenuItem("Exit").also {
            it.addActionListener {
                exitProcess(0)
            }
        }

        val popup = PopupMenu()
        popup.add(startItem)
        popup.add(emulatorItem)
        popup.add(SettingsMenu())
        popup.add(infoItem)
        popup.addSeparator()
        popup.add(exitItem)

        val image = loadImage("/nl/sajansen/hardwarecockpitclient/icon-16.png")

        trayIcon = TrayIcon(image, ApplicationInfo.name, popup)
        trayIcon?.isImageAutoSize = true
        trayIcon?.toolTip = ApplicationInfo.name

        val tray = SystemTray.getSystemTray()
        try {
            tray.add(trayIcon!!)
        } catch (e: Exception) {
            logger.severe("TrayIcon could not be added to system tray")
            e.printStackTrace()
        }

        if (Config.connectOnStartUp) {
            logger.info("Auto connecting (on start up)")
            startConnection()
        }
    }

    private fun startConnection() {
        startItem.isEnabled = false
        startItem.name = "Running..."
        trayIcon?.toolTip = "${ApplicationInfo.name}: running"

        loadConnectors()
        if (connectWithHardware()) {
            return
        }

        JOptionPane.showMessageDialog(
            null,
            "Failed to connect with hardware device",
            "Failed connection",
            JOptionPane.ERROR_MESSAGE
        )
    }
}