package nl.sajansen.hardwarecockpitclient.mocks


import nl.sajansen.hardwarecockpitclient.connectors.Connector
import java.util.logging.Logger

class TestConnector : Connector {
    private val logger = Logger.getLogger(TestConnector::class.java.name)

    var valueUpdated = false
    var valueUpdatedWithKey = ""
    var valueUpdatedWithValue: Any? = null

    override fun valueUpdate(name: String, value: Any?) {
        valueUpdated = true
        valueUpdatedWithKey = name
        valueUpdatedWithValue = value
    }
}