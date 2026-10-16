package com.securityawareness.secutrainapp.dto.userManagementDTOs;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkCreateUsersRequest {
    @NotEmpty(message = "User list cannot be empty")
    private java.util.List<CreateUserRequest> users;
}
