package dev.moon5.web.controller;

import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/labels")
public class LabelViewController {

    @GetMapping("/list")
    public String list() {
        return "labels/list";
    }

}
