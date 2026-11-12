package com.aurelia.backend.controller;

import com.aurelia.backend.dto.DashboardResponse;
import com.aurelia.backend.security.AuthUser;
import com.aurelia.backend.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 🛎️ Personel paneli (resepsiyon + yönetici) — SecurityConfig: /api/admin/**
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DashboardService dashboardService;

    public AdminController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard(@AuthenticationPrincipal Jwt jwt) {
        return dashboardService.getDashboard(AuthUser.from(jwt));
    }
}