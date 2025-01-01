package org.acme.resource;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.dto.request.UserRequestDTO;
import org.acme.dto.response.UserResponseDTO;
import org.acme.entity.User;
import org.acme.mapper.UserMapper;
import org.acme.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserRepository userRepository;

    @GET
    @RolesAllowed("ADMIN")
    public Uni<List<UserResponseDTO>> getAllUsers() {
        return userRepository.listAll()
                .onItem().transform(users -> users.stream()
                        .map(UserMapper::toResponseDTO)
                        .collect(Collectors.toList()));
    }

    @POST
    public Uni<Response> createUser(@Valid UserRequestDTO userRequestDTO) {
        User user = UserMapper.toEntity(userRequestDTO);
        return userRepository.persist(user)
                .onItem().transform(inserted -> {
                    UserResponseDTO userResponseDTO = UserMapper.toResponseDTO(user);
                    return Response.status(Response.Status.CREATED).entity(userResponseDTO).build();
                });
    }
}
