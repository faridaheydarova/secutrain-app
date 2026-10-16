package com.securityawareness.secutrainapp.service;

import com.securityawareness.secutrainapp.dto.authDTOs.JwtAuthenticationResponse;
import com.securityawareness.secutrainapp.dto.authDTOs.LoginRequest;
import com.securityawareness.secutrainapp.dto.authDTOs.RefreshTokenRequest;
import com.securityawareness.secutrainapp.dto.authDTOs.RegisterRequest;
import com.securityawareness.secutrainapp.model.Role;
import com.securityawareness.secutrainapp.model.User;
import com.securityawareness.secutrainapp.repository.UserRepository;
import com.securityawareness.secutrainapp.security.jwt.JwtTokenProvider;
import com.securityawareness.secutrainapp.security.jwt.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public JwtAuthenticationResponse register(RegisterRequest request){
        if(!request.getPassword().equals(request.getConfirmPassword())){
            throw new RuntimeException("passwords dont match");
        }
        if(userRepository.existsByUsername(request.getUsername())){
            throw new RuntimeException("username already exists");
        }
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("email already exists");
        }
        User user= User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.USER)
                .isActive(true)
                .build();
        userRepository.save(user);
        log.info("new user registered:{}",user.getUsername());

        String accessToken=jwtTokenProvider.generateAccessTokenFromUsername(user.getUsername(),user.getId());
        String refreshToken=jwtTokenProvider.generateRefreshToken(user.getUsername(),user.getId());
        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresIn(86400000L)
                .build();
    }

    @Transactional
    public JwtAuthenticationResponse login(LoginRequest request){
        Authentication authentication=authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                ));
        User user =userRepository.findByUsername(request.getUsername())
                .orElseThrow(()-> new RuntimeException("user not found"));
        userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername(), user.getId());

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        log.info("user logged in: {}", user.getUsername());

        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(userDetails.getUserId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .role(userDetails.getAuthorities().stream()
                        .findFirst()
                        .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                        .orElse("USER"))
                .expiresIn(86400000L)
                .build();
    }
    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtTokenProvider.generateAccessTokenFromUsername(username, userId);

        return JwtAuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .userId(userId)
                .username(username)
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresIn(86400000L)
                .build();
    }
    }


