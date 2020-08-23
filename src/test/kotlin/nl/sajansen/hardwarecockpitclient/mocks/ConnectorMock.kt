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

class ConnectorMockWithDelay : Connector {
    private val logger = Logger.getLogger(ConnectorMock::class.java.name)

    @Volatile
    var valueUpdated = false
    @Volatile
    var valueUpdatedWithKey = ""
    @Volatile
    var valueUpdatedWithValues: ArrayList<Any?> = arrayListOf()

    override fun valueUpdate(name: String, value: Any) {
        logger.info("Receiving value: $value, from name: $name")

        valueUpdated = true
        valueUpdatedWithKey = name
        valueUpdatedWithValues.add(value)
        logger.info("Added value: $value, to arraylist")

        Thread.sleep(20)
    }
}