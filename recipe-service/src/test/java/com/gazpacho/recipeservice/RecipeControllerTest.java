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

import java.util.*;

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
    
        Mockito.when(recipeService.viewRecipe(testID)).thenReturn(Optional.of(recipe));
    
        mockMvc.perform(get("/recipes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Spaghetti"))
                .andExpect(jsonPath("$.id").value((int)testID));
    }
    
    @Test
    void testSearchRecipes_DefaultToRecipeType() throws Exception {
        long testID = 1L;
        RecipeDTO recipe1 = new RecipeDTO(testID, "Spaghetti", "https://www.fakeurl.com", new ArrayList<>(), new HashSet<>(), new ArrayList<>(), "");
        RecipeDTO recipe2 = new RecipeDTO(testID+1, "Spaghetti Bolognese", "https://www.fakeurl.com", new ArrayList<>(), new HashSet<>(), new ArrayList<>(), "");
        List<RecipeDTO> recipes = Arrays.asList(recipe1, recipe2);

        Mockito.when(recipeService.searchRecipes("spaghetti", "recipe")).thenReturn(recipes);

        mockMvc.perform(get("/recipes/search?q=spaghetti"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Spaghetti"))
                .andExpect(jsonPath("$[1].name").value("Spaghetti Bolognese"));
    }
    
    @Test
    void testSearchRecipes_ByIngredientType() throws Exception {
        long testID = 3L;
        RecipeDTO recipe = new RecipeDTO(testID, "Mac & Cheese", "https://www.fakeurl.com", new ArrayList<>(), new HashSet<>(), new ArrayList<>(), "");
        Mockito.when(recipeService.searchRecipes("cheese", "ingredient"))
                .thenReturn(Arrays.asList(recipe));

        mockMvc.perform(get("/recipes/search?q=cheese&type=ingredient"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Mac & Cheese"))
                .andExpect(jsonPath("$[0].id").value((int)testID));
    }
    
    @Test
    void testSearchRecipes_ByAllergenType() throws Exception {
        long testID = 4L;
        RecipeDTO recipe = new RecipeDTO(testID, "Peanut Pie", "https://www.fakeurl.com", new ArrayList<>(), new HashSet<>(), new ArrayList<>(), "");
        Mockito.when(recipeService.searchRecipes("peanut", "allergen"))
                .thenReturn(Arrays.asList(recipe));

        mockMvc.perform(get("/recipes/search?q=peanut&type=allergen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Peanut Pie"))
                .andExpect(jsonPath("$[0].id").value((int)testID));
    }
    
    @Test
    void testSearchRecipes_UnknownTypeFallsBack() throws Exception {
        long testID = 5L;
        RecipeDTO recipe = new RecipeDTO(testID, "Chicken Alfredo", "https://www.fakeurl.com", new ArrayList<>(), new HashSet<>(), new ArrayList<>(), "");
        Mockito.when(recipeService.searchRecipes("alfredo", "unknown"))
                .thenReturn(Arrays.asList(recipe));

        mockMvc.perform(get("/recipes/search?q=alfredo&type=unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Chicken Alfredo"))
                .andExpect(jsonPath("$[0].id").value((int)testID));
    }
    
    // -- Delete endpoint tests --
    @Test
    void testDeleteRecipe_Success() throws Exception {
        long testID = 1L;
        doNothing().when(recipeService).deleteRecipe(testID);

        mockMvc.perform(delete("/recipes/" + testID))
                .andExpect(status().isOk())
                .andExpect(content().string("Recipe deleted successfully"));
    }

    @Test
    void testDeleteRecipe_NotFound() throws Exception {
        long testID = 1L;
        doThrow(new RuntimeException("Recipe not found")).when(recipeService).deleteRecipe(testID);

        mockMvc.perform(delete("/recipes/" + testID))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Recipe not found"));
    }

    @Test
    void testDeleteRecipe_UnexpectedException() throws Exception {
        long testID = 2L;
        doThrow(new RuntimeException("Unexpected error")).when(recipeService).deleteRecipe(testID);

        mockMvc.perform(delete("/recipes/" + testID))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Unexpected error"));
    }
}
