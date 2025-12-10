package com.CollabSphere.CollabSphere.UI;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileUIController {

    @GetMapping("/profile")
    public String profileView() {
        return "profile"; // profile.html
    }
}

