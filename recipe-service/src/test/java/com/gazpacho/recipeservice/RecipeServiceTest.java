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
        long testID = 1L;
        RecipeEntity recipe = new RecipeEntity();
        recipe.setId(testID);
        recipe.setName("Spaghetti");

        when(recipeRepository.findById(testID)).thenReturn(Optional.of(recipe));

        Optional<RecipeEntity> result = recipeService.viewRecipe(testID);
        assertTrue(result.isPresent());
        assertEquals("Spaghetti", result.get().getName());
        Optional<RecipeEntity> result = recipeService.viewRecipe(testID);
        assertTrue(result.isPresent());
        assertEquals("Spaghetti", result.get().getName());
    }

    @Test
    void testViewRecipe_NotFound() {
        long testID = 1L;
        when(recipeRepository.findById(testID)).thenReturn(Optional.empty());
        
        Optional<RecipeEntity> result = recipeService.viewRecipe(testID);
        assertFalse(result.isPresent());
    }

    @Test
    void testSearchRecipes() {
        long testID = 1L;
        RecipeEntity recipe1 = new RecipeEntity();
        recipe1.setId(testID);
        recipe1.setName("Spaghetti");

        long testID2 = 2L;
        RecipeEntity recipe2 = new RecipeEntity();
        recipe2.setId(testID2);
        recipe2.setName("Pizza");

        when(recipeRepository.findAll()).thenReturn(Arrays.asList(recipe1, recipe2));

        List<RecipeDTO> results = recipeService.searchRecipes("spa");
        assertEquals(1, results.size());
        assertEquals("Spaghetti", results.get(0).getName());
    }
}
