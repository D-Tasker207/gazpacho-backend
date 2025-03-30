package com.gazpacho.recipeservice;

import com.gazpacho.sharedlib.dto.RecipeDTO;
import com.gazpacho.recipeservice.model.RecipeEntity;
import com.gazpacho.recipeservice.repository.RecipeRepository;
import com.gazpacho.recipeservice.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RecipeServiceTest {

    private RecipeRepository recipeRepository;
    private RecipeService recipeService;

    @BeforeEach
    void setUp() {
        recipeRepository = mock(RecipeRepository.class);
        recipeService = new RecipeService(recipeRepository);
    }

    @Test
    void testViewRecipe_Found() {
        RecipeEntity recipe = new RecipeEntity();
        recipe.setId(1);
        recipe.setName("Spaghetti");

        when(recipeRepository.findById(1)).thenReturn(Optional.of(recipe));

        RecipeEntity result = recipeService.viewRecipe(1);
        assertEquals("Spaghetti", result.getName());
    }

    @Test
    void testViewRecipe_NotFound() {
        when(recipeRepository.findById(1)).thenReturn(Optional.empty());
        Exception ex = assertThrows(RuntimeException.class, () -> {
            recipeService.viewRecipe(1);
        });
        assertEquals("Recipe not found", ex.getMessage());
    }

    @Test
    void testSearchRecipes() {
        RecipeEntity recipe1 = new RecipeEntity();
        recipe1.setId(1);
        recipe1.setName("Spaghetti");

        RecipeEntity recipe2 = new RecipeEntity();
        recipe2.setId(2);
        recipe2.setName("Pizza");

        when(recipeRepository.findAll()).thenReturn(Arrays.asList(recipe1, recipe2));

        List<RecipeDTO> results = recipeService.searchRecipes("spa");
        assertEquals(1, results.size());
        assertEquals("Spaghetti", results.get(0).getName());
    }
}
