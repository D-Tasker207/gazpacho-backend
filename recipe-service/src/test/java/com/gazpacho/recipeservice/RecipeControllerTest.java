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
    
        Mockito.when(recipeService.viewRecipe(testID)).thenReturn(recipe);
    
        mockMvc.perform(get("/recipes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Spaghetti"))
                .andExpect(jsonPath("$.id").value(testID));
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
}
