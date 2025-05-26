package com.localizedLauncherIcon

import java.awt.Image
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object LauncherGenerator {
    fun generateLauncherIcons(
        projectBasePath: String,
        iconName: String,
        localeCode: String,
        fgIconPath: String,
        bgIconPath: String,
        cornerRadiusPx: Int = 3
    ) {
        val densities = mapOf(
            "mdpi" to 1.0,
            "hdpi" to 1.5,
            "xhdpi" to 2.0,
            "xxhdpi" to 3.0,
            "xxxhdpi" to 4.0
        )

        val fgImage = ImageIO.read(File(fgIconPath))
        val bgImage = ImageIO.read(File(bgIconPath))
        val baseSize = 48

        for ((dpi, scale) in densities) {
            val size = (baseSize * scale).toInt()
            val radius = (cornerRadiusPx * scale).toFloat()

            //create ic_launcher_background
            val backgroundImage = imageLauncher(
                fgImage = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB),
                bgImage = bgImage,
                size = size,
                cornerRadius = 0f
            )
            createMipMapImage(backgroundImage, projectBasePath, "ic_launcher_background", dpi, localeCode)

            //create ic_launcher_foreground
            val foregroundImage = imageLauncher(
                fgImage = fgImage,
                bgImage = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB),
                size = size,
                cornerRadius = 0f
            )
            createMipMapImage(foregroundImage, projectBasePath, "ic_launcher_foreground", dpi, localeCode)

            //create ic_launcher_round
            val roundImage = imageLauncher(
                fgImage = fgImage,
                bgImage = bgImage,
                size = size,
                isCircle = true
            )
            createMipMapImage(roundImage, projectBasePath, "ic_launcher_round", dpi, localeCode)

            //ic_launcher
            val outputImage = imageLauncher(fgImage, bgImage, size, radius)

            createMipMapImage(
                image = outputImage,
                projectBasePath = projectBasePath,
                iconName = iconName,
                dpi = dpi,
                localeCode = localeCode
            )
        }

        generateMonochromeLayout(projectBasePath, localeCode)
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

    private fun generateMonochromeLayout(projectBasePath: String, localeCode: String){
        val xmlDir = File("$projectBasePath/mipmap-${localeCode}-anydpi-v26").apply { mkdirs() }
        val xmlContent = """
        |<?xml version="1.0" encoding="utf-8"?>
        |<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
        |    <background android:drawable="@mipmap/ic_launcher_background" />
        |    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
        |</adaptive-icon>
    """.trimMargin()
        File(xmlDir, "ic_launcher.xml").writeText(xmlContent)
        File(xmlDir, "ic_launcher_round.xml").writeText(xmlContent)
    }
}