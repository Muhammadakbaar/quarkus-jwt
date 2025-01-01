package org.acme.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.dto.request.LoginRequestDTO;
import org.acme.dto.request.RegisterRequestDTO;
import org.acme.dto.response.ApiResponse;
import org.acme.service.AuthService;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/register")
    public Uni<Response> register(@Valid RegisterRequestDTO registerRequestDTO) {
        return authService.register(registerRequestDTO)
                .onItem().transform(apiResponse -> Response.status(Response.Status.CREATED).entity(apiResponse).build());
    }

    @POST
    @Path("/login")
    public Uni<Response> login(@Valid LoginRequestDTO loginRequestDTO) {
        return authService.login(loginRequestDTO)
                .onItem().transform(apiResponse -> Response.ok(apiResponse).build())
                .onFailure().recoverWithItem(failure -> Response.status(Response.Status.UNAUTHORIZED).entity(new ApiResponse("error", "Login failed", null)).build());
    }

    @POST
    @Path("/refresh")
    public Uni<Response> refreshToken(@QueryParam("refreshToken") String refreshToken) {
        return authService.refreshToken(refreshToken)
                .onItem().transform(response -> Response.ok(response).build())
                .onFailure().recoverWithItem(failure -> Response.status(Response.Status.UNAUTHORIZED).build());
    }
}
