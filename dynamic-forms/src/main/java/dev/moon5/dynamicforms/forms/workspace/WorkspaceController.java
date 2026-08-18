package dev.moon5.dynamicforms.forms.workspace;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
	public String select(Model model) {
		model.addAttribute("templates", templateService.getAll());
		model.addAttribute("categoryNames", templateService.getCategoryNameMap());
		return "workspace/select";
	}

	@GetMapping("/view")
	public String view(@RequestParam List<Long> templateId, Model model) {
		List<Template> templates = templateId.stream()
			.map(templateService::getById)
			.filter(t -> t != null)
			.toList();
		model.addAttribute("templates", templates);
		return "workspace/view";
	}
}
