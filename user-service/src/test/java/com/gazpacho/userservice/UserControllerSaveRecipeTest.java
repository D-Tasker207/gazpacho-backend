package com.gazpacho.userservice;

import com.gazpacho.sharedlib.dto.PublicUserDTO;
import com.gazpacho.userservice.controller.UserController;
import com.gazpacho.userservice.model.UserEntity;
import com.gazpacho.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerSaveRecipeTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    // If you're stubbing at the service layer using mockMvc,
    // these tests mainly check the endpoint response.
    @Test
    void testSaveRecipe_Success() throws Exception {
        doNothing().when(userService).saveRecipeForUser(eq(1L), eq(10L));

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail("testemail@gmail.com");
        user.setPassword("hashedpassword");

        String validTokenString = "Bearer valid.token.string";
        when(userService.fetchUserByToken(validTokenString))
                .thenReturn(Optional.of(user));

        doNothing().when(userService).saveRecipeForUser(eq(1L), eq(10L));

        mockMvc.perform(post("/users/recipes/10").header("Authorization", validTokenString))
               .andExpect(status().isOk())
               .andExpect(content().string("Recipe saved successfully"));
    }

    @Test
    void testSaveRecipe_UserNotFound() throws Exception {
        String invalidTokenString = "Bearer invalid.token.string";
        when(userService.fetchUserByToken(invalidTokenString))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/users/recipes/10").header("Authorization", invalidTokenString))
               .andExpect(status().isUnauthorized());
    }

    /*@Test
    void testSaveRecipe_RecipeNotFound() throws Exception {
        doThrow(new RuntimeException("Recipe not found")).when(userService).saveRecipeForUser(eq(1L), eq(999L));

        mockMvc.perform(post("/users/1/recipes/999"))
               .andExpect(status().isBadRequest())
               .andExpect(content().string("Recipe not found"));
    }*/

}
