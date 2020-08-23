package nl.sajansen.hardwarecockpitclient.connectors

import java.util.logging.Logger

object ConnectorRegister {
    private val logger = Logger.getLogger(ConnectorRegister::class.java.name)

    val connectors = hashSetOf<Connector>()

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

    fun disableAll() {
        logger.info("Disabling all connectors")

        connectors
            .toTypedArray()
            .forEach {
                try {
                    it.disable()
                } catch (e: Exception) {
                    logger.severe("Failed to disable connector: ${it.javaClass.name}")
                    e.printStackTrace()
                }
            }
    }

    fun valueUpdate(name: String, value: Any) {
        logger.info("Sending value update to connectors")
        try {
            connectors
                .toTypedArray()
                .forEach {
                    try {
                        it.valueUpdate(name, value)
                    } catch (e: Exception) {
                        logger.severe("Failed to update value: $value, name: $name, for connector: ${it.javaClass.name}")
                        e.printStackTrace()
                    }
                }
        } catch (e: Exception) {
            logger.severe("Failed to update value: $value, along connectors for name: $name")
            e.printStackTrace()
        }
    }
}