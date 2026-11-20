package com.localizedlaunchericon.generation

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class LocalizationGeneratorTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun pngFile(name: String, color: Color): File = tmp.newFile(name).also { file ->
        val image = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
        image.createGraphics().apply {
            this.color = color
            fillRect(0, 0, 32, 32)
            dispose()
        }
        ImageIO.write(image, "png", file)
    }

    private fun icons() = LocalizationRequest.Icons(
        name = "ic_launcher",
        foreground = pngFile("fg.png", Color.WHITE),
        background = pngFile("bg.png", Color.BLUE),
    )

    @Test
    fun `generates icons and app name and reports progress steps in order`() = runBlocking {
        val res = tmp.newFolder("res")
        val steps = mutableListOf<String>()

        val files = LocalizationGenerator.generate(
            LocalizationRequest(res, listOf("ru"), icons = icons(), appName = "Cook"),
        ) { steps += it }

        // 4 bitmaps x 5 densities + 2 adaptive XML + strings.xml
        assertEquals(23, files.size)
        assertTrue(File(res, "values-ru/strings.xml").exists())
        assertEquals(listOf("Generating launcher icons…", "Writing app names…"), steps)
    }

    @Test
    fun `generates only the app name when no icons are requested`() = runBlocking {
        val res = tmp.newFolder("res")

        val files = LocalizationGenerator.generate(LocalizationRequest(res, listOf("ru", "kk-rKZ"), appName = "Cook"))

        assertEquals(listOf("values-ru", "values-kk-rKZ"), files.map { it.parentFile.name })
        assertTrue(res.listFiles()!!.none { it.name.startsWith("mipmap") })
    }

    @Test
    fun `fails with a readable message for an unreadable image`() {
        val res = tmp.newFolder("res")
        val notAnImage = tmp.newFile("broken.png").apply { writeText("not an image") }
        val request = LocalizationRequest(
            res,
            listOf("ru"),
            icons = icons().copy(foreground = notAnImage),
        )

        val error = assertThrows(IllegalArgumentException::class.java) {
            runBlocking { LocalizationGenerator.generate(request) }
        }
        assertTrue(error.message, error.message!!.contains("broken.png"))
    }
}
