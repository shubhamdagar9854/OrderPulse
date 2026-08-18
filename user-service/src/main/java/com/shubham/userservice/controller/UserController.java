package com.shubham.userservice.controller;

import com.shubham.userservice.dto.*;
import com.shubham.userservice.exception.ForbiddenException;
import com.shubham.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getProfile(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateProfile(id, request));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody UpdateRoleRequest request) {
        UserResponse user = userService.updateRole(id, request.getRole());
        return ResponseEntity.ok(Map.of("message", "Role updated to " + request.getRole(), "user", user));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Role", required = false) String xRole) {
        if (!"ADMIN".equals(xRole) && !String.valueOf(id).equals(xUserId)) {
            throw new ForbiddenException("You do not have permission to change this user's password");
        }
        boolean adminReset = "ADMIN".equals(xRole) && !String.valueOf(id).equals(xUserId);
        userService.changePassword(id, request, adminReset);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }
}