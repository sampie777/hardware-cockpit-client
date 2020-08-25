package nl.sajansen.hardwarecockpitclient.gui.emulator


import nl.sajansen.hardwarecockpitclient.gui.loadResource
import nl.sajansen.hardwarecockpitclient.hardware.CockpitDevice
import nl.sajansen.hardwarecockpitclient.utils.NumberMap
import org.flypad.joystick.Joystick
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.util.logging.Logger
import javax.imageio.ImageIO
import javax.swing.JPanel

class HardwareEmulatorPanel : JPanel(null) {
    private val logger = Logger.getLogger(HardwareEmulatorPanel::class.java.name)

    val components = arrayListOf(
        createCockpitComponent(CockpitDevice.NAME_BUTTON_PAUSE, Point(75, 122), Dimension(88, 88)),
        createCockpitComponent(CockpitDevice.NAME_BUTTON_ATC, Point(105, 316), Dimension(56, 56)),
        createCockpitComponent(CockpitDevice.NAME_BUTTON_1, Point(196, 316), Dimension(56, 56)),
        createCockpitComponent(CockpitDevice.NAME_BUTTON_2, Point(105, 407), Dimension(56, 56)),
        createCockpitComponent(CockpitDevice.NAME_BUTTON_3, Point(196, 407), Dimension(56, 56)),
        createCockpitComponent(CockpitDevice.NAME_BUTTON_4, Point(105, 499), Dimension(56, 56)),
        createCockpitComponent(CockpitDevice.NAME_BUTTON_5, Point(196, 499), Dimension(56, 56)),
        createCockpitComponent(CockpitDevice.NAME_BUTTON_6, Point(105, 590), Dimension(56, 56)),
        createCockpitComponent(CockpitDevice.NAME_BUTTON_7, Point(196, 590), Dimension(56, 56)),
        createCockpitComponent(CockpitDevice.NAME_BUTTON_8, Point(105, 682), Dimension(56, 56)),
        createCockpitComponent(CockpitDevice.NAME_BUTTON_9, Point(196, 682), Dimension(56, 56)),
        createCockpitComponent(CockpitDevice.NAME_BUTTON_A, Point(919, 133), Dimension(56, 56)),
        createCockpitComponent(CockpitDevice.NAME_BUTTON_B, Point(1056, 133), Dimension(56, 56)),
        createCockpitComponent(CockpitDevice.NAME_BUTTON_C, Point(1193, 133), Dimension(56, 56)),
        createCockpitComponent(CockpitDevice.NAME_BUTTON_D, Point(1330, 133), Dimension(56, 56)),

        createCockpitComponent(CockpitDevice.NAME_SWITCH_BCN, Point(278, 133), Dimension(57, 57)),
        createCockpitComponent(CockpitDevice.NAME_SWITCH_LAND, Point(370, 133), Dimension(57, 57)),
        createCockpitComponent(CockpitDevice.NAME_SWITCH_TAXI, Point(461, 133), Dimension(57, 57)),
        createCockpitComponent(CockpitDevice.NAME_SWITCH_NAV, Point(553, 133), Dimension(57, 57)),
        createCockpitComponent(CockpitDevice.NAME_SWITCH_STROBE, Point(644, 133), Dimension(57, 57)),
        createCockpitComponent(CockpitDevice.NAME_SWITCH_CABIN, Point(736, 133), Dimension(57, 57)),
        createCockpitComponent(CockpitDevice.NAME_SWITCH_G, Point(1531, 545), Dimension(57, 57)),
        createCockpitComponent(CockpitDevice.NAME_SWITCH_PARKING_BRAKE, Point(1531, 695), Dimension(57, 57)),
        createCockpitComponent(CockpitDevice.NAME_SWITCH_LANDING_GEAR, Point(1751, 481), Dimension(65, 65)),
        createCockpitComponent(CockpitDevice.NAME_SWITCH_MASTER, Point(1787, 115), Dimension(33, 111)),

        createCockpitComponent(CockpitDevice.NAME_ROTARY_TRIM_ELEVATOR, Point(736, 336), Dimension(210, 46)),
        createCockpitComponent(CockpitDevice.NAME_ROTARY_TRIM_AILERONS, Point(736, 382), Dimension(210, 46)),
        createCockpitComponent(CockpitDevice.NAME_ROTARY_TRIM_RUDDER, Point(736, 428), Dimension(210, 46)),
        createCockpitComponent(CockpitDevice.NAME_ROTARY_AP_SPEED, Point(736, 474), Dimension(210, 46)),
        createCockpitComponent(CockpitDevice.NAME_ROTARY_AP_HEADING, Point(736, 520), Dimension(210, 46)),
        createCockpitComponent(CockpitDevice.NAME_ROTARY_AP_ALTITUDE, Point(736, 566), Dimension(210, 46)),
        createCockpitComponent(CockpitDevice.NAME_ROTARY_AP_VSPEED, Point(736, 611), Dimension(210, 46)),
        createCockpitComponent(CockpitDevice.NAME_ROTARY_E, Point(736, 657), Dimension(210, 46)),

        createCockpitComponent(CockpitDevice.NAME_SLIDER_FLAPS, Point(1088, 383), Dimension(74, 289)),
        createCockpitComponent(CockpitDevice.NAME_SLIDER_SPOILER, Point(1307, 383), Dimension(74, 289)),
        createCockpitComponent(CockpitDevice.NAME_SLIDER_F, Point(1506, 377), Dimension(107, 107)),
        createCockpitComponent(CockpitDevice.NAME_SLIDER_FEET_PEDAL_LEFT, Point(846 - 34, 799 - 58 / 2), Dimension(34, 58)),
        createCockpitComponent(CockpitDevice.NAME_SLIDER_FEET_PEDAL_RIGHT, Point(846 + 224, 799 - 58 / 2), Dimension(34, 58))
    )

