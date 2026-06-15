package com.familyfinance.finance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "dashboard";
    }

    @GetMapping("/operations")
    public String operationsPage() {
        return "operations";
    }

    @GetMapping("/family")
    public String familyPage() {
        return "family";
    }

    @GetMapping("/reports")
    public String reportsPage() {
        return "reports";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }
}
