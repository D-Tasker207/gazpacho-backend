package com.gazpacho.recipeservice;

import com.gazpacho.recipeservice.controller.RecipeController;
import com.gazpacho.sharedlib.dto.RecipeDTO;
import com.gazpacho.recipeservice.model.RecipeEntity;
import com.gazpacho.recipeservice.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecipeController.class)
public class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecipeService recipeService;

    @Test
    void testViewRecipe() throws Exception {
        RecipeEntity recipe = new RecipeEntity();
        recipe.setId(1);
        recipe.setName("Spaghetti");

        Mockito.when(recipeService.viewRecipe(1)).thenReturn(recipe);

        mockMvc.perform(get("/recipes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Spaghetti"));
    }

    @Test
    void testSearchRecipes() throws Exception {
        RecipeDTO recipe1 = new RecipeDTO(1, "Spaghetti");
        RecipeDTO recipe2 = new RecipeDTO(2, "Spaghetti Bolognese");
        List<RecipeDTO> recipes = Arrays.asList(recipe1, recipe2);

        Mockito.when(recipeService.searchRecipes("spaghetti")).thenReturn(recipes);

        mockMvc.perform(get("/recipes/search?q=spaghetti"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Spaghetti"))
                .andExpect(jsonPath("$[1].name").value("Spaghetti Bolognese"));
    }
}
