package dev.moon5.dynamicforms.forms.category;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/category")
public class CategoryController {

	private final CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping("")
	public String list(Model model) {
		model.addAttribute("categories", categoryService.getTreeOrdered());
		return "admin/category/list";
	}

	@GetMapping("/new")
	public String newForm(@RequestParam(required = false) Long parentId, Model model) {
		model.addAttribute("parentId", parentId);
		model.addAttribute("categories", categoryService.getTreeOrdered());
		return "admin/category/form";
	}

	@PostMapping("")
	public String create(
			@RequestParam(required = false) Long parentId,
			@RequestParam String name,
			@RequestParam(required = false) Integer sortOrder,
			Model model) {
		try {
			categoryService.create(parentId, name, sortOrder);
		} catch (IllegalArgumentException | IllegalStateException e) {
			model.addAttribute("error", e.getMessage());
			model.addAttribute("parentId", parentId);
			model.addAttribute("categories", categoryService.getTreeOrdered());
			return "admin/category/form";
		}
		return "redirect:/admin/category";
	}

	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		model.addAttribute("category", categoryService.getById(id));
		return "admin/category/edit";
	}

	@PostMapping("/{id}")
	public String update(
			@PathVariable Long id,
			@RequestParam String name,
			@RequestParam(required = false) Integer sortOrder,
			Model model) {
		try {
			categoryService.update(id, name, sortOrder);
		} catch (IllegalArgumentException e) {
			model.addAttribute("error", e.getMessage());
			model.addAttribute("category", categoryService.getById(id));
			return "admin/category/edit";
		}
		return "redirect:/admin/category";
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, Model model) {
		try {
			categoryService.delete(id);
		} catch (IllegalStateException e) {
			model.addAttribute("categories", categoryService.getTreeOrdered());
			model.addAttribute("error", e.getMessage());
			return "admin/category/list";
		}
		return "redirect:/admin/category";
	}
}
