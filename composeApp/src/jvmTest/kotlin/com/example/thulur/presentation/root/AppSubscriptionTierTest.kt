package com.example.thulur.presentation.root

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppSubscriptionTierTest {
    @Test
    fun `maps known api tiers into app tiers`() {
        assertEquals(AppSubscriptionTier.Free, "free".toAppSubscriptionTier())
        assertEquals(AppSubscriptionTier.Pro, "pro".toAppSubscriptionTier())
        assertEquals(AppSubscriptionTier.Corporate, "corporate".toAppSubscriptionTier())
    }

    @Test
    fun `maps unknown api tiers to unknown`() {
        assertEquals(AppSubscriptionTier.Unknown, "legacy".toAppSubscriptionTier())
    }

    @Test
    fun `allows thread discussion only for paid tiers`() {
        assertFalse(AppSubscriptionTier.Unknown.canDiscussThread())
        assertFalse(AppSubscriptionTier.Free.canDiscussThread())
        assertTrue(AppSubscriptionTier.Pro.canDiscussThread())
        assertTrue(AppSubscriptionTier.Corporate.canDiscussThread())
    }
}
