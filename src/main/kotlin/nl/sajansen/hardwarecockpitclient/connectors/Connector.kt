package nl.sajansen.hardwarecockpitclient.connectors

interface Connector {
    fun enable() {
        ConnectorRegister.register(this)
    }

    fun disable() {
        ConnectorRegister.unregister(this)
    }

    fun valueUpdate(name: String, value: Any)
}