package org.acme.service;

import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.request.LoginRequestDTO;
import org.acme.dto.request.RegisterRequestDTO;
import org.acme.dto.response.ApiResponse;
import org.acme.dto.response.AuthResponseDTO;
import org.acme.dto.response.UserAuthResponse;
import org.acme.dto.response.UserResponseDTO;
import org.acme.entity.User;
import org.acme.mapper.UserMapper;
import org.acme.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

@ApplicationScoped
public class AuthService {

    @Inject
    UserRepository userRepository;

    @WithTransaction
    public Uni<ApiResponse> register(RegisterRequestDTO registerRequestDTO) {
        User user = new User();
        user.setName(registerRequestDTO.getName());
        user.setEmail(registerRequestDTO.getEmail());
        user.setPassword(BCrypt.hashpw(registerRequestDTO.getPassword(), BCrypt.gensalt()));
        user.setRole(registerRequestDTO.getRole());

        return userRepository.persist(user)
                .onItem().transform(inserted -> {
                    String token = generateToken(user);
                    String refreshToken = generateRefreshToken(user);
                    user.setRefreshToken(refreshToken);
                    UserResponseDTO userResponseDTO = UserMapper.toResponseDTO(user);
                    UserAuthResponse userAuthResponse = new UserAuthResponse(userResponseDTO, token, refreshToken);
                    return new ApiResponse("success", "Registrasi success", userAuthResponse);
                });
    }

    @WithSession
    public Uni<ApiResponse> login(LoginRequestDTO loginRequestDTO) {
        return userRepository.findByEmail(loginRequestDTO.getEmail())
                .onItem().ifNotNull().transformToUni(user -> {
                    if (BCrypt.checkpw(loginRequestDTO.getPassword(), user.getPassword())) {
                        String token = generateToken(user);
                        String refreshToken = generateRefreshToken(user);
                        user.setRefreshToken(refreshToken);
                        UserResponseDTO userResponseDTO = UserMapper.toResponseDTO(user);
                        UserAuthResponse userAuthResponse = new UserAuthResponse(userResponseDTO, token, refreshToken);
                        return Uni.createFrom().item(new ApiResponse("success", "Login success", userAuthResponse));
                    } else {
                        return Uni.createFrom().failure(new IllegalArgumentException("Invalid credentials"));
                    }
                })
                .onItem().ifNull().failWith(() -> new IllegalArgumentException("User not found"));
    }

    public Uni<AuthResponseDTO> refreshToken(String refreshToken) {
        return userRepository.find("refreshToken", refreshToken).firstResult()
                .onItem().ifNotNull().transformToUni(user -> {
                    String newToken = generateToken(user);
                    String newRefreshToken = generateRefreshToken(user);
                    user.setRefreshToken(newRefreshToken);
                    return userRepository.persist(user)
                            .onItem().transform(updated -> createAuthResponse(newToken, newRefreshToken));
                })
                .onItem().ifNull().failWith(() -> new IllegalArgumentException("Invalid refresh token"));
    }

    public String generateToken(User user) {
        return Jwt.issuer("https://example.com/issuer")
                .upn(user.getEmail())
                .groups(user.getRole())
                .expiresIn(3600) // Token valid for 1 hour
                .sign();
    }

    private String generateRefreshToken(User user) {
        return UUID.randomUUID().toString(); // Refresh Token berupa UUID
    }

    private AuthResponseDTO createAuthResponse(String token, String refreshToken) {
        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        return response;
    }
}
