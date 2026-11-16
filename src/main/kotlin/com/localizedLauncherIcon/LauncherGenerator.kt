package com.localizedLauncherIcon

import com.localizedLauncherIcon.validator.parseLocales
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.geom.Ellipse2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Generates a complete launcher icon set for one or more locales:
 *
 * ```
 * mipmap-<locale>-<density>/<name>.webp              48 dp, rounded square (legacy)
 * mipmap-<locale>-<density>/<name>_round.webp        48 dp, circle (legacy round)
 * mipmap-<locale>-<density>/<name>_foreground.webp   108 dp adaptive layer
 * mipmap-<locale>-<density>/<name>_background.webp   108 dp adaptive layer
 * mipmap-<locale>-anydpi-v26/<name>.xml, <name>_round.xml
 * ```
 *
 * WebP is used when the IDE provides an ImageIO WebP writer, PNG otherwise.
 */
object LauncherGenerator {

    /** Legacy launcher icon size. */
    const val LEGACY_ICON_DP = 48

    /** Adaptive icon layer size: 72 dp visible area + 18 dp on each side for masks and parallax. */
    const val ADAPTIVE_LAYER_DP = 108

    val DENSITIES: Map<String, Double> = linkedMapOf(
        "mdpi" to 1.0,
        "hdpi" to 1.5,
        "xhdpi" to 2.0,
        "xxhdpi" to 3.0,
        "xxxhdpi" to 4.0
    )

    private val imageFormat: String by lazy {
        if (ImageIO.getImageWritersByFormatName("webp").hasNext()) "webp" else "png"
    }

    /** Entry point used by the dialog. @return all files that were written. */
    suspend fun generateLauncherIcons(
        projectBasePath: String,
        iconName: String,
        localeCode: String,
        fgIconPath: String,
        bgIconPath: String,
        cornerArcDp: Int = 3
    ): List<File> {
        val (foreground, background) = withContext(Dispatchers.IO) {
            readImage(fgIconPath) to readImage(bgIconPath)
        }
        return generate(
            resDir = File(projectBasePath),
            iconName = iconName,
            locales = parseLocales(localeCode),
            foreground = foreground,
            background = background,
            cornerArcDp = cornerArcDp
        )
    }

    suspend fun generate(
        resDir: File,
        iconName: String,
        locales: List<String>,
        foreground: BufferedImage,
        background: BufferedImage,
        cornerArcDp: Int = 3
    ): List<File> = coroutineScope {
        val bitmaps = locales.flatMap { locale ->
            DENSITIES.map { (density, scale) ->
                async(Dispatchers.IO) {
                    val dir = File(resDir, "mipmap-$locale-$density")
                    generateForDensity(dir, iconName, scale, cornerArcDp, foreground, background)
                }
            }
        }
        val adaptiveXml = locales.map { locale ->
            async(Dispatchers.IO) { writeAdaptiveIconXml(File(resDir, "mipmap-$locale-anydpi-v26"), iconName) }
        }
        (bitmaps.awaitAll() + adaptiveXml.awaitAll()).flatten()
    }

    private fun generateForDensity(
        dir: File,
        iconName: String,
        scale: Double,
        cornerArcDp: Int,
        foreground: BufferedImage,
        background: BufferedImage
    ): List<File> {
        val legacy = (LEGACY_ICON_DP * scale).toInt()
        val layer = (ADAPTIVE_LAYER_DP * scale).toInt()
        val arc = (cornerArcDp * scale).toFloat()

        return listOf(
            write(
                compose(legacy, background, foreground, RoundRectangle2D.Float(0f, 0f, legacy.toFloat(), legacy.toFloat(), arc, arc)),
                dir, iconName
            ),
            write(
                compose(legacy, background, foreground, Ellipse2D.Float(0f, 0f, legacy.toFloat(), legacy.toFloat())),
                dir, "${iconName}_round"
            ),
            write(compose(layer, null, foreground, null), dir, "${iconName}_foreground"),
            write(compose(layer, background, null, null), dir, "${iconName}_background")
        )
    }

    /** Draws [background] and [foreground] scaled to [size] and cuts them with an anti-aliased [mask]. */
    internal fun compose(size: Int, background: BufferedImage?, foreground: BufferedImage?, mask: Shape?): BufferedImage {
        val output = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        output.createGraphics().apply {
            setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            background?.let { drawImage(it, 0, 0, size, size, null) }
            foreground?.let { drawImage(it, 0, 0, size, size, null) }
            if (mask != null) {
                // Graphics2D.clip is not anti-aliased, so apply the shape as an alpha mask instead.
                composite = AlphaComposite.DstIn
                drawImage(alphaMask(size, mask), 0, 0, null)
            }
            dispose()
        }
        return output
    }

    private fun alphaMask(size: Int, shape: Shape): BufferedImage =
        BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB).apply {
            createGraphics().apply {
                setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                color = Color.WHITE
                fill(shape)
                dispose()
            }
        }

    private fun write(image: BufferedImage, dir: File, name: String): File {
        dir.mkdirs()
        // aapt fails on duplicate resources, so remove a stale copy in the other format.
        listOf("png", "webp").filter { it != imageFormat }.forEach { File(dir, "$name.$it").delete() }

        val file = File(dir, "$name.$imageFormat")
        check(ImageIO.write(image, imageFormat, file)) { "No ImageIO writer for $imageFormat" }
        return file
    }

    private fun writeAdaptiveIconXml(dir: File, iconName: String): List<File> {
        dir.mkdirs()
        val xml = """
            |<?xml version="1.0" encoding="utf-8"?>
            |<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
            |    <background android:drawable="@mipmap/${iconName}_background" />
            |    <foreground android:drawable="@mipmap/${iconName}_foreground" />
            |</adaptive-icon>
            |""".trimMargin()
        return listOf(File(dir, "$iconName.xml"), File(dir, "${iconName}_round.xml"))
            .onEach { it.writeText(xml) }
    }

    private fun readImage(path: String): BufferedImage =
        requireNotNull(ImageIO.read(File(path))) { "Unsupported image format: $path (use PNG, JPEG or WebP)" }
}
