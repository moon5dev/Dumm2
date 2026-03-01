package dev.moon5.board.controller;

import dev.moon5.board.dto.UserCreateDto;
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

        model.addAttribute("users", userService.getAll(PageRequest.of(0, 10)));

        return "users/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        log.info("UserController :: createForm()");

        model.addAttribute("mode", "create");

        return "users/form";
    }

    @GetMapping("/{userId}")
    public String editForm(@PathVariable long userId, Model model) {
        log.info("UserController :: editForm() :: userId = {}", userId);

        model.addAttribute("mode", "edit");
        model.addAttribute("user", userService.get(userId));

        return "users/form";
    }

    @PostMapping
    public String create(UserCreateDto dto, Model model) {
        log.info("UserController :: createUser() :: dto = {}", dto);

        try {
            userService.create(dto);

            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("mode", "create");
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("form", dto);

            return "users/form";
        }
    }

    @PostMapping("/{userId}")
    public String update(@PathVariable long userId, UserUpdateDto dto, Model model) {
        log.info("UserController :: updateUser() :: dto = {}", dto);

        userService.update(userId, dto);

        return "redirect:/admin/users";
    }

    @PostMapping("/{userId}/delete")
    public String delete(@PathVariable long userId, Model model) {
        log.info("UserController :: deleteUser() :: userId = {}", userId);

        userService.delete(userId);

        return "redirect:/admin/users";
    }

}
