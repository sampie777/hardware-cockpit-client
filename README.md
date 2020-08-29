
## Requirements

### Java 8

### PPJoy

https://github.com/elitak/PPJoy/releases.
Install for the virtual joystick driver. Create two joysticks "PPJoy Virtual Joystick 1" and "PPJoy Virtual Joystick 2" using the configuration:
- Parallel port: Virtual Joysticks
- Controller number: Controller 1 (for the second one: Controller 2)

Edit their mappings to 8 axes, 32 buttons, and 0 POVs.

Thanks to https://github.com/dumbledore/FlypadJoystick for this integration.

## Config

- hardwareDeviceComName: Execute jar with `--list-devices` option to get a list of available serial devices. Copy the name of the preferred device (excluding the port name between brackets). Insert this in the properties file (generated when jar gets executed without parameters).
- serialMetaBitsValue: Meta code for the device communication
- ignoreWindowsPlatformCheck: Set this to true to force Windows platform specific/dependent functions to execute on other platforms (may cause crashed!)
- hardwareDeviceConnect: Set this to false in order to disable connection with hardware

`HardwareDevice` contains the virtual copy of the hardware device.

`Connector`s are output variations. 
`KeyboardConnector` will execute keystrokes for inputs as specified in its code. 
`JoystickConnector` will send values to PPJoy to act as a joystick. 
`HardwareDeviceEmulatorConnector` is used to connect inputs to the virtual hardware emulator. Also, buttons and switches in the emulator will send a signal to the Component when clicked.

Signal flow:
```
Hardware  --USB-->  SerialListener (-> HardwareDevice) -> Component -> ConnectorRegister -> Connectors
```

## Run

### Parameters

- `--list-devices` Prints a list of all available COM devices
- `--help` Prints a list of available commands
- `--headless` Start the application without the GUI (system tray icon)
- `--disconnect` Disable connection with hardware
- `-v` Verbose logging
- `--disable-logging` Disable logging completely
- `--joystick` Enable joystick controller
- `--keyboard` Enable keyboard controller
- `--emulator` Enable hardware emulator (opens in a new window)
