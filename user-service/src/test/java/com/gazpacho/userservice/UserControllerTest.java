package com.gazpacho.userservice;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.Optional;

import com.gazpacho.sharedlib.dto.LoginDTO;
import com.gazpacho.sharedlib.dto.PublicUserDTO;
import com.gazpacho.sharedlib.dto.RefreshRequestDTO;
import com.gazpacho.sharedlib.dto.TokenResponseDTO;
import com.gazpacho.userservice.controller.UserController;
import com.gazpacho.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService userService;

  @Test
  void testRegisterUser() throws Exception {
    when(userService.registerUser(any(LoginDTO.class))).thenReturn(null);

    mockMvc
        .perform(post("/users/register")
            .contentType("application/json")
            .content("{\"email\":\"test@example.com\",\"password\":"
                + "\"password123\"}"))
        .andExpect(status().isCreated());
  }

  @Test
  void testRegisterUser_InvalidInput400() throws Exception {
    // Here the email is invalid and the password is too short.
    mockMvc
        .perform(
            post("/users/register")
                .contentType("application/json")
                .content(
                    "{\"email\": \"invalid-email\", \"password\": \"short\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testLoginUser() throws Exception {
    when(userService.loginUser(any(LoginDTO.class)))
        .thenReturn(Optional
            .of(TokenResponseDTO.builder()
                .accessToken("jtw-access-token-abc123")
                .refreshToken("jtw-refresh-token-abc123")
                .build()));

    mockMvc
        .perform(post("/users/login")
            .contentType("application/json")
            .content("{\"email\":\"existingUser@example.com\", " +
                "\"password\": \"password123\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void testLoginUser_IncorrectPassword401() throws Exception {
    when(userService.loginUser(any(LoginDTO.class))).thenReturn(Optional.empty());

    mockMvc
        .perform(post("/users/login")
            .contentType("application/json")
            .content("{\"email\":\"existingUser@example.com\", " +
                "\"password\": \"incorrectPassword\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void testLoginUser_UserNotFound401() throws Exception {
    when(userService.loginUser(any(LoginDTO.class))).thenReturn(Optional.empty());

    mockMvc
        .perform(post("/users/login")
            .contentType("application/json")
            .content("{\"email\":\"nonExistantUser@example.com\", " +
                "\"password\": \"password123\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void testLoginUser_InvalidInput400() throws Exception {
    when(userService.loginUser(any(LoginDTO.class))).thenReturn(Optional.empty());

    mockMvc
        .perform(post("/users/login")
            .contentType("application/json")
            .content("{\"email\":\"notAnEmail\", " +
                "\"password\": \"short\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testRefresh_ValidToken200() throws Exception {
    when(userService.refreshToken(any(RefreshRequestDTO.class)))
        .thenReturn(Optional.of(TokenResponseDTO.builder()
            .accessToken("jtw-access-token-abc123")
            .refreshToken("jtw-refresh-token-abc123")
            .build()));

    mockMvc
        .perform(post("/users/refresh")
            .contentType("application/json")
            .content("{\"refreshToken\":\"valid-refresh-token\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void testRefresh_InvalidToken403() throws Exception {
    when(userService.refreshToken(any(RefreshRequestDTO.class)))
        .thenReturn(Optional.empty());

    mockMvc
        .perform(post("/users/refresh")
            .contentType("application/json")
            .content("{\"refreshToken\":\"invalid-refresh-token\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void testRefresh_InvalidRequestFormat400() throws Exception {
    mockMvc
        .perform(post("/users/refresh")
            .contentType("application/json")
            .content("{\"invalidField\":\"invalid-refresh-token\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testFetchUser_ValidTokenString() throws Exception {
    String validTokenString = "Bearer valid.token.string";
    when(userService.fetchUser(validTokenString))
            .thenReturn(Optional.of(new PublicUserDTO(1L, "testemail@gmail.com", new ArrayList<>())));

    mockMvc.perform(get("/users")
                    .header("Authorization", validTokenString))
            .andExpect(status().isOk());
  }

  @Test
  void testFetchUser_InvalidTokenString() throws Exception {
    String invalidTokenString = "Bearer invalid.token.string";

    mockMvc.perform(get("/users")
                    .header("Authorization", invalidTokenString))
            .andExpect(status().isNotFound());
  }
}
