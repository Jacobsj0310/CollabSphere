package com.CollabSphere.CollabSphere.UI;

import com.CollabSphere.CollabSphere.DTO.AuthRequestDTO;
import com.CollabSphere.CollabSphere.DTO.AuthResponseDTO;
import com.CollabSphere.CollabSphere.DTO.RegisterRequestDTO;
import com.CollabSphere.CollabSphere.Interface.AuthServiceInterface;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthUIController {

    public static final String SESSION_TOKEN = "AUTH_TOKEN";
    public static final String SESSION_EMAIL = "AUTH_EMAIL";

    private final AuthServiceInterface authService;

    @GetMapping({"/", "/login"})
    public String loginForm(Model model) {
        model.addAttribute("authRequest", new AuthRequestDTO());
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@Valid @ModelAttribute("authRequest") AuthRequestDTO request,
                          BindingResult br,
                          HttpSession session,
                          Model model) {
        if (br.hasErrors()) return "login";
        try {
            AuthResponseDTO resp = authService.login(request);
            session.setAttribute(SESSION_TOKEN, resp.getToken());
            session.setAttribute(SESSION_EMAIL, resp.getEmail());
            return "redirect:/dashboard";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequestDTO());
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@Valid @ModelAttribute("registerRequest") RegisterRequestDTO request,
                             BindingResult br,
                             HttpSession session,
                             Model model) {
        if (br.hasErrors()) return "register";
        try {
            AuthResponseDTO resp = authService.register(request);
            session.setAttribute(SESSION_TOKEN, resp.getToken());
            session.setAttribute(SESSION_EMAIL, resp.getEmail());
            return "redirect:/dashboard";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}