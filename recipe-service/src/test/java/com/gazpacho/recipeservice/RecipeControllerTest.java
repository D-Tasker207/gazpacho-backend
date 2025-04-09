package com.gazpacho.recipeservice;

import com.gazpacho.recipeservice.controller.RecipeController;
import com.gazpacho.sharedlib.dto.RecipeDTO;
import com.gazpacho.recipeservice.model.RecipeEntity;
import com.gazpacho.recipeservice.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Optional;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecipeController.class)
public class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecipeService recipeService;

    @Test
    void testViewRecipe() throws Exception {
        long testID = 1L;
        RecipeEntity recipe = new RecipeEntity();
        recipe.setId(testID);
        recipe.setName("Spaghetti");
    
        // Return Optional.of(recipe)
        Mockito.when(recipeService.viewRecipe(testID)).thenReturn(Optional.of(recipe));
    
        mockMvc.perform(get("/recipes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Spaghetti"))
                .andExpect(jsonPath("$.id").value((int)testID));
    }
    
    @Test
    void testSearchRecipes() throws Exception {
        long testID = 1L;
        long testID2 = 2L;
        RecipeDTO recipe1 = new RecipeDTO(testID, "Spaghetti");
        RecipeDTO recipe2 = new RecipeDTO(testID2, "Spaghetti Bolognese");
        List<RecipeDTO> recipes = Arrays.asList(recipe1, recipe2);

        Mockito.when(recipeService.searchRecipes("spaghetti")).thenReturn(recipes);

        mockMvc.perform(get("/recipes/search?q=spaghetti"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Spaghetti"))
                .andExpect(jsonPath("$[1].name").value("Spaghetti Bolognese"));
    }

    //Recipe Delete Tests:
    @Test
    void testDeleteRecipe_Success() throws Exception {
        long testID = 1L;
        // Simulate successful deletion
        doNothing().when(recipeService).deleteRecipe(testID);

        mockMvc.perform(delete("/recipes/" + testID))
                .andExpect(status().isOk())
                .andExpect(content().string("Recipe deleted successfully"));
    }

    @Test
    void testDeleteRecipe_NotFound() throws Exception {
        long testID = 1L;
        //Simulate not found
        doThrow(new RuntimeException("Recipe not found")).when(recipeService).deleteRecipe(testID);

        mockMvc.perform(delete("/recipes/" + testID))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Recipe not found"));
    }

    @Test
    void testDeleteRecipe_UnexpectedException() throws Exception {
        long testID = 2L;
        //boundary case: Try throwing an unexpected exception instead of a not found exception
        doThrow(new RuntimeException("Unexpected error")).when(recipeService).deleteRecipe(testID);

        mockMvc.perform(delete("/recipes/" + testID))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Unexpected error"));
    }
}
