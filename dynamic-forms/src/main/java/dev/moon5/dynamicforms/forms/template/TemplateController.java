package dev.moon5.dynamicforms.forms.template;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import dev.moon5.dynamicforms.auth.AuthController;
import dev.moon5.dynamicforms.auth.User;
import dev.moon5.dynamicforms.auth.UserMapper;

@Controller
@RequestMapping("/admin/template")
public class TemplateController {

	private final TemplateService templateService;
	private final UserMapper userMapper;

	public TemplateController(TemplateService templateService, UserMapper userMapper) {
		this.templateService = templateService;
		this.userMapper = userMapper;
	}

	@GetMapping("")
	public String list(Model model) {
		model.addAttribute("templates", templateService.getAll());
		model.addAttribute("categoryNames", templateService.getCategoryNameMap());
		return "admin/template/list";
	}

	@GetMapping("/new")
	public String newForm(Model model) {
		model.addAttribute("categories", templateService.getCategoriesForSelect());
		return "admin/template/form";
	}

	@PostMapping("")
	public String create(
			@RequestParam Long categoryId,
			@RequestParam String name,
			@RequestParam String contentHtml,
			HttpSession session,
			Model model) {
		try {
			templateService.create(categoryId, name, contentHtml, currentUserId(session));
		} catch (IllegalArgumentException e) {
			model.addAttribute("error", e.getMessage());
			model.addAttribute("categories", templateService.getCategoriesForSelect());
			return "admin/template/form";
		}
		return "redirect:/admin/template";
	}

	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		model.addAttribute("template", templateService.getById(id));
		model.addAttribute("categories", templateService.getCategoriesForSelect());
		return "admin/template/edit";
	}

	@PostMapping("/{id}")
	public String update(
			@PathVariable Long id,
			@RequestParam Long categoryId,
			@RequestParam String name,
			@RequestParam String contentHtml,
			HttpSession session,
			Model model) {
		try {
			templateService.update(id, categoryId, name, contentHtml, currentUserId(session));
		} catch (IllegalArgumentException e) {
			model.addAttribute("error", e.getMessage());
			model.addAttribute("template", templateService.getById(id));
			model.addAttribute("categories", templateService.getCategoriesForSelect());
			return "admin/template/edit";
		}
		return "redirect:/admin/template";
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id) {
		templateService.delete(id);
		return "redirect:/admin/template";
	}

	private Long currentUserId(HttpSession session) {
		String loginId = (String) session.getAttribute(AuthController.SESSION_LOGIN_ID_KEY);
		User user = userMapper.findByLoginId(loginId);
		return user.getId();
	}
}
