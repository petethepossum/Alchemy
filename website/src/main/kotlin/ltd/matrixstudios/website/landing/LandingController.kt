package ltd.matrixstudios.website.landing

import ltd.matrixstudios.alchemist.models.website.AlchemistUser
import ltd.matrixstudios.alchemist.redis.RedisOnlineStatusService.getOnlinePlayersCount
import ltd.matrixstudios.alchemist.service.expirable.RankGrantService
import ltd.matrixstudios.alchemist.service.server.UniqueServerService
import ltd.matrixstudios.website.ranks.RankRepository
import ltd.matrixstudios.website.user.loader.UserServicesComponent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest

/**
 * Class created on 11/24/2023

 * @author 98ping
 * @project Alchemist
 * @website https://solo.to/redis
 */
@Controller
class LandingController @Autowired constructor(private val rankRepository: RankRepository) {

    @RequestMapping(value = ["/", "/home"], method = [RequestMethod.GET])
    fun onLandRequest(): ModelAndView = ModelAndView("login")

    @RequestMapping(value = ["/dashboard", "/panel"], method = [RequestMethod.GET])
    fun onDashboardRequest(request: HttpServletRequest) : ModelAndView
    {
        val modelAndView = ModelAndView("home")
        val profile = request.session.getAttribute("user") as AlchemistUser? ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "You must be logged in to view this page")

        val grantCount = try {
            RankGrantService.handler.retrieveAll().size
        } catch (e: Exception) {
            0
        }

        val rankSize = rankRepository.findAll().size
        val userCount = getOnlinePlayersCount()
        val serverCount = UniqueServerService.getValues().size

        modelAndView.addObject("user", profile)
        modelAndView.addObject("rankSize", rankSize)
        modelAndView.addObject("userCount", userCount)
        modelAndView.addObject("serverCount", serverCount)
        modelAndView.addObject("grantCount", grantCount)

        return modelAndView
    }
}