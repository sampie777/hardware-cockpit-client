
## Requirements

### Java 8

### PPJoy

https://github.com/elitak/PPJoy/releases.
Install for the virtual joystick driver. Create a joystick "PPJoy Virtual Joystick 1" using the configuration:
- Parallel port: Virtual Joysticks
- Controller number: Controller 2

Edit its mappings to 8 axes, 24 buttons, and 0 POVs.

## Config

- hardwareDeviceComName: Execute jar with `--list-devices` option to get a list of available serial devices. Copy the name of the preferred device (excluding the port name between brackets). Insert this in the properties file (generated when jar gets executed without parameters).
- serialMetaBitsValue: Meta code for the device communication

`HardwareDevice` contains the virtual copy of the hardware device.

`Connector`s are output variations. `KeyboardConnector` will execute keystrokes for inputs as specified in its code.

Signal flow:
```
Hardware  --USB-->  SerialListener (-> HardwareDevice) -> Component -> ConnectorRegister -> Connectors
```

## Run

### Parameters

- `--list-devices`
- `--help`
- `--virtual-joystick`
- `--keyboard`