package com.securityawareness.secutrainapp.dto.userManagementDTOs;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class UserListResponse {
    private java.util.List<UserDTO> users;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
}
