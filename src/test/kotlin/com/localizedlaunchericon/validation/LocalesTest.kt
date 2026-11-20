package com.localizedlaunchericon.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LocalesTest {

    @Test
    fun `parses comma separated list, trimming blanks and duplicates`() {
        assertEquals(listOf("es-rCL", "es-rAR", "qu-rPE"), parseLocales(" es-rCL, es-rAR ,, qu-rPE, es-rCL "))
    }

    @Test
    fun `accepts language and language-region qualifiers`() {
        listOf("ru", "fil", "en-rUS", "kk-rKZ", "en, ru, uz-rUZ").forEach {
            assertNull(it, validateLocales(it))
        }
    }

    @Test
    fun `rejects malformed qualifiers and names the bad one`() {
        assertNotNull(validateLocales(""))
        assertNotNull(validateLocales("RU"))
        assertNotNull(validateLocales("en-US"))
        val error = validateLocales("en, ru_RU")
        assertNotNull(error)
        assert(error!!.contains("ru_RU")) { error }
    }

    @Test
    fun `validates android resource names`() {
        assertNull(validateIconName("ic_launcher"))
        assertNull(validateIconName("ic_launcher_2"))
        assertNotNull(validateIconName(""))
        assertNotNull(validateIconName("IcLauncher"))
        assertNotNull(validateIconName("1icon"))
        assertNotNull(validateIconName("ic-launcher"))
    }
}
