package ltd.matrixstudios.website.game

import com.google.gson.JsonObject
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.models.website.AlchemistUser
import ltd.matrixstudios.alchemist.service.expirable.RankGrantService
import ltd.matrixstudios.alchemist.service.profiles.ProfileGameService
import ltd.matrixstudios.alchemist.service.chatsnap.ChatSnapService
import ltd.matrixstudios.website.user.loader.UserServicesComponent
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import java.util.*
import javax.servlet.http.HttpServletRequest

@Controller
class GameProfileController {

    @RequestMapping(value = ["/api/users"], method = [RequestMethod.GET])
    fun getAllUsers(request: HttpServletRequest): ModelAndView {
        val modelAndView = ModelAndView("user/users")

        val profile = request.session.getAttribute("user") as AlchemistUser?
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "You must be logged in to view this page")
        if (!profile.hasPermission("alchemist.website.users")) throw ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "You do not have permission to view this page."
        )

        val page = 1
        val users = ProfileGameService.handler.retrieveAll().take(page * 10)

        modelAndView.addObject("section", "profiles")
        modelAndView.addObject("user", profile)
        modelAndView.addObject("users", users)
        modelAndView.addObject("page", page)
        return modelAndView
    }

    @RequestMapping(value = ["/api/users/lookup/{id}"], method = [RequestMethod.GET])
    fun onLookupProfile(
        @PathVariable id: String,
        @RequestParam(value = "tab", defaultValue = "general") tab: String,
        request: HttpServletRequest
    ): ModelAndView {
        val modelAndView = ModelAndView("user/user-lookup")

        val profile = request.session.getAttribute("user") as AlchemistUser?
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "You must be logged in to view this page")
        if (!profile.hasPermission("alchemist.website.users")) throw ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "You do not have permission to view this page."
        )

        val found = UserServicesComponent.userService.findProfileByNiceUUID(id)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "The user requested does not exist. Please ensure that the UUID is correct!"
            )

        ltd.matrixstudios.alchemist.service.server.UniqueServerService.loadAll()

        val friends = getFriendProfiles(found.friends)

        val allGrants = RankGrantService.findByTarget(found.uuid).get().sortedByDescending { it.expirable.addedAt }
        
        val highestRankEver = allGrants.mapNotNull { grant ->
            ltd.matrixstudios.alchemist.service.ranks.RankService.byId(grant.rankId)
        }.maxByOrNull { it.weight }
        
        val currentHighestRank = found.getHighestGlobalRank()
        
        val hadHigherRank = highestRankEver != null && 
                           (currentHighestRank.weight < highestRankEver.weight || 
                            (currentHighestRank.weight == highestRankEver.weight && 
                             currentHighestRank.id != highestRankEver.id && 
                             allGrants.any { it.rankId == highestRankEver.id && !it.expirable.isActive() }))
        
        val defaultRank = ltd.matrixstudios.alchemist.service.ranks.RankService.findFirstAvailableDefaultRank()
        val rankHistoryMap = allGrants
            .mapNotNull { grant ->
                val rank = ltd.matrixstudios.alchemist.service.ranks.RankService.byId(grant.rankId)
                if (rank != null) {
                    RankHistoryEntry(
                        rank = rank,
                        grantedAt = grant.expirable.addedAt,
                        isActive = grant.expirable.isActive(),
                        grant = grant
                    )
                } else null
            }
            .groupBy { it.rank.id }
            .mapValues { (_, entries) ->
                entries.maxByOrNull { it.grantedAt } ?: entries.first()
            }
            .toMutableMap()
        
        if (defaultRank != null) {
            val hasDefaultRankInGrants = rankHistoryMap.keys.contains(defaultRank.id)
            val currentRankIsDefault = currentHighestRank.id == defaultRank.id
            val hasActiveGrants = allGrants.any { it.expirable.isActive() }
            
            if (!hasDefaultRankInGrants && (allGrants.isEmpty() || (currentRankIsDefault && !hasActiveGrants))) {
                val defaultGrantedAt = found.firstLoginAt ?: found.lastLoginAt ?: System.currentTimeMillis()
                val isDefaultActive = currentRankIsDefault
                
                rankHistoryMap[defaultRank.id] = RankHistoryEntry(
                    rank = defaultRank,
                    grantedAt = defaultGrantedAt,
                    isActive = isDefaultActive,
                    grant = null
                )
            }
        }
        
        val rankHistory = rankHistoryMap.values
            .sortedWith(compareByDescending<RankHistoryEntry> { it.isActive }
                .thenByDescending { it.grantedAt })

        when (tab.lowercase()) {
            "punishments" -> {
                val punishments = found.getPunishments().sortedByDescending { it.expirable.addedAt }
                modelAndView.addObject("punishments", punishments)
            }
            "grants" -> {
                val grants = allGrants
                modelAndView.addObject("grants", grants)
            }
            "chatsnaps" -> {
                val chatSnaps = ChatSnapService.getByOwner(found.uuid, 50)
                modelAndView.addObject("chatSnaps", chatSnaps)
            }
            "tickets" -> {
                modelAndView.addObject("tickets", emptyList<Any>())
            }
            "staffhistory" -> {
                val executedPunishments = ltd.matrixstudios.alchemist.service.expirable.PunishmentService.findExecutorPunishments(found.uuid)
                    .sortedByDescending { it.expirable.addedAt }
                    .map { punishment ->
                        val targetProfile = ProfileGameService.byId(punishment.target)
                        PunishmentWithTarget(punishment, targetProfile?.username ?: "Unknown")
                    }
                modelAndView.addObject("staffHistory", executedPunishments)
            }
            "staffperformance" -> {
                modelAndView.addObject("staffPerformance", emptyList<Any>())
            }
        }

        val lastSeenAgo = if (!found.isOnline() && found.lastSeenAt > 0) {
            formatDuration(System.currentTimeMillis() - found.lastSeenAt)
        } else null

        modelAndView.addObject("target", found)
        modelAndView.addObject("users", friends)
        modelAndView.addObject("section", "userLookup")
        modelAndView.addObject("page", 1)
        modelAndView.addObject("activeTab", tab.lowercase())
        modelAndView.addObject("allGrants", allGrants)
        modelAndView.addObject("highestRankEver", highestRankEver ?: currentHighestRank)
        modelAndView.addObject("currentHighestRank", currentHighestRank)
        modelAndView.addObject("hadHigherRank", hadHigherRank)
        modelAndView.addObject("rankHistory", rankHistory)
        modelAndView.addObject("lastSeenAgo", lastSeenAgo)

        return modelAndView
    }

    private fun getFriendProfiles(friendUUIDs: List<UUID>): List<GameProfile> {
        return friendUUIDs.mapNotNull { ProfileGameService.byId(it) }
    }
    
    private fun formatDuration(millis: Long): String {
        if (millis <= 0) return "just now"
        
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = weeks / 4
        val years = months / 12
        
        return when {
            years > 0 -> "$years year${if (years > 1) "s" else ""}"
            months > 0 -> "$months month${if (months > 1) "s" else ""}"
            weeks > 0 -> "$weeks week${if (weeks > 1) "s" else ""}"
            days > 0 -> "$days day${if (days > 1) "s" else ""}"
            hours > 0 -> "$hours hour${if (hours > 1) "s" else ""}"
            minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""}"
            else -> "$seconds second${if (seconds > 1) "s" else ""}"
        }
    }
    
    
    data class PunishmentWithTarget(
        val punishment: ltd.matrixstudios.alchemist.models.grant.types.Punishment,
        val targetName: String
    )
    
    data class RankHistoryEntry(
        val rank: ltd.matrixstudios.alchemist.models.ranks.Rank,
        val grantedAt: Long,
        val isActive: Boolean,
        val grant: ltd.matrixstudios.alchemist.models.grant.types.RankGrant?
    )
}
