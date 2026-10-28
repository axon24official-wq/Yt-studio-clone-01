package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.LocalAdminStore
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AdminSessionSecurityTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        LocalAdminStore.init(context)
        LocalAdminStore.logout()
        LocalAdminStore.clearUsedKeysForTesting()
    }

    @Test
    fun test1_initialAdminLoginStarts10MinuteSession() {
        assertFalse(LocalAdminStore.isLoggedIn())
        assertFalse(LocalAdminStore.isSessionActive())

        val success = LocalAdminStore.login("admin", "Admin123")
        assertTrue(success)
        assertTrue(LocalAdminStore.isLoggedIn())
        assertTrue(LocalAdminStore.isSessionActive())
        assertFalse(LocalAdminStore.isUnlimitedSession())

        val remainingSec = LocalAdminStore.getRemainingSeconds()
        // Should be between 590 and 600 seconds
        assertTrue("Expected ~600 seconds, got $remainingSec", remainingSec in 590..600)
    }

    @Test
    fun test2_kingpanelOneTimeUsage10Minutes() {
        LocalAdminStore.login("admin", "Admin123")
        LocalAdminStore.lockSession()
        assertFalse(LocalAdminStore.isSessionActive())
        assertFalse(LocalAdminStore.isKeyUsed("Kingpanel"))

        // First redemption: must succeed for 10 minutes
        val firstResult = LocalAdminStore.unlockWithPassword("Kingpanel")
        assertEquals(LocalAdminStore.UnlockResult.SUCCESS_10_MIN, firstResult)
        assertTrue(LocalAdminStore.isSessionActive())
        assertFalse(LocalAdminStore.isUnlimitedSession())
        assertTrue(LocalAdminStore.isKeyUsed("Kingpanel"))

        val remainingSec = LocalAdminStore.getRemainingSeconds()
        assertTrue("Expected ~600 seconds for Kingpanel, got $remainingSec", remainingSec in 590..600)

        // Session lock again
        LocalAdminStore.lockSession()
        assertFalse(LocalAdminStore.isSessionActive())

        // Second redemption: must be rejected as already used
        val secondResult = LocalAdminStore.unlockWithPassword("Kingpanel")
        assertEquals(LocalAdminStore.UnlockResult.INVALID_OR_USED_KEY, secondResult)
        assertFalse(LocalAdminStore.isSessionActive())

        // Third redemption: still rejected
        val thirdResult = LocalAdminStore.unlockWithPassword("Kingpanel")
        assertEquals(LocalAdminStore.UnlockResult.INVALID_OR_USED_KEY, thirdResult)
        assertFalse(LocalAdminStore.isSessionActive())
    }

    @Test
    fun test3_rajsingpanelOneTimeUsage20Minutes() {
        LocalAdminStore.login("admin", "Admin123")
        LocalAdminStore.lockSession()
        assertFalse(LocalAdminStore.isSessionActive())
        assertFalse(LocalAdminStore.isKeyUsed("Rajsingpanel"))

        // First redemption: must succeed for 20 minutes
        val firstResult = LocalAdminStore.unlockWithPassword("Rajsingpanel")
        assertEquals(LocalAdminStore.UnlockResult.SUCCESS_20_MIN, firstResult)
        assertTrue(LocalAdminStore.isSessionActive())
        assertFalse(LocalAdminStore.isUnlimitedSession())
        assertTrue(LocalAdminStore.isKeyUsed("Rajsingpanel"))

        val remainingSec = LocalAdminStore.getRemainingSeconds()
        assertTrue("Expected ~1200 seconds for Rajsingpanel, got $remainingSec", remainingSec in 1190..1200)

        // Session lock again
        LocalAdminStore.lockSession()
        assertFalse(LocalAdminStore.isSessionActive())

        // Second redemption: must be rejected as already used
        val secondResult = LocalAdminStore.unlockWithPassword("Rajsingpanel")
        assertEquals(LocalAdminStore.UnlockResult.INVALID_OR_USED_KEY, secondResult)
        assertFalse(LocalAdminStore.isSessionActive())
    }

    @Test
    fun test4_dkpanelGrantsUnlimitedAccessAndCanBeReused() {
        LocalAdminStore.login("admin", "Admin123")
        LocalAdminStore.lockSession()
        assertFalse(LocalAdminStore.isSessionActive())

        // First use: unlimited access
        val result1 = LocalAdminStore.unlockWithPassword("Dkpanel")
        assertEquals(LocalAdminStore.UnlockResult.SUCCESS_UNLIMITED, result1)
        assertTrue(LocalAdminStore.isSessionActive())
        assertTrue(LocalAdminStore.isUnlimitedSession())
        assertEquals(Long.MAX_VALUE, LocalAdminStore.getRemainingSeconds())

        // Dkpanel must NOT be marked as a one-time key
        assertFalse(LocalAdminStore.isKeyUsed("Dkpanel"))

        // Lock and unlock with Dkpanel again (permanent key remains reusable)
        LocalAdminStore.lockSession()
        assertFalse(LocalAdminStore.isSessionActive())

        val result2 = LocalAdminStore.unlockWithPassword("Dkpanel")
        assertEquals(LocalAdminStore.UnlockResult.SUCCESS_UNLIMITED, result2)
        assertTrue(LocalAdminStore.isSessionActive())
        assertTrue(LocalAdminStore.isUnlimitedSession())

        // Ensure invalid password fails cleanly
        LocalAdminStore.lockSession()
        val invalidResult = LocalAdminStore.unlockWithPassword("WrongPassword")
        assertEquals(LocalAdminStore.UnlockResult.INVALID_OR_USED_KEY, invalidResult)
    }

    @Test
    fun test5_appRestartDoesNotResetExpiredTimedSession() {
        LocalAdminStore.login("admin", "Admin123")
        // Simulate session expired 1 minute ago
        val pastExpiry = System.currentTimeMillis() - 60_000L
        val prefs = context.getSharedPreferences("yt_studio_local_admin_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("admin_session_expiry", pastExpiry).commit()

        // Simulate app restart by re-initializing store from disk
        LocalAdminStore.init(context)
        LocalAdminStore.refreshSessionTick()

        // Logged in is still true, but session is expired
        assertTrue(LocalAdminStore.isLoggedIn())
        assertFalse(LocalAdminStore.isSessionActive())
        assertEquals(0L, LocalAdminStore.getRemainingSeconds())
    }

    @Test
    fun test6_usedKeysSurviveAppRestartAndLogout() {
        LocalAdminStore.login("admin", "Admin123")
        LocalAdminStore.lockSession()

        // Redeem Kingpanel and Rajsingpanel once
        assertEquals(LocalAdminStore.UnlockResult.SUCCESS_10_MIN, LocalAdminStore.unlockWithPassword("Kingpanel"))
        LocalAdminStore.lockSession()
        assertEquals(LocalAdminStore.UnlockResult.SUCCESS_20_MIN, LocalAdminStore.unlockWithPassword("Rajsingpanel"))

        assertTrue(LocalAdminStore.isKeyUsed("Kingpanel"))
        assertTrue(LocalAdminStore.isKeyUsed("Rajsingpanel"))

        // Perform full logout
        LocalAdminStore.logout()
        assertFalse(LocalAdminStore.isLoggedIn())

        // Simulate app restart
        LocalAdminStore.init(context)

        // Login again
        LocalAdminStore.login("admin", "Admin123")
        LocalAdminStore.lockSession()

        // Both keys must still be permanently USED and rejected
        assertTrue(LocalAdminStore.isKeyUsed("Kingpanel"))
        assertTrue(LocalAdminStore.isKeyUsed("Rajsingpanel"))

        assertEquals(LocalAdminStore.UnlockResult.INVALID_OR_USED_KEY, LocalAdminStore.unlockWithPassword("Kingpanel"))
        assertEquals(LocalAdminStore.UnlockResult.INVALID_OR_USED_KEY, LocalAdminStore.unlockWithPassword("Rajsingpanel"))
    }

    @Test
    fun test7_usedKeysSurviveDemoDataReset() {
        LocalAdminStore.login("admin", "Admin123")
        LocalAdminStore.lockSession()

        // Redeem Kingpanel
        assertEquals(LocalAdminStore.UnlockResult.SUCCESS_10_MIN, LocalAdminStore.unlockWithPassword("Kingpanel"))
        assertTrue(LocalAdminStore.isKeyUsed("Kingpanel"))

        // Reset demo data
        LocalAdminStore.resetToDefaultDemoData(context)

        // Re-check: one-time key status MUST remain USED
        assertTrue(LocalAdminStore.isKeyUsed("Kingpanel"))
        LocalAdminStore.lockSession()
        assertEquals(LocalAdminStore.UnlockResult.INVALID_OR_USED_KEY, LocalAdminStore.unlockWithPassword("Kingpanel"))
    }

    @Test
    fun test8_existingAdminPanelDataRemainsIntact() {
        LocalAdminStore.login("admin", "Admin123")

        // Save channel profile data
        LocalAdminStore.saveChannelProfile(
            name = "Test Channel",
            handle = "@testchannel",
            subscribersCount = "1.5M",
            email = "test@example.com",
            description = "Custom description",
            avatarUri = null,
            isMonetized = true
        )

        // Lock session and unlock with Dkpanel
        LocalAdminStore.lockSession()
        assertFalse(LocalAdminStore.isSessionActive())
        assertEquals(LocalAdminStore.UnlockResult.SUCCESS_UNLIMITED, LocalAdminStore.unlockWithPassword("Dkpanel"))
        assertTrue(LocalAdminStore.isSessionActive())

        // Check that repository and store data remain preserved
        val prefs = context.getSharedPreferences("yt_studio_local_admin_prefs", Context.MODE_PRIVATE)
        assertEquals("Test Channel", prefs.getString("channel_name", null))
        assertEquals("@testchannel", prefs.getString("channel_handle", null))
        assertEquals("1.5M", prefs.getString("channel_subs", null))
        assertTrue(LocalAdminStore.hasCustomLocalData())
    }
}
