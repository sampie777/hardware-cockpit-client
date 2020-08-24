package nl.sajansen.hardwarecockpitclient.connectors


import nl.sajansen.hardwarecockpitclient.gui.emulator.HardwareEmulatorPanel
import java.util.logging.Logger

class HardwareDeviceEmulatorConnector : Connector {
    private val logger = Logger.getLogger(HardwareDeviceEmulatorConnector::class.java.name)

    override fun valueUpdate(name: String, value: Any) {
        logger.info("New value update")
        if (HardwareEmulatorPanel.getInstance() == null) {
            logger.info("Hardware instance is null")
            return
        }

        val component = HardwareEmulatorPanel.getInstance()!!.components.find {
            it.component.name == name
        }

        if (component == null) {
            logger.info("Could not find comopnent $name")
            return
        }

        component.highlight()
    }
}