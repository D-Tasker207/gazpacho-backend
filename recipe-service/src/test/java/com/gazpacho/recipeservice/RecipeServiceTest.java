package com.gazpacho.recipeservice;

import com.gazpacho.sharedlib.dto.RecipeDTO;
import com.gazpacho.recipeservice.model.IngredientAllergenEntity;
import com.gazpacho.recipeservice.model.IngredientEntity;
import com.gazpacho.recipeservice.model.RecipeEntity;
import com.gazpacho.recipeservice.model.AllergenEntity;
import com.gazpacho.recipeservice.repository.RecipeRepository;
import com.gazpacho.recipeservice.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.*;


@DisplayName("RecipeService Unit Tests")
class RecipeServiceTest {

    private RecipeRepository repo;
    private RecipeService svc;

    @BeforeEach
    void setup() {
        repo = mock(RecipeRepository.class);
        svc = new RecipeService(repo);
    }

    @Nested
    @DisplayName("viewRecipe()")
    class ViewRecipe {
        @Test @DisplayName("returns Optional.of(entity) when found")
        void found() {
            RecipeEntity e = new RecipeEntity();
            e.setId(1L);
            e.setName("Soup");
            given(repo.findById(1L)).willReturn(Optional.of(e));

            Optional<RecipeEntity> result = svc.viewRecipe(1L);
            assertTrue(result.isPresent());
            assertEquals("Soup", result.get().getName());
        }

        @Test @DisplayName("returns Optional.empty() when missing")
        void missing() {
            given(repo.findById(2L)).willReturn(Optional.empty());
            Optional<RecipeEntity> result = svc.viewRecipe(2L);
            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("deleteRecipe()")
    class DeleteRecipe {
        @Test @DisplayName("deletes when repo.existsById() is true")
        void deleteWhenPresent() {
            given(repo.existsById(10L)).willReturn(true);
            // should not throw
            svc.deleteRecipe(10L);
            then(repo).should().deleteById(10L);
        }

        @Test @DisplayName("throws RuntimeException(\"Recipe not found\") when missing")
        void throwsWhenMissing() {
            given(repo.existsById(20L)).willReturn(false);
            RuntimeException ex = assertThrows(RuntimeException.class, () -> svc.deleteRecipe(20L));
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
            given(repo.findByNameContainingIgnoreCase("pi")).willReturn(List.of(e));

            List<RecipeDTO> out = svc.searchRecipes("pi", "recipe");
            assertEquals(1, out.size());
            assertEquals(1L, out.get(0).getId());
            assertEquals("Pie", out.get(0).getName());
        }

        @Test @DisplayName("type=\"ingredient\" filters findAll() by ingredient.name")
        void ingredientType() {
            RecipeEntity r = new RecipeEntity();
            r.setId(2L);
            r.setName("Cheesy Pasta");
            IngredientEntity ing = new IngredientEntity();
            ing.setName("Cheddar Cheese");
            r.getIngredients().add(ing);
            given(repo.findAll()).willReturn(List.of(r));

            List<RecipeDTO> out = svc.searchRecipes("cheese", "ingredient");
            assertEquals(1, out.size());
            assertEquals("Cheesy Pasta", out.get(0).getName());
        }

        @Test @DisplayName("type=\"ingredient\" yields empty when none match")
        void ingredientTypeNoMatch() {
            given(repo.findAll()).willReturn(List.of());
            List<RecipeDTO> out = svc.searchRecipes("any", "ingredient");
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
            IngredientAllergenEntity join = new IngredientAllergenEntity();
            join.setIngredient(ing);
            join.setAllergen(allergen);
            ing.getIngredientAllergens().add(join);
            r.getIngredients().add(ing);

            given(repo.findAll()).willReturn(List.of(r));

            List<RecipeDTO> out = svc.searchRecipes("peanut", "allergen");
            assertEquals(1, out.size());
            assertEquals("Nutty Cake", out.get(0).getName());
        }

        @Test @DisplayName("type=\"allergen\" yields empty when none match")
        void allergenTypeNoMatch() {
            given(repo.findAll()).willReturn(List.of());
            List<RecipeDTO> out = svc.searchRecipes("none", "allergen");
            assertTrue(out.isEmpty());
        }

        @Test @DisplayName("unknown type falls back to recipe search")
        void unknownTypeFallback() {
            RecipeEntity e = new RecipeEntity();
            e.setId(4L);
            e.setName("Fallback Stew");
            given(repo.findByNameContainingIgnoreCase("foo"))
                .willReturn(List.of(e));

            List<RecipeDTO> out = svc.searchRecipes("foo", "whoknows");
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
            given(repo.findByNameContainingIgnoreCase("def"))
                .willReturn(List.of(e));

            // call the one-arg overload
            List<RecipeDTO> out = svc.searchRecipes("def");
            assertEquals(1, out.size());
            assertEquals(5L, out.get(0).getId());
        }

        @Test @DisplayName("returns empty list when no matches for default search")
        void defaultEmpty() {
            given(repo.findByNameContainingIgnoreCase("missing"))
                .willReturn(Collections.emptyList());
            List<RecipeDTO> out = svc.searchRecipes("missing");
            assertTrue(out.isEmpty());
        }
    }
}