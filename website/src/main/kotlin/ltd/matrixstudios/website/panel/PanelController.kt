package ltd.matrixstudios.website.panel

import ltd.matrixstudios.alchemist.models.website.AlchemistUser
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest

@Controller
class PanelController {

//    @GetMapping("/panel")
 //   fun panel(request: HttpServletRequest): ModelAndView {
  //      val user = request.session.getAttribute("user") as AlchemistUser?
   //         ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "You must be logged in to view this page")

    //    val mv = ModelAndView("panel")
     //   mv.addObject("section", "panel")
     //   mv.addObject("user", user)
     //   return mv
 //   }

    @GetMapping("/logout")
    fun logout(request: HttpServletRequest): ModelAndView {
        request.session.removeAttribute("user")
        request.session.invalidate()
        return ModelAndView("redirect:/login")
    }
}
