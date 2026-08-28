package com.djisyncflow.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class UpdateCheckerTest {
    private val validHash = "a".repeat(64)
    private val verifiedUpdate = UpdateInfo(
        versionCode = 999,
        versionName = "9.99",
        apkUrl = "https://github.com/Falkinou/viseofh/releases/latest/download/Orange-Drone-Compagnon.apk",
        apkSha256 = validHash,
        apkSizeBytes = 1234,
    )

    @Test
    fun acceptsNewVerifiedManifest() {
        val update = checker { verifiedUpdate }.check()

        assertNotNull(update)
        assertEquals("9.99", update?.versionName)
        assertEquals(validHash, update?.apkSha256)
        assertEquals(1234L, update?.apkSizeBytes)
    }

    @Test
    fun fallsBackToSecondManifestWhenPrimaryFails() {
        val update = UpdateChecker(
            manifestUrls = listOf("memory://primary", "memory://legacy"),
            manifestLoader = { url ->
                if (url.endsWith("primary")) error("primary unavailable")
                "legacy manifest"
            },
            manifestParser = { verifiedUpdate },
        ).check()

        assertEquals("9.99", update?.versionName)
    }

    @Test
    fun keepsUpToDateStateWhenManifestHasNoNewVersion() {
        val update = checker { null }.check()

        assertNull(update)
    }

    @Test
    fun surfacesInvalidDownloadMetadata() {
        val error = assertThrows(IllegalStateException::class.java) {
            checker { throw IllegalArgumentException("Empreinte SHA-256 absente") }.check()
        }

        assertTrue(error.cause?.message.orEmpty().contains("SHA-256"))
    }

    @Test
    fun surfacesDisallowedDownloadHost() {
        val error = assertThrows(IllegalStateException::class.java) {
            checker { throw IllegalArgumentException("Domaine APK non autorisé") }.check()
        }

        assertTrue(error.cause?.message.orEmpty().contains("Domaine APK"))
    }

    private fun checker(parser: () -> UpdateInfo?): UpdateChecker = UpdateChecker(
        manifestUrls = listOf("memory://manifest"),
        manifestLoader = { "memory manifest" },
        manifestParser = { parser() },
    )
}
