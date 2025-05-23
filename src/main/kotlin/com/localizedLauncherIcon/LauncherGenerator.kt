package com.localizedLauncherIcon

import java.awt.Image
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object LauncherGenerator {
    fun generateLauncherIcons(
        projectBasePath: String,
        iconName: String,
        localeCode: String,
        fgIconPath: String,
        bgIconPath: String
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

        val baseSize = 48 // px, базовый размер для mdpi

        for ((dpi, scale) in densities) {
            val size = (baseSize * scale).toInt()

            val outputImage = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
            val g = outputImage.createGraphics()
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            // Отрисовываем фон
            g.drawImage(bgImage.getScaledInstance(size, size, Image.SCALE_SMOOTH), 0, 0, null)

            // Отрисовываем передний план
            g.drawImage(fgImage.getScaledInstance(size, size, Image.SCALE_SMOOTH), 0, 0, null)
            g.dispose()

            // Путь до директории res/mipmap-dpi-localeCode/
            val outputDir = File("$projectBasePath/mipmap-$localeCode-$dpi")
            println("OutPut: $outputDir")
            if (!outputDir.exists()) outputDir.mkdirs()

            val outputFile = File(outputDir, "$iconName.webp")
            ImageIO.write(outputImage, "webp", outputFile)
        }
    }

}