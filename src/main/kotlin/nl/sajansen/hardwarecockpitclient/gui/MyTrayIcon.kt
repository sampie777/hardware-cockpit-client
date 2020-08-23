package nl.sajansen.hardwarecockpitclient.gui


import com.fazecast.jSerialComm.SerialPort
import nl.sajansen.hardwarecockpitclient.ApplicationInfo
import nl.sajansen.hardwarecockpitclient.config.Config
import nl.sajansen.hardwarecockpitclient.connectWithHardware
import nl.sajansen.hardwarecockpitclient.connectors.ConnectorRegister
import nl.sajansen.hardwarecockpitclient.connectors.JoystickConnector
import nl.sajansen.hardwarecockpitclient.connectors.KeyboardConnector
import nl.sajansen.hardwarecockpitclient.loadConnectors
import java.awt.*
import java.util.logging.Logger
import javax.swing.JOptionPane
import kotlin.system.exitProcess


class MyTrayIcon {
    private val logger = Logger.getLogger(MyTrayIcon::class.java.name)

    private var trayIcon: TrayIcon? = null
    private val serialDeviceMenu = Menu("Serial device")

    fun show() {
        //Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            logger.severe("SystemTray is not supported")
            return
        }

        // Create a pop-up menu components
        val startItem = MenuItem("Start").also {
            it.addActionListener { _ ->
                it.isEnabled = false
                it.name = "Running..."
                trayIcon?.toolTip = "${ApplicationInfo.name}: running"
                loadConnectors()
                connectWithHardware()
            }
        }
        val keyboardSettingItem = CheckboxMenuItem("Keyboard").also { it ->
            it.state = Config.keyboardConnectorEnabled
            it.addItemListener { _ ->
                logger.info("Changing keyboardConnectorEnabled to: ${it.state}")
                Config.keyboardConnectorEnabled = it.state
                Config.save()

                ConnectorRegister.connectors
                    .filterIsInstance<KeyboardConnector>()
                    .toTypedArray()
                    .forEach { connector -> connector.disable() }

                if (it.state) {
                    KeyboardConnector().enable()
                }
            }
        }
        val joystickSettingItem = CheckboxMenuItem("Joystick").also {
            it.state = Config.joystickConnectorEnabled
            it.addItemListener { _ ->
                logger.info("Changing joystickConnectorEnabled to: ${it.state}")
                Config.joystickConnectorEnabled = it.state
                Config.save()

                ConnectorRegister.connectors
                    .filterIsInstance<JoystickConnector>()
                    .toTypedArray()
                    .forEach { connector -> connector.disable() }

                if (it.state) {
                    JoystickConnector().enable()
                }
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

        serialDeviceMenu.addActionListener { logger.info("Hey, I've got an action!") }
        updateSerialDeviceMenu()

        val settingsMenu = Menu("Settings")
        settingsMenu.add(serialDeviceMenu)
        settingsMenu.addSeparator()
        settingsMenu.add(keyboardSettingItem)
        settingsMenu.add(joystickSettingItem)

        val popup = PopupMenu()
        popup.add(startItem)
        popup.add(settingsMenu)
        popup.add(infoItem)
        popup.addSeparator()
        popup.add(exitItem)

        val image = try {
            val resource =
                MyTrayIcon::class.java.classLoader.getResource("nl/sajansen/hardwarecockpitclient/icon-16.png")
            Toolkit.getDefaultToolkit().getImage(resource)
        } catch (e: Exception) {
            logger.warning("Failed to load tray icon image")
            e.printStackTrace()
            null
        }

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
    }

    private fun updateSerialDeviceMenu() {
        serialDeviceMenu.removeAll()

        SerialPort.getCommPorts().forEach { port ->
            val deviceItem = CheckboxMenuItem("[${port.systemPortName}]   ${port.descriptivePortName}")
            deviceItem.state = Config.hardwareDeviceComName == port.descriptivePortName
            deviceItem.addItemListener {
                logger.info("Setting new hardware device to: ${port.descriptivePortName}")
                Config.hardwareDeviceComName = port.descriptivePortName
                Config.save()
                updateSerialDeviceMenu()
            }

            serialDeviceMenu.add(deviceItem)
        }
    }
}