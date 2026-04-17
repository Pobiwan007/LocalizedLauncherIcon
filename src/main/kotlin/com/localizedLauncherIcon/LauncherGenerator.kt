package com.localizedLauncherIcon

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.awt.Image
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object LauncherGenerator {
    suspend fun generateLauncherIcons(
        projectBasePath: String,
        iconName: String,
        localeCode: String,
        fgIconPath: String,
        bgIconPath: String,
        cornerRadiusPx: Int = 3
    ) = coroutineScope {

        val densities = mapOf(
            "mdpi" to 1.0,
            "hdpi" to 1.5,
            "xhdpi" to 2.0,
            "xxhdpi" to 3.0,
            "xxxhdpi" to 4.0
        )

        val localeList = localeCode
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val fgImage = withContext(Dispatchers.IO) { ImageIO.read(File(fgIconPath)) }
        val bgImage = withContext(Dispatchers.IO) { ImageIO.read(File(bgIconPath)) }

        val baseSize = 48

        densities.flatMap { (dpi, scale) ->
            localeList.map { locale ->
                async(Dispatchers.Default) {
                    generateForOne(
                        dpi, scale, locale,
                        fgImage, bgImage,
                        baseSize, cornerRadiusPx,
                        projectBasePath, iconName
                    )
                }
            }
        }.awaitAll()

    }

    private suspend fun generateForOne(
        dpi: String,
        scale: Double,
        locale: String,
        fgImage: BufferedImage,
        bgImage: BufferedImage,
        baseSize: Int,
        cornerRadiusPx: Int,
        projectBasePath: String,
        iconName: String
    ) {
        val size = (baseSize * scale).toInt()
        val radius = (cornerRadiusPx * scale).toFloat()

        coroutineScope {
            val jobs = listOf(
                async { // background
                    val img = imageLauncher(
                        BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB),
                        bgImage,
                        size
                    )
                    createMipMapImage(img, projectBasePath, "${iconName}_background", dpi, locale)
                },
                async { // foreground
                    val img = imageLauncher(
                        fgImage,
                        BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB),
                        size
                    )
                    createMipMapImage(img, projectBasePath, "${iconName}_foreground", dpi, locale)
                },
                async { // round
                    val img = imageLauncher(
                        fgImage, bgImage, size, isCircle = true
                    )
                    createMipMapImage(img, projectBasePath, "${iconName}_round", dpi, locale)
                },
                async { // main icon
                    val img = imageLauncher(fgImage, bgImage, size, radius)
                    createMipMapImage(img, projectBasePath, iconName, dpi, locale)
                },
                async {
                    generateMonochromeLayout(projectBasePath, locale, iconName)
                }
            )

            jobs.awaitAll()
        }
    }

    private fun imageLauncher(
        fgImage: BufferedImage,
        bgImage: BufferedImage,
        size: Int,
        cornerRadius: Float = 0f,
        isCircle: Boolean = false
    ): BufferedImage {
        val outputImage = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g = outputImage.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // shape of image
        val clipShape = if (isCircle) {
            Ellipse2D.Float(0f, 0f, size.toFloat(), size.toFloat())
        } else {
            RoundRectangle2D.Float(0f, 0f, size.toFloat(), size.toFloat(), cornerRadius, cornerRadius)
        }
        g.clip = clipShape

        g.drawImage(bgImage.getScaledInstance(size, size, Image.SCALE_SMOOTH), 0, 0, null)
        g.drawImage(fgImage.getScaledInstance(size, size, Image.SCALE_SMOOTH), 0, 0, null)
        g.dispose()

        return outputImage
    }

    private fun createMipMapImage(
        image: BufferedImage,
        projectBasePath: String,
        iconName: String,
        dpi: String,
        localeCode: String
    ) {
        val outputDir = File("$projectBasePath/mipmap-$localeCode-$dpi")
        if (!outputDir.exists()) outputDir.mkdirs()

        val outputFile = File(outputDir, "$iconName.webp")
        ImageIO.write(image, "webp", outputFile)

        println("Saved: ${outputFile.absolutePath}")
    }

    private fun generateMonochromeLayout(projectBasePath: String, localeCode: String, iconName: String) {
        val xmlDir = File("$projectBasePath/mipmap-${localeCode}-anydpi-v26").apply { mkdirs() }
        val xmlContent = """
        |<?xml version="1.0" encoding="utf-8"?>
        |<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
        |    <background android:drawable="@mipmap/${iconName}_background" />
        |    <foreground android:drawable="@mipmap/${iconName}_foreground" />
        |</adaptive-icon>
    """.trimMargin()
        File(xmlDir, "${iconName}.xml").writeText(xmlContent)
        File(xmlDir, "${iconName}_round.xml").writeText(xmlContent)
    }
}