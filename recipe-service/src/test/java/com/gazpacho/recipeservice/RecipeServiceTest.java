package com.gazpacho.recipeservice;

import com.gazpacho.recipeservice.repository.AllergenRepository;
import com.gazpacho.recipeservice.repository.IngredientRepository;
import com.gazpacho.sharedlib.dto.RecipeDTO;
import com.gazpacho.recipeservice.model.IngredientEntity;
import com.gazpacho.recipeservice.model.RecipeEntity;
import com.gazpacho.recipeservice.model.AllergenEntity;
import com.gazpacho.recipeservice.repository.RecipeRepository;
import com.gazpacho.recipeservice.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RecipeServiceTest {

  private RecipeRepository recipeRepository;
  private AllergenRepository allergenRepository;
  private IngredientRepository ingredientRepository;
  private RecipeService recipeService;

  @BeforeEach
  void setUp() {
    recipeRepository = mock(RecipeRepository.class);
    allergenRepository = mock(AllergenRepository.class);
    ingredientRepository = mock(IngredientRepository.class);
    recipeService = new RecipeService(recipeRepository, allergenRepository, ingredientRepository);
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
  }

    @Test
    void testViewRecipe_NotFound() {
        long testID = 1L;
        when(recipeRepository.findById(testID)).thenReturn(Optional.empty());
        
        Optional<RecipeEntity> result = recipeService.viewRecipe(testID);
        assertFalse(result.isPresent());
    }

    @Test
    void testSearchRecipes_RecipeType() {
        long testID = 1L;
        RecipeEntity recipe = new RecipeEntity();
        recipe.setId(testID);
        recipe.setName("Spaghetti");
        when(recipeRepository.findByNameContainingIgnoreCase("spa"))
                .thenReturn(Collections.singletonList(recipe));

        List<RecipeDTO> results = recipeService.searchRecipes("spa", "recipe");
        assertEquals(1, results.size());
        assertEquals("Spaghetti", results.get(0).getName());
    }

    @Test
    void testSearchRecipes_IngredientType() {
        //simulating a recipe with an ingredient whose name contains the query
        long testID = 2L;
        RecipeEntity recipe = new RecipeEntity();
        recipe.setId(testID);
        recipe.setName("Mac & Cheese");

        IngredientEntity ingredient = new IngredientEntity();
        ingredient.setId(10L);
        ingredient.setName("Cheddar Cheese");
        recipe.getIngredients().add(ingredient);

        when(recipeRepository.findAll()).thenReturn(Collections.singletonList(recipe));

        List<RecipeDTO> results = recipeService.searchRecipes("cheese", "ingredient");
        assertEquals(1, results.size());
        assertEquals("Mac & Cheese", results.get(0).getName());
    }

    @Test
    void testSearchRecipes_AllergenType() {
        //simulate a recipe with an ingredient that has an allergen join matching the query
        long testID = 3L;
        RecipeEntity recipe = new RecipeEntity();
        recipe.setId(testID);
        recipe.setName("Peanut Pie");

        IngredientEntity ingredient = new IngredientEntity();
        ingredient.setId(20L);
        ingredient.setName("All-Purpose Flour");

        AllergenEntity allergen = new AllergenEntity();
        allergen.setId(100L);
        allergen.setName("Peanuts");

        ingredient.getAllergens().add(allergen);
        recipe.getIngredients().add(ingredient);

        when(recipeRepository.findAll()).thenReturn(Collections.singletonList(recipe));

        List<RecipeDTO> results = recipeService.searchRecipes("peanut", "allergen");
        assertEquals(1, results.size());
        assertEquals("Peanut Pie", results.get(0).getName());
    }

    @Test
    void testSearchRecipes_UnknownTypeFallsBack() {
        //check for unknown type
        long testID = 4L;
        RecipeEntity recipe = new RecipeEntity();
        recipe.setId(testID);
        recipe.setName("Chicken Alfredo");
        when(recipeRepository.findByNameContainingIgnoreCase("alfredo"))
                .thenReturn(Collections.singletonList(recipe));

        List<RecipeDTO> results = recipeService.searchRecipes("alfredo", "unknown");
        assertEquals(1, results.size());
        assertEquals("Chicken Alfredo", results.get(0).getName());
    }
    
    @Test
    void testSearchRecipes_DefaultSearch() {
        //check that the overloaded default is be used here
        long testID = 1L;
        RecipeEntity recipe = new RecipeEntity();
        recipe.setId(testID);
        recipe.setName("Spaghetti");

        when(recipeRepository.findByNameContainingIgnoreCase("spa"))
                .thenReturn(Collections.singletonList(recipe));

    List<RecipeDTO> results = recipeService.searchRecipes("spa");
    assertEquals(1, results.size());
    assertEquals("Spaghetti", results.get(0).getName());
  }
}
