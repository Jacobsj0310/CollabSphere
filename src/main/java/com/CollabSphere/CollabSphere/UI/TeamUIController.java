package com.CollabSphere.CollabSphere.UI;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class TeamUIController {

    @GetMapping("/teams")
    public String teamList() {
        return "teams"; // teams.html
    }

    @GetMapping("/teams/create")
    public String createTeamPage() {
        return "create-team"; // create-team.html
    }

    @GetMapping("/teams/{teamId}")
    public String teamDetails(@PathVariable Long teamId) {
        return "team-details"; // team-details.html
    }
}

