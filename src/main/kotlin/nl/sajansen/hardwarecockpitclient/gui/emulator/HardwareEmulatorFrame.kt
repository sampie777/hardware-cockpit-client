package nl.sajansen.hardwarecockpitclient.gui.emulator


import nl.sajansen.hardwarecockpitclient.ApplicationInfo
import nl.sajansen.hardwarecockpitclient.gui.loadImage
import java.awt.Dimension
import java.util.logging.Logger
import javax.swing.JFrame

class HardwareEmulatorFrame : JFrame() {
    private val logger = Logger.getLogger(HardwareEmulatorFrame::class.java.name)

    companion object {
        private var instance: HardwareEmulatorFrame? = null
        fun getInstance() = instance

        fun create(): HardwareEmulatorFrame = HardwareEmulatorFrame()

        fun createAndShow(): HardwareEmulatorFrame {
            val frame = create()
            frame.isVisible = true
            return frame
        }
    }

    init {
        instance = this
        logger.info("Creating HardwareEmulatorFrame")

        initGUI()
    }

    private fun initGUI() {
        add(HardwareEmulatorPanel())

        size = Dimension(1500, 643)
        title = "Emulator - ${ApplicationInfo.name}"
        defaultCloseOperation = EXIT_ON_CLOSE
        iconImage = loadImage("/nl/sajansen/hardwarecockpitclient/icon-512.png")
    }
}