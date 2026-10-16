package com.securityawareness.secutrainapp.controller;

import com.securityawareness.secutrainapp.dto.userManagementDTOs.CreateUserRequest;
import com.securityawareness.secutrainapp.dto.userManagementDTOs.UpdateUserRequest;
import com.securityawareness.secutrainapp.dto.userManagementDTOs.UserDTO;
import com.securityawareness.secutrainapp.model.Role;
import com.securityawareness.secutrainapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j

public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {

        log.info("Fetching all users - page: {}, size: {}", page, size);
        Page<UserDTO> users = userService.getAllUsers(page, size);

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "User ID")
            @PathVariable Long id) {

        log.info("Fetching user with ID: {}", id);
        UserDTO user = userService.getUserById(id);

        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser( @Valid @RequestBody CreateUserRequest request) {

        log.info("Creating new user: {}", request.getUsername());
        UserDTO createdUser = userService.createUser(toUserDTO(request));

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User created successfully");
        response.put("user", createdUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @Parameter(description = "User ID")
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        log.info("Updating user with ID: {}", id);
        UserDTO updatedUser = userService.updateUser(id, toUserDTO(request));

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User updated successfully");
        response.put("user", updatedUser);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @Parameter(description = "User ID")
            @PathVariable Long id) {

        log.info("Deleting user with ID: {}", id);
        userService.deleteUser(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted successfully");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<Map<String, Object>> changeUserRole(
            @Parameter(description = "User ID")
            @PathVariable Long id,
            @RequestParam String newRole) {

        log.info("Changing user role for ID: {} to: {}", id, newRole);
        UserDTO updatedUser = userService.changeUserRole(id, Role.valueOf(newRole));

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User role changed successfully");
        response.put("user", updatedUser);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")

    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @Parameter(description = "User ID")
            @PathVariable Long id,
            @RequestParam Boolean isActive) {

        log.info("Updating user status for ID: {} to: {}", id, isActive);
        userService.activateDeactivateUser(id, isActive);

        Map<String, Object> response = new HashMap<>();
        response.put("message", isActive ? "User activated" : "User deactivated");
        response.put("userId", id);
        response.put("isActive", isActive);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/role/{role}")

    public ResponseEntity<java.util.List<UserDTO>> getUsersByRole(
            @Parameter(description = "Role: ADMIN or USER")
            @PathVariable Role role) {

        log.info("Fetching users by role: {}", role);
        java.util.List<UserDTO> users = userService.getUsersByRole(role);

        return ResponseEntity.ok(users);
    }

    @GetMapping("/active/all")
    public ResponseEntity<java.util.List<UserDTO>> getActiveUsers() {

        log.info("Fetching all active users");
        java.util.List<UserDTO> users = userService.getActiveUsers();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    public ResponseEntity<java.util.List<UserDTO>> searchUsers(
            @Parameter(description = "Search term")
            @RequestParam String searchTerm) {

        log.info("Searching users with term: {}", searchTerm);
        java.util.List<UserDTO> results = userService.getActiveUsers();

        return ResponseEntity.ok(results);
    }

    @GetMapping("/stats/overview")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {

        log.info("Fetching user statistics");

        long totalUsers = 0;
        long activeUsers = userService.getActiveUsers().size();
        long adminUsers = userService.getUsersByRole(Role.valueOf("ADMIN")).size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("adminUsers", adminUsers);
        stats.put("regularUsers", totalUsers - adminUsers);

        return ResponseEntity.ok(stats);
    }

    private UserDTO toUserDTO(CreateUserRequest request) {
        return UserDTO.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole() != null ? request.getRole() : "USER")
                .build();
    }

    private UserDTO toUserDTO(UpdateUserRequest request) {
        return UserDTO.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();
    }
}