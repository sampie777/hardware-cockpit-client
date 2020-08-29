package nl.sajansen.hardwarecockpitclient.gui


import com.fazecast.jSerialComm.SerialPort
import nl.sajansen.hardwarecockpitclient.config.Config
import nl.sajansen.hardwarecockpitclient.config.PropertyLoader
import nl.sajansen.hardwarecockpitclient.connectors.ConnectorRegister
import nl.sajansen.hardwarecockpitclient.connectors.JoystickConnector
import nl.sajansen.hardwarecockpitclient.connectors.KeyboardConnector
import java.awt.CheckboxMenuItem
import java.awt.Desktop
import java.awt.Menu
import java.awt.MenuItem
import java.util.logging.Logger
import javax.swing.JOptionPane

class SettingsMenu : Menu("Settings") {
    private val logger = Logger.getLogger(SettingsMenu::class.java.name)

    private val serialDeviceMenu = Menu("Serial device")

    init {
        initGui()
    }

    private fun initGui() {
        val settingsFileItem = MenuItem("Settings folder").also {
            it.addActionListener {
                logger.info("Opening settings file folder")
                Config.save()

                try {
                    Desktop.getDesktop().open(PropertyLoader.getPropertiesFile().parentFile)
                } catch (e: Exception) {
                    logger.info("Failed to open settings folder")
                    e.printStackTrace()
                    JOptionPane.showMessageDialog(
                        null,
                        "Failed to open folder: ${e.localizedMessage}",
                        "Failure",
                        JOptionPane.ERROR_MESSAGE
                    )
                }
            }
        }
        val autoStartItem = CheckboxMenuItem("Connect on start").also {
            it.state = Config.connectOnStartUp
            it.addItemListener { _ ->
                logger.info("Change connect on start setting")
                Config.connectOnStartUp = it.state
                Config.save()
            }
        }
        
        val keyboardSettingItem = CheckboxMenuItem("Keyboard").also {
            it.state = Config.keyboardConnectorEnabled
            it.addItemListener { _ ->
                logger.info("Changing keyboardConnectorEnabled to: ${it.state}")
                Config.keyboardConnectorEnabled = it.state
                Config.save()

                // Live reload, if the hardware is running
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

                // Live reload, if the hardware is running
                ConnectorRegister.connectors
                    .filterIsInstance<JoystickConnector>()
                    .toTypedArray()
                    .forEach { connector -> connector.disable() }

                if (it.state) {
                    JoystickConnector().enable()
                }
            }
        }

        updateSerialDeviceMenu()

        add(settingsFileItem)
        add(serialDeviceMenu)
        add(autoStartItem)
        addSeparator()
        add(keyboardSettingItem)
        add(joystickSettingItem)
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

        serialDeviceMenu.addSeparator()

        CheckboxMenuItem("Enable connection").also {
            it.state = Config.hardwareDeviceConnect
            it.addItemListener { _ ->
                Config.hardwareDeviceConnect = it.state
                Config.save()
            }
            serialDeviceMenu.add(it)
        }
    }
}