package com.example.curriculoia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        // Redireciona a raiz para o index.html servido em /static/
        return "redirect:/static/index.html";
    }
}
