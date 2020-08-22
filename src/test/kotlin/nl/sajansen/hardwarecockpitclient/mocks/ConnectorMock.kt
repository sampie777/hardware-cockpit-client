package nl.sajansen.hardwarecockpitclient.mocks


import nl.sajansen.hardwarecockpitclient.connectors.Connector
import java.util.logging.Logger

class ConnectorMock : Connector {
    private val logger = Logger.getLogger(ConnectorMock::class.java.name)

    var valueUpdated = false
    var valueUpdatedWithKey = ""
    var valueUpdatedWithValue: Any? = null

    override fun valueUpdate(name: String, value: Any) {
        valueUpdated = true
        valueUpdatedWithKey = name
        valueUpdatedWithValue = value
    }
}