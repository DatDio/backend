package com.mailshop_dragonvu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private String avatarUrl;
    private String authProvider;
    private Boolean emailVerified;
    private Set<String> roles;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
