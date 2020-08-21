package nl.sajansen.hardwarecockpitclient.connectors

import java.util.logging.Logger

object ConnectorRegister {
    private val logger = Logger.getLogger(ConnectorRegister::class.java.name)

    private val connectors = hashSetOf<Connector>()

    fun register(connector: Connector) {
        logger.info("Registering connector: ${connector.javaClass.name}")
        connectors.add(connector)
    }

    fun unregister(connector: Connector) {
        logger.info("Unregistering connector: ${connector.javaClass.name}")
        connectors.remove(connector)
    }

    fun unregisterAll() {
        logger.info("Unregistering all connectors")
        connectors.clear()
    }

    fun valueUpdate(name: String, value: Any?) {
        logger.info("Sending value update to connectors")
        try {
            connectors
                .toTypedArray()
                .forEach { it.valueUpdate(name, value) }
        } catch (e: Exception) {
            logger.severe("Failed to update value: $value, along connectors for name: $name")
            e.printStackTrace()
        }
    }
}