package com.localizedlaunchericon.generation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class AppNameGeneratorTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun appNameIn(file: File): String =
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
            .getElementsByTagName("string").item(0).textContent

    @Test
    fun `creates strings xml for every locale`() {
        val res = tmp.newFolder("res")

        val files = AppNameGenerator.generate(res, listOf("ru", "kk-rKZ"), "Повар")

        assertEquals(listOf("values-ru", "values-kk-rKZ"), files.map { it.parentFile.name })
        files.forEach { assertEquals("Повар", appNameIn(it)) }
    }

    @Test
    fun `replaces an existing app_name and keeps other strings`() {
        val res = tmp.newFolder("res")
        val strings = File(res, "values-ru/strings.xml").apply {
            parentFile.mkdirs()
            writeText(
                """
                <resources>
                    <string name="app_name" translatable="true">Old</string>
                    <string name="hello">Привет</string>
                </resources>
                """.trimIndent()
            )
        }

        AppNameGenerator.generate(res, listOf("ru"), "New")

        val text = strings.readText()
        assertTrue(text, text.contains("""<string name="app_name">New</string>"""))
        assertTrue(text, text.contains("""<string name="hello">Привет</string>"""))
        assertEquals(1, Regex("app_name").findAll(text).count())
    }

    @Test
    fun `appends app_name when the file has none`() {
        val res = tmp.newFolder("res")
        val strings = File(res, "values-ru/strings.xml").apply {
            parentFile.mkdirs()
            writeText("<resources>\n    <string name=\"hello\">Привет</string>\n</resources>")
        }

        AppNameGenerator.generate(res, listOf("ru"), "Повар")

        assertTrue(strings.readText().contains("""<string name="app_name">Повар</string>"""))
    }

    @Test
    fun `special characters produce valid xml`() {
        val res = tmp.newFolder("res")

        val file = AppNameGenerator.generate(res, listOf("en"), "Tom & Jerry's <\$1>").single()

        // Parses as XML and keeps aapt escapes for the apostrophe.
        assertEquals("Tom & Jerry\\'s <\$1>", appNameIn(file))
    }

    @Test
    fun `escapes aapt special characters`() {
        assertEquals("Let\\'s \\\"go\\\"", AppNameGenerator.escapeAndroidString("Let's \"go\""))
        assertEquals("a\\\\b", AppNameGenerator.escapeAndroidString("a\\b"))
        assertEquals("\\@home", AppNameGenerator.escapeAndroidString("@home"))
        assertEquals("\\?x", AppNameGenerator.escapeAndroidString("?x"))
        assertEquals("1 &lt; 2 &amp;&amp; 3 &gt; 2", AppNameGenerator.escapeAndroidString("1 < 2 && 3 > 2"))
    }

    @Test
    fun `fails instead of silently skipping a strings xml without a closing tag`() {
        val res = tmp.newFolder("res")
        File(res, "values-ru/strings.xml").apply {
            parentFile.mkdirs()
            writeText("<resources>")
        }

        val error = assertThrows(IllegalStateException::class.java) {
            AppNameGenerator.generate(res, listOf("ru"), "Cook")
        }
        assertTrue(error.message, error.message!!.contains("</resources>"))
    }

    @Test
    fun `escapes new lines and keeps non-leading at signs`() {
        assertEquals("line1\\nline2", AppNameGenerator.escapeAndroidString("line1\nline2"))
        assertEquals("me@home", AppNameGenerator.escapeAndroidString("me@home"))
    }
}
