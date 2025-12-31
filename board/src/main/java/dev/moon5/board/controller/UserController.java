package dev.moon5.board.controller;

import dev.moon5.board.dto.UserRegisterDto;
import dev.moon5.board.dto.UserUpdateDto;
import dev.moon5.board.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    public String list(Model model) {
        log.info("UserController :: list()");

        model.addAttribute("users", userService.findAll(PageRequest.of(0, 10)));

        return "users/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        log.info("UserController :: createForm()");

        model.addAttribute("mode", "create");

        return "users/form";
    }

    @GetMapping("/{id}")
    public String editForm(@PathVariable long id, Model model) {
        log.info("UserController :: editForm() :: id = {}", id);

        model.addAttribute("mode", "edit");
        model.addAttribute("user", userService.findById(id));

        return "users/form";
    }

    @PostMapping
    public String createUser(UserRegisterDto dto, Model model) {
        log.info("UserController :: createUser() :: dto = {}", dto);

        try {
            userService.register(dto);

            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("mode", "create");
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("form", dto);

            return "users/form";
        }
    }

    @PostMapping("/{id}")
    public String updateUser(@PathVariable long id, UserUpdateDto dto, Model model) {
        log.info("UserController :: updateUser() :: dto = {}", dto);

        userService.updateUser(id, dto);

        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable long id, Model model) {
        log.info("UserController :: deleteUser() :: id = {}", id);

        userService.deleteUser(id);

        return "redirect:/admin/users";
    }

}
