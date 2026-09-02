package dev.moon5.dynamicforms.forms.workspace;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import dev.moon5.dynamicforms.forms.template.Template;
import dev.moon5.dynamicforms.forms.template.TemplateService;

@Controller
@RequestMapping("/workspace")
public class WorkspaceController {

	private final TemplateService templateService;

	public WorkspaceController(TemplateService templateService) {
		this.templateService = templateService;
	}

	@GetMapping("")
	public String select(
			@RequestParam(required = false) Long categoryId,
			@RequestParam(required = false) List<Long> selected,
			Model model) {
		model.addAttribute("templates", templateService.getAll(categoryId));
		model.addAttribute("categoryNames", templateService.getCategoryNameMap());
		model.addAttribute("categories", templateService.getCategoriesForSelect());
		model.addAttribute("categoryCounts", templateService.getCategoryTemplateCounts());
		model.addAttribute("totalCount", templateService.getAll().size());
		model.addAttribute("selectedCategoryId", categoryId);
		model.addAttribute("selectedIds", selected == null ? Set.of() : new HashSet<>(selected));
		model.addAttribute("selectedIdsCsv", selected == null ? "" :
			selected.stream().map(String::valueOf).collect(Collectors.joining(",")));
		return "workspace/select";
	}

	@GetMapping("/order")
	public String order(@RequestParam(required = false) List<Long> templateId, Model model) {
		if (templateId == null || templateId.isEmpty()) {
			return "redirect:/workspace";
		}
		List<Template> templates = templateId.stream()
			.map(templateService::getById)
			.filter(t -> t != null)
			.toList();
		if (templates.isEmpty()) {
			return "redirect:/workspace";
		}
		if (templates.size() == 1) {
			return "redirect:/workspace/view?templateId=" + templates.get(0).getId();
		}
		model.addAttribute("templates", templates);
		return "workspace/order";
	}

	@GetMapping("/view")
	public String view(@RequestParam(required = false) List<Long> templateId, Model model) {
		if (templateId == null || templateId.isEmpty()) {
			return "redirect:/workspace";
		}
		List<Template> templates = templateId.stream()
			.map(templateService::getForFilling)
			.filter(t -> t != null)
			.toList();
		if (templates.isEmpty()) {
			return "redirect:/workspace";
		}
		model.addAttribute("templates", templates);
		return "workspace/view";
	}

	@PostMapping("/draft")
	@ResponseBody
	public ResponseEntity<Void> saveDraft(@RequestParam Long templateId, @RequestParam String contentHtml) {
		templateService.saveDraft(templateId, contentHtml);
		return ResponseEntity.ok().build();
	}
}
