package org.example.new_new_mvp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @GetMapping("/company")
    public String company() {
        return "company";
    }
    
    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }
}
