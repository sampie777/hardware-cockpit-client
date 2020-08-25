package nl.sajansen.hardwarecockpitclient.gui.emulator


import nl.sajansen.hardwarecockpitclient.config.Config
import nl.sajansen.hardwarecockpitclient.gui.createGraphics
import nl.sajansen.hardwarecockpitclient.gui.loadResource
import nl.sajansen.hardwarecockpitclient.hardware.CockpitDevice
import nl.sajansen.hardwarecockpitclient.hardware.components.*
import nl.sajansen.hardwarecockpitclient.hardware.components.Button
import nl.sajansen.hardwarecockpitclient.hardware.components.Component
import java.awt.*
import java.awt.image.BufferedImage
import java.util.*
import java.util.logging.Logger
import javax.imageio.ImageIO

class GuiComponent(val component: Component, val location: Point, val size: Dimension) {
    private val logger = Logger.getLogger(GuiComponent::class.java.name)

    var isHighlighted: Boolean = false
    private var highlightTimer: Timer = Timer()

    fun click() {
        logger.info("Component $this clicked!")

        if (component is Button) {
            component.set(1)
        } else if (component is Switch) {
            component.set(if(component.value() as Boolean) 0 else 1)
        }
    }

    fun highlight() {
        logger.info("Highlighting Component: $this")
        isHighlighted = true
        HardwareEmulatorPanel.getInstance()?.repaint()

        if (component is Switch) {
            return
        }

        try {
            highlightTimer.cancel()
            highlightTimer = Timer()
        } catch (e: Exception) {
            logger.info("Caught exception during timer cancellation")
            e.printStackTrace()
        } finally {
            highlightTimer.schedule(UnHighlightTimerTask(this), Config.hardwareDeviceEmulatorConnectorHighlightDuration)
        }
    }

    fun unHighlight() {
        logger.info("UnHighlighting Component: $this")
        isHighlighted = false
        HardwareEmulatorPanel.getInstance()?.repaint()
    }

    fun getPaintingObject(): BufferedImage? {
        if (component is Switch) {
            if (!(component.value() as Boolean)) {
                return null
            }
        } else if (component is Button) {
            if (!isHighlighted && !component.rawValue()) {
                return null
            }
        } else if (component is Rotary) {
            if (!isHighlighted) {
                return null
            }
        } else if (component.name == CockpitDevice.NAME_SLIDER_FEET_PEDAL_LEFT || component.name == CockpitDevice.NAME_SLIDER_FEET_PEDAL_RIGHT) {
            if (!isHighlighted) {
                return getRudderPainting()
            }
        } else if (component is Slider) {
            if (!isHighlighted) {
                return null
            }
        }

        val (bufferedImageTemp, g2: Graphics2D) = createGraphics(
            size.width,
            size.height,
            BufferedImage.TRANSLUCENT
        )

        g2.stroke = BasicStroke(1F)
        g2.color = Color(255, 255, 0, 100)

        when (component.name) {
            CockpitDevice.NAME_SLIDER_F -> g2.fillOval(0, 0, size.width, size.height)
            else -> when (component) {
                is Rotary -> g2.fillRect(0, 0, size.width, size.height)
                is Slider -> g2.fillRect(0, 0, size.width, size.height)
                else -> g2.fillOval(0, 0, size.width, size.height)
            }
        }

        g2.dispose()
        return bufferedImageTemp
    }

    private fun getRudderPainting(): BufferedImage? {
        val (bufferedImageTemp, g2: Graphics2D) = createGraphics(
            size.width,
            size.height,
            BufferedImage.TRANSLUCENT
        )

        g2.stroke = BasicStroke(1F)
        g2.color = Color(139, 139, 139)

        g2.fillRect(0, 0, size.width, size.height)

        g2.dispose()
        return bufferedImageTemp
    }

    fun getImage(): BufferedImage {
        val uri = "/nl/sajansen/hardwarecockpitclient/gui/emulator/components/" +
                when (component.name) {
                    CockpitDevice.NAME_BUTTON_PAUSE -> "button_red_large1.png"
                    else -> throw IllegalArgumentException("No image defined for component: $component")
                }

        val loadResource = loadResource(uri)
        return ImageIO.read(loadResource)
    }
}

class UnHighlightTimerTask(private val component: GuiComponent) : TimerTask() {
    override fun run() {
        component.unHighlight()
    }
}

