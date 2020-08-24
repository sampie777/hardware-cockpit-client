package nl.sajansen.hardwarecockpitclient.gui

import nl.sajansen.hardwarecockpitclient.ApplicationInfo
import java.awt.Graphics2D
import java.awt.Image
import java.awt.RenderingHints
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.net.URL
import java.util.logging.Logger

private val logger: Logger = Logger.getLogger("utils")

fun createGraphics(width: Int, height: Int, mode: Int = BufferedImage.TYPE_INT_ARGB): Pair<BufferedImage, Graphics2D> {
    val bufferedImage = BufferedImage(width, height, mode)
    val g2: Graphics2D = bufferedImage.createGraphics()
    setDefaultRenderingHints(g2)
    return Pair(bufferedImage, g2)
}

fun setDefaultRenderingHints(g2: Graphics2D) {
    g2.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON
    )
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON
    )
    g2.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR
    )
}

fun drawImageInXYCenter(mainGraphics2D: Graphics2D, mainWidth: Int, mainHeight: Int, bufferedImage: BufferedImage) {
    mainGraphics2D.drawImage(
        bufferedImage,
        null,
        (mainWidth - bufferedImage.width) / 2,
        (mainHeight - bufferedImage.height) / 2
    )
}

fun drawImageInXCenter(mainGraphics2D: Graphics2D, y: Int, mainWidth: Int, bufferedImage: BufferedImage) {
    mainGraphics2D.drawImage(bufferedImage, null, (mainWidth - bufferedImage.width) / 2, y)
}

fun drawImageInYCenter(mainGraphics2D: Graphics2D, mainHeight: Int, x: Int, bufferedImage: BufferedImage) {
    mainGraphics2D.drawImage(bufferedImage, null, x, (mainHeight - bufferedImage.height) / 2)
}

fun loadImage(path: String): Image? {
    val resource: URL? = loadResource(path)
    if (resource == null) {
        logger.warning("Could not find image: $path")
        return null
    }

    return Toolkit.getDefaultToolkit().getImage(resource)
}

fun loadResource(path: String) = ApplicationInfo::class.java.getResource(path)