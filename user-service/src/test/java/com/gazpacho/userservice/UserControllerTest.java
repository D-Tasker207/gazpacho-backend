package com.gazpacho.userservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.gazpacho.sharedlib.dto.LoginDTO;
import com.gazpacho.userservice.controller.UserController;
import com.gazpacho.userservice.service.UserService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(UserController.class)
class UserControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService userService;

  @Test
  void testRegisterUser() throws Exception {
    Mockito.when(userService.registerUser(Mockito.any(LoginDTO.class)))
        .thenReturn(null);

    mockMvc
        .perform(
            post("/users/register")
                .contentType("application/json")
                .content(
                    "{\"email\":\"test@example.com\",\"password\":\"password123\"}"))
        .andExpect(status().isCreated());
  }
}
