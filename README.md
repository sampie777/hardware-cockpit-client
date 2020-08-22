## Config

- hardwareDeviceComName: Execute jar with `--list-devices` option to get a list of available serial devices. Copy the name of the preferred device (excluding the portname between brackets). Insert this in the properties file (generated when jar gets executed without parameters).
- serialMetaBitsValue: Meta code for the device communication

`HardwareDevice` contains the virtual copy of the hardware device.

`Connector`s are output variations. `KeyboardConnector` will execute keystrokes for inputs as specified in its code.

Signal flow:
```
Hardware  --USB-->  SerialListener (-> HardwareDevice) -> Component -> ConnectorRegister -> Connectors
```
