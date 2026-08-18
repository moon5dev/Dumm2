package dev.moon5.dynamicforms.auth;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AuthController {

	public static final String SESSION_LOGIN_ID_KEY = "loginId";

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@GetMapping("/login")
	public String loginForm() {
		return "admin/login";
	}

	@PostMapping("/login")
	public String login(
			@RequestParam String loginId,
			@RequestParam String password,
			HttpSession session,
			Model model) {
		User user = authService.authenticate(loginId, password);
		if (user == null) {
			model.addAttribute("error", true);
			return "admin/login";
		}

		session.setAttribute(SESSION_LOGIN_ID_KEY, user.getLoginId());
		return "redirect:/admin";
	}

	@PostMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/admin/login";
	}

	@GetMapping("")
	public String adminHome() {
		return "admin/index";
	}
}
