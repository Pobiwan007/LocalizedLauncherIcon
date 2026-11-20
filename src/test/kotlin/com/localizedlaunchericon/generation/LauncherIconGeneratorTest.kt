package com.localizedlaunchericon.generation

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.awt.Color
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class LauncherIconGeneratorTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun solid(color: Color, size: Int = 64) = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB).apply {
        createGraphics().apply {
            this.color = color
            fillRect(0, 0, size, size)
            dispose()
        }
    }

    private fun File.imageSize(): Int = ImageIO.read(this).width

    @Test
    fun `generates the full icon set for every locale and density`() = runBlocking {
        val res = tmp.newFolder("res")

        val files = LauncherIconGenerator.generate(
            resDir = res,
            iconName = "ic_launcher",
            locales = listOf("ru", "en-rUS"),
            foreground = solid(Color.WHITE),
            background = solid(Color.BLUE),
        )

        // 4 bitmaps x 5 densities x 2 locales + 2 xml x 2 locales
        assertEquals(44, files.size)
        assertTrue(files.all { it.exists() })
        assertTrue(
            File(res, "mipmap-ru-anydpi-v26/ic_launcher.xml").readText().contains("@mipmap/ic_launcher_foreground"),
        )
    }

    @Test
    fun `legacy icons are 48 dp and adaptive layers are 108 dp`() = runBlocking {
        val res = tmp.newFolder("res")
        val files = LauncherIconGenerator.generate(
            res,
            "ic_launcher",
            listOf("ru"),
            solid(Color.WHITE),
            solid(Color.BLUE),
        )

        fun size(density: String, name: String) =
            files.single { it.parentFile.name == "mipmap-ru-$density" && it.nameWithoutExtension == name }.imageSize()

        assertEquals(48, size("mdpi", "ic_launcher"))
        assertEquals(192, size("xxxhdpi", "ic_launcher_round"))
        assertEquals(108, size("mdpi", "ic_launcher_foreground"))
        assertEquals(432, size("xxxhdpi", "ic_launcher_background"))
    }

    @Test
    fun `round icon is transparent outside the circle and opaque inside`() {
        val size = 96
        val image = LauncherIconGenerator.compose(
            size,
            solid(Color.RED),
            null,
            Ellipse2D.Float(0f, 0f, size.toFloat(), size.toFloat()),
        )

        val cornerAlpha = image.getRGB(0, 0) ushr 24
        val centerAlpha = image.getRGB(size / 2, size / 2) ushr 24
        assertEquals(0, cornerAlpha)
        assertEquals(255, centerAlpha)
    }

    @Test
    fun `regeneration replaces files instead of duplicating resources`() = runBlocking {
        val res = tmp.newFolder("res")
        repeat(2) {
            LauncherIconGenerator.generate(res, "ic_launcher", listOf("ru"), solid(Color.WHITE), solid(Color.BLUE))
        }

        val names = File(res, "mipmap-ru-mdpi").listFiles()!!.map { it.nameWithoutExtension }
        assertEquals(names.distinct().size, names.size)
    }
}
