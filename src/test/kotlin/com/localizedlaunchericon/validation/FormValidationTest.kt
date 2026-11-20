package com.localizedlaunchericon.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FormValidationTest {

    private fun validate(
        addIcons: Boolean = false,
        hasForeground: Boolean = false,
        hasBackground: Boolean = false,
        addAppName: Boolean = false,
        appName: String = "",
    ) = validateSelection(addIcons, hasForeground, hasBackground, addAppName, appName)

    @Test
    fun `requires at least one thing to generate`() {
        assertEquals(SelectionError.NOTHING_SELECTED, validate())
    }

    @Test
    fun `icons require both images`() {
        assertEquals(SelectionError.IMAGES_MISSING, validate(addIcons = true))
        assertEquals(SelectionError.IMAGES_MISSING, validate(addIcons = true, hasForeground = true))
        assertEquals(SelectionError.IMAGES_MISSING, validate(addIcons = true, hasBackground = true))
        assertNull(validate(addIcons = true, hasForeground = true, hasBackground = true))
    }

    @Test
    fun `app name must not be blank`() {
        assertEquals(SelectionError.APP_NAME_EMPTY, validate(addAppName = true, appName = "   "))
        assertNull(validate(addAppName = true, appName = "Cook"))
    }

    @Test
    fun `missing images are ignored when icons are not requested`() {
        assertNull(validate(addIcons = false, addAppName = true, appName = "Cook"))
    }

    @Test
    fun `app name is ignored when it is not requested`() {
        assertNull(validate(addIcons = true, hasForeground = true, hasBackground = true, addAppName = false))
    }

    @Test
    fun `reports missing images before an empty app name`() {
        assertEquals(SelectionError.IMAGES_MISSING, validate(addIcons = true, addAppName = true))
    }
}