    companion object {
        private var instance: HardwareEmulatorPanel? = null
        fun getInstance() = instance

        fun create(): HardwareEmulatorPanel {
            return instance ?: HardwareEmulatorPanel()
        }
    }

    private val originalSize: Dimension = Dimension(1920, 823)
    private var widthRatio: Double = 0.0
    private var heightRatio: Double = 0.0

    private fun Int.horRatio() = (this * widthRatio).toInt()
    private fun Int.verRatio() = (this * heightRatio).toInt()

    private val rudderIndicatorRailLocation = Point(846, 799)
    private val rudderIndicatorRailSize = Dimension(224, 6)
    private val rudderIndicatorSize = Dimension(10, 30)
    private var rudder: Int = 0
    private val rudderLeftComponent = CockpitDevice.components
        .find { it.name == CockpitDevice.NAME_SLIDER_FEET_PEDAL_LEFT }
    private val rudderRightComponent = CockpitDevice.components
        .find { it.name == CockpitDevice.NAME_SLIDER_FEET_PEDAL_RIGHT }

    private fun setSizeRatio() {
        widthRatio = width / originalSize.width.toDouble()
        heightRatio = height / originalSize.height.toDouble()
    }

    init {
        instance = this

        background = Color.WHITE

        addMouseListener(object : MouseListener {
            override fun mouseReleased(event: MouseEvent) {}
            override fun mouseEntered(event: MouseEvent) {}
            override fun mouseClicked(event: MouseEvent) {}
            override fun mouseExited(event: MouseEvent) {}

            override fun mousePressed(event: MouseEvent) {
                logger.info("Mouse press event on: [${event.x};${event.y}]")
                mouseClickOnLocation(event)
            }
        })
    }

    private fun createCockpitComponent(name: String, location: Point, size: Dimension): GuiComponent {
        val component = CockpitDevice.components.find { it.name == name }
            ?: throw IllegalArgumentException("Component name does not exist in CockpitDevice")
        return GuiComponent(component, location, size)
    }

    private fun mouseClickOnLocation(event: MouseEvent) {
        val clickPosition = Point(
            (event.x / widthRatio).toInt(),
            (event.y / heightRatio).toInt()
        )

        val component = components.find {
            it.location.x <= clickPosition.x && it.location.x + it.size.width >= clickPosition.x &&
                    it.location.y <= clickPosition.y && it.location.y + it.size.height >= clickPosition.y
        }
        if (component == null) {
            logger.info("No component found at that location")
            return
        }

        component.click()
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        setSizeRatio()

        drawBackgroundImage(g2)

        paintGuiComponents(g2)

        paintRudderIndicator(g2)
    }

    private fun drawBackgroundImage(g2: Graphics2D) {
        val backgroundImage =
            ImageIO.read(loadResource("/nl/sajansen/hardwarecockpitclient/gui/emulator/background.png"))
        g2.drawImage(backgroundImage, 0, 0, width, height, null)
    }

    private fun paintGuiComponents(g2: Graphics2D) {
        components.forEach { paintGuiComponent(g2, it) }
    }

    private fun paintGuiComponent(g2: Graphics2D, component: GuiComponent) {
        val image = component.getPaintingObject() ?: return
        g2.drawImage(
            image,
            component.location.x.horRatio(),
            component.location.y.verRatio(),
            component.size.width.horRatio() + 1,
            component.size.height.verRatio() + 1, null
        )
    }

    private fun paintRudderIndicator(g2: Graphics2D) {
        if (rudderLeftComponent == null || rudderRightComponent == null) {
            logger.warning("No rudder components found, cannot display rudder indicator")
            return
        }

        rudder = Joystick.ANALOG_MID + ((rudderLeftComponent.value() as Int) - (rudderRightComponent.value() as Int))

        val map = NumberMap(
            rudderIndicatorRailLocation.x,
            rudderIndicatorRailLocation.x + rudderIndicatorRailSize.width,
            Joystick.ANALOG_MIN,
            Joystick.ANALOG_MAX
        )
        val rudderIndicatorPositionX = map.map(rudder)

        g2.stroke = BasicStroke(1F)
        g2.color = Color(28, 28, 28)
        g2.fillRect(
            rudderIndicatorRailLocation.x.horRatio(),
            rudderIndicatorRailLocation.y.verRatio(),
            rudderIndicatorRailSize.width.horRatio() + 1,
            rudderIndicatorRailSize.height.verRatio() + 1
        )

        g2.color = Color(200, 200, 200)
        g2.fillOval(
            (rudderIndicatorPositionX - rudderIndicatorSize.width / 2).horRatio(),
            (rudderIndicatorRailLocation.y - rudderIndicatorSize.height / 2).verRatio() + 1,
            rudderIndicatorSize.width.horRatio() + 1,
            rudderIndicatorSize.height.verRatio() + 1
        )
    }
}