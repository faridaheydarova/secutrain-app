package com.securityawareness.secutrainapp.security.ad;

import com.securityawareness.secutrainapp.model.Role;
import com.securityawareness.secutrainapp.model.User;
import com.securityawareness.secutrainapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdJitProvisioningService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void provision(String username, String email, String displayName, String[] memberOf) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }
        Role role = mapRole(memberOf);
        String name = (displayName != null) ? displayName : username;

        User user = User.builder()
                .username(username)
                .email((email != null) ? email : username + "@corp.lab")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .firstName(name)
                .lastName("AD")
                .role(role)
                .isActive(true)
                .build();

        userRepository.save(user);
        log.info("AD user JIT-provisioned: {} -> {}", username, role);
    }

    private Role mapRole(String[] memberOf) {
        if (memberOf != null) {
            for (String dn : memberOf) {
                if (dn.toLowerCase().contains("platform-admins")) {
                    return Role.ADMIN;
                }
            }
        }
        return Role.USER;
    }
}