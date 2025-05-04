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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;


@DisplayName("RecipeService Unit Tests")
class RecipeServiceTest {

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

    @Nested
    @DisplayName("viewRecipe()")
    class ViewRecipe {
        @Test @DisplayName("returns Optional.of(entity) when found")
        void found() {
            RecipeEntity e = new RecipeEntity();
            e.setId(1L);
            e.setName("Soup");
            given(recipeRepository.findById(1L)).willReturn(Optional.of(e));
            Optional<RecipeEntity> result = recipeService.viewRecipe(1L);
            assertTrue(result.isPresent());
            assertEquals("Soup", result.get().getName());
        }

        @Test @DisplayName("returns Optional.empty() when missing")
        void missing() {
            given(recipeRepository.findById(2L)).willReturn(Optional.empty());
            Optional<RecipeEntity> result = recipeService.viewRecipe(2L);
            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("deleteRecipe()")
    class DeleteRecipe {
        @Test @DisplayName("deletes when repo.existsById() is true")
        void deleteWhenPresent() {
            given(recipeRepository.existsById(10L)).willReturn(true);
            // should not throw
            recipeService.deleteRecipe(10L);
            then(recipeRepository).should().deleteById(10L);
        }

        @Test @DisplayName("throws RuntimeException(\"Recipe not found\") when missing")
        void throwsWhenMissing() {
            given(recipeRepository.existsById(20L)).willReturn(false);
            RuntimeException ex = assertThrows(RuntimeException.class, () -> recipeService.deleteRecipe(20L));
            assertEquals("Recipe not found", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("searchRecipes(query, type)")
    class SearchByType {
        @Test @DisplayName("type=\"recipe\" uses findByNameContainingIgnoreCase")
        void recipeType() {
            RecipeEntity e = new RecipeEntity();
            e.setId(1L);
            e.setName("Pie");
            given(recipeRepository.findByNameContainingIgnoreCase("pi")).willReturn(List.of(e));

            List<RecipeDTO> out = recipeService.searchRecipes("pi", "recipe");
            assertEquals(1, out.size());
            assertEquals(1L, out.get(0).getId());
            assertEquals("Pie", out.get(0).getName());
        }

        @Test @DisplayName("type=\"ingredient\" filters findAll() by ingredient.name")
        void ingredientType() {
            RecipeEntity r = new RecipeEntity();
            r.setId(2L);
            r.setName("Mac & Cheese");
            IngredientEntity ing = new IngredientEntity();
            ing.setName("Cheddar Cheese");
            r.getIngredients().add(ing);
            given(recipeRepository.findAll()).willReturn(List.of(r));

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
            List<RecipeDTO> out = recipeService.searchRecipes("cheese", "ingredient");
            assertEquals(1, out.size());
            assertEquals("Cheesy Pasta", out.get(0).getName());
        }

        @Test @DisplayName("type=\"ingredient\" yields empty when none match")
        void ingredientTypeNoMatch() {
            given(recipeRepository.findAll()).willReturn(List.of());
            List<RecipeDTO> out = recipeService.searchRecipes("any", "ingredient");
            assertTrue(out.isEmpty());
        }

        @Test @DisplayName("type=\"allergen\" filters findAll() by allergen.name")
        void allergenType() {
            RecipeEntity r = new RecipeEntity();
            r.setId(3L);
            r.setName("Nutty Cake");
            IngredientEntity ing = new IngredientEntity();
            AllergenEntity allergen = new AllergenEntity();
            allergen.setName("Peanut");
            ing.getAllergens().add(allergen);
            r.getIngredients().add(ing);

            given(recipeRepository.findAll()).willReturn(List.of(recipe));

            List<RecipeDTO> out = recipeService.searchRecipes("peanut", "allergen");
            assertEquals(1, out.size());
            assertEquals("Nutty Cake", out.get(0).getName());
        }

        @Test @DisplayName("type=\"allergen\" yields empty when none match")
        void allergenTypeNoMatch() {
            given(recipeRepository.findAll()).willReturn(List.of());
            List<RecipeDTO> out = recipeService.searchRecipes("none", "allergen");
            assertTrue(out.isEmpty());
        }

        @Test @DisplayName("unknown type falls back to recipe search")
        void unknownTypeFallback() {
            RecipeEntity e = new RecipeEntity();
            e.setId(4L);
            e.setName("Fallback Stew");
            given(recipeRepository.findByNameContainingIgnoreCase("foo"))
                .willReturn(List.of(e));

            List<RecipeDTO> out = recipeService.searchRecipes("foo", "whoknows");
            assertEquals(1, out.size());
            assertEquals("Fallback Stew", out.get(0).getName());
        }
    }

    @Nested
    @DisplayName("searchRecipes(query) — overloaded default")
    class DefaultSearch {
        @Test @DisplayName("delegates to searchRecipes(query, \"recipe\")")
        void defaultDelegates() {
            RecipeEntity e = new RecipeEntity();
            e.setId(5L);
            e.setName("Default Dish");
            // stub the two-arg method
            given(recipeRepository.findByNameContainingIgnoreCase("def"))
                .willReturn(List.of(e));

            // call the one-arg overload
            List<RecipeDTO> out = recipeService.searchRecipes("def");
            assertEquals(1, out.size());
            assertEquals(5L, out.get(0).getId());
        }

        @Test @DisplayName("returns empty list when no matches for default search")
        void defaultEmpty() {
            given(recipeRepository.findByNameContainingIgnoreCase("missing"))
                .willReturn(Collections.emptyList());
            List<RecipeDTO> out = recipeService.searchRecipes("missing");
            assertTrue(out.isEmpty());
        }
    }
}
}