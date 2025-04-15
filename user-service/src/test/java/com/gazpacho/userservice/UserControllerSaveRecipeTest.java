package com.gazpacho.userservice;

import com.gazpacho.userservice.controller.UserController;
import com.gazpacho.userservice.model.UserEntity;
import com.gazpacho.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.gazpacho.recipeservice.repository.RecipeRepository;
import com.gazpacho.userservice.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@WebMvcTest(UserController.class)
public class UserControllerSaveRecipeTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private RecipeRepository recipeRepository;

    @Test
    void testSaveRecipe_Success() throws Exception {
        doNothing().when(userService).saveRecipeForUser(eq(1L), eq(10L));

        mockMvc.perform(post("/users/1/recipes/10"))
               .andExpect(status().isOk())
               .andExpect(content().string("Recipe saved successfully"));
    }

    @Test
    void testSaveRecipe_UserNotFound() throws Exception {
        doThrow(new RuntimeException("User not found")).when(userService).saveRecipeForUser(eq(999L), eq(10L));

        mockMvc.perform(post("/users/999/recipes/10"))
               .andExpect(status().isBadRequest())
               .andExpect(content().string("User not found"));
    }

    @Test
    void testSaveRecipe_RecipeNotFound() throws Exception {
        doThrow(new RuntimeException("Recipe not found")).when(userService).saveRecipeForUser(eq(1L), eq(999L));

        mockMvc.perform(post("/users/1/recipes/999"))
               .andExpect(status().isBadRequest())
               .andExpect(content().string("Recipe not found"));
    }

    @Test
    void testSaveRecipe_PreventsDuplicateSaves() {
        //TODO: Fix implementation to pass this test
        //create a dummy user with an empty saved recipe list
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail("user@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(recipeRepository.existsById(10L)).thenReturn(true);
    
        //call saveRecipeForUser with the same recipe ID twice
        userService.saveRecipeForUser(1L, 10L);
        userService.saveRecipeForUser(1L, 10L);
    
        //ensure user's savedRecipeIds list still contains only one entry
        assertEquals(1, user.getSavedRecipeIds().size());

        //having trouble verifying this so far...
        verify(userRepository, times(1)).save(user);
    }

    void testGetSavedRecipiesById() {
        // Suppose we add a method getSavedRecipiesById in UserService.
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.getSavedRecipeIds().addAll(Arrays.asList(10L, 20L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        List<Long> savedRecipes = userService.getSavedRecipiesById(1L);
        assertEquals(2, savedRecipes.size());
        assertTrue(savedRecipes.contains(10L));
        assertTrue(savedRecipes.contains(20L));
    }

}
