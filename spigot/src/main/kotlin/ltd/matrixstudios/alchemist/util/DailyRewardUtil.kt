package ltd.matrixstudios.alchemist.util

import com.cryptomorin.xseries.XSound
import ltd.matrixstudios.alchemist.AlchemistSpigotPlugin
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.service.profiles.ProfileGameService
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object DailyRewardUtil {

    // You can tweak these
    private const val DAILY_BASE_COINS = 100               // base reward
    private const val DAILY_COOLDOWN_MILLIS = 24 * 60 * 60 * 1000L   // 24h
    private const val STREAK_RESET_MILLIS = 48 * 60 * 60 * 1000L     // 48h

    /**
     * Lore to show on the Daily Reward button.
     */
    fun getDailyRewardLore(profile: GameProfile): MutableList<String> {
        val lore = mutableListOf<String>()
        lore.add("")

        val now = System.currentTimeMillis()
        val last = profile.lastDailyReward

        if (last == 0L || now - last >= DAILY_COOLDOWN_MILLIS) {
            // Can claim now
            val nextStreak = calculateNextStreak(profile, now)
            val nextReward = DAILY_BASE_COINS * nextStreak

            lore.add(Chat.format("&7Claim your daily reward!"))
            lore.add(Chat.format("&7Reward: &6$nextReward &ecoins"))
            if (profile.dailyRewardStreak > 0) {
                lore.add(Chat.format("&7Current Streak: &e${profile.dailyRewardStreak} days"))
            }
            lore.add("")
            lore.add(Chat.format("&aClick to claim!"))
        } else {
            // Still on cooldown
            val remaining = DAILY_COOLDOWN_MILLIS - (now - last)
            lore.add(Chat.format("&cYou have already claimed today's reward."))
            lore.add(Chat.format("&7Next in: &e${TimeUtil.formatDuration(remaining)}"))
            if (profile.dailyRewardStreak > 0) {
                lore.add(Chat.format("&7Current Streak: &e${profile.dailyRewardStreak} days"))
            }
        }

        lore.add("")
        return lore
    }

    /**
     * Called when the player clicks the Daily Reward button.
     */
    fun handleDailyRewardClaim(player: Player, profile: GameProfile): Boolean {
        val now = System.currentTimeMillis()
        val last = profile.lastDailyReward

        // Cooldown check
        if (last != 0L && now - last < DAILY_COOLDOWN_MILLIS) {
            val remaining = DAILY_COOLDOWN_MILLIS - (now - last)
            player.sendMessage(
                Chat.format(
                    "&cYou have already claimed your daily reward today! " +
                            "&7Come back in &e${TimeUtil.formatDuration(remaining)}&7."
                )
            )
            return false
        }

        // Determine new streak
        val nextStreak = calculateNextStreak(profile, now)
        val reward = DAILY_BASE_COINS * nextStreak

        profile.dailyRewardStreak = nextStreak
        profile.lastDailyReward = now
        profile.coins += reward

        ProfileGameService.save(profile)

        player.sendMessage(
            Chat.format(
                "&e&l[Rewards] &aYou claimed &e$reward &acoins! " +
                        "&7(Streak: &e${profile.dailyRewardStreak} days&7)"
            )
        )
        return true
    }
    fun handleDailyRewardClaimSilently(player: Player, profile: GameProfile) {
        val now = System.currentTimeMillis()
        val last = profile.lastDailyReward


        if (last != 0L && now - last < DAILY_COOLDOWN_MILLIS) {
            return
        }

        val nextStreak = calculateNextStreak(profile, now)
        val reward = DAILY_BASE_COINS * nextStreak

        profile.dailyRewardStreak = nextStreak
        profile.lastDailyReward = now
        profile.coins += reward

        ProfileGameService.save(profile)

        Bukkit.getScheduler().runTaskLater(
            AlchemistSpigotPlugin.instance,
            Runnable {
                if (!player.isOnline) return@Runnable

                XSound.ENTITY_CHICKEN_EGG.play(player, 1f, 1f)

                player.sendMessage(
                    Chat.format("&e&l[Rewards] &aYour daily reward was auto-claimed! &7(+${reward} coins)")
                )
            },
            5 * 20L
        )
    }

    private fun calculateNextStreak(profile: GameProfile, now: Long): Int {
        val last = profile.lastDailyReward

        if (last == 0L) {
            // First ever claim
            return 1
        }

        val diff = now - last

        return if (diff <= STREAK_RESET_MILLIS) {
            // Within streak window -> increase
            (profile.dailyRewardStreak.takeIf { it > 0 } ?: 1) + 1
        } else {
            // Missed too long -> reset
            1
        }
    }
}
