package com.localizedlaunchericon.generation

import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.File

class LocalizationRequestTest {

    private val res = File("res")

    @Test
    fun `rejects a request without locales`() {
        assertThrows(IllegalArgumentException::class.java) {
            LocalizationRequest(res, emptyList(), appName = "Cook")
        }
    }

    @Test
    fun `rejects a request with nothing to generate`() {
        assertThrows(IllegalArgumentException::class.java) {
            LocalizationRequest(res, listOf("ru"))
        }
    }
}
