package org.acme.mapper;

import org.acme.dto.request.UserRequestDTO;
import org.acme.dto.response.UserResponseDTO;
import org.acme.entity.User;

public class UserMapper {
    public static UserResponseDTO toResponseDTO(User user) {
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(user.id);
        userResponseDTO.setName(user.getName());
        userResponseDTO.setEmail(user.getEmail());
        userResponseDTO.setRoles(user.getRoles()); // Set roles sebagai Set<String>
        return userResponseDTO;
    }

    public static User toEntity(UserRequestDTO userRequestDTO) {
        User user = new User();
        user.setName(userRequestDTO.getName());
        user.setEmail(userRequestDTO.getEmail());
        user.setRoles(userRequestDTO.getRoles()); // Set roles sebagai Set<String>
        return user;
    }
}