package nl.sajansen.hardwarecockpitclient.mocks


import org.flypad.joystick.Joystick
import java.util.logging.Logger

class JoystickMock : Joystick() {
    private val logger = Logger.getLogger(JoystickMock::class.java.name)

    @Volatile
    var isSend: Boolean = false
    var isClosed: Boolean = false

    override fun send() {
        logger.info("Sending Mock Joystick")
        isSend = true
    }

    override fun initializeHandle(name: String) {}

    override fun close() {
        logger.info("Closing Mock Joystick")
        isClosed = true
    }
}