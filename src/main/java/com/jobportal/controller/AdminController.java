package com.jobportal.controller;

import com.jobportal.dto.GenericResponse;
import com.jobportal.model.Admin;
import com.jobportal.model.CompanyProfile;
import com.jobportal.model.Employer;
import com.jobportal.model.enums.ApprovalStatus;
import com.jobportal.service.AdminService;
import com.jobportal.utils.ResponseBuilder;
import com.jobportal.utils.Role;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<GenericResponse<Admin>> create(@RequestBody Admin admin) {
        Admin created = service.create(admin);
        return ResponseBuilder.success(created, "Admin created successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<Admin>> getById(@PathVariable int id) {
        Admin admin = service.findById(id);
        return ResponseBuilder.success(admin, "Admin retrieved successfully");
    }

    @GetMapping
    public ResponseEntity<GenericResponse<List<Admin>>> getAll() {
        List<Admin> admins = service.findAll();
        return ResponseBuilder.success(admins, "All admins retrieved successfully");
    }

    @GetMapping("/employers/pending")
    public ResponseEntity<GenericResponse<List<Employer>>> getPendingEmployers() {
        List<Employer> pending = service.listPendingEmployers();
        return ResponseBuilder.success(pending, "Pending employers retrieved successfully");
    }
    
    @Role("ADMIN")
    @PutMapping("/employer/{id}/status")
    public ResponseEntity<GenericResponse<Employer>> updateEmployerStatus(
            @PathVariable int id,
            @RequestParam ApprovalStatus status) {

        Employer employer = service.approveOrRejectEmployer(id, status);
        return ResponseBuilder.success(employer, "Employer status updated successfully to " + status);
    }
    
  
}
