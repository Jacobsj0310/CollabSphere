package com.CollabSphere.CollabSphere.UI;


import ch.qos.logback.core.model.Model;
import com.CollabSphere.CollabSphere.DTO.TeamDTO;
import com.CollabSphere.CollabSphere.Interface.TeamServiceInterface;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardUIController {

    private final TeamServiceInterface teamService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        String email = (String) session.getAttribute(AuthUIController.SESSION_EMAIL);
        if (email == null) return "redirect:/login";

        // get teams for user (returns TeamDto.TeamResponse list)
        List<TeamDTO.TeamResponse> teams = teamService.listTeamsForUser(email);
        model.addAttributes("teams", teams);
        model.addAttribute("userEmail", email);
        return "dashboard";
    }
}
