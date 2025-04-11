package com.gazpacho.recipeservice.service;

import com.gazpacho.sharedlib.dto.RecipeDTO;
import com.gazpacho.recipeservice.model.RecipeEntity;
import com.gazpacho.recipeservice.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecipeService {

  private final RecipeRepository recipeRepository;

  public RecipeService(RecipeRepository recipeRepository) {
    this.recipeRepository = recipeRepository;
  }

  public Optional<RecipeEntity> viewRecipe(Long recipeId) {
      return recipeRepository.findById(recipeId);
  }

  public void deleteRecipe(Long recipeId) {
    if (recipeRepository.existsById(recipeId)) {
        recipeRepository.deleteById(recipeId);
    } else {
        throw new RuntimeException("Recipe not found");
    }
  }

  // Overloaded method to default to recipe search.
  public List<RecipeDTO> searchRecipes(String query) {
    return searchRecipes(query, "recipe");
  }

  //segmented search-- if type is not one of expected, default to recipesearch
  public List<RecipeDTO> searchRecipes(String query, String type) {
    if ("recipe".equalsIgnoreCase(type)) {
      return recipeRepository.findByNameContainingIgnoreCase(query)
              .stream()
              .map(recipe -> new RecipeDTO(recipe.getId(), recipe.getName()))
              .collect(Collectors.toList());
    } else if ("ingredient".equalsIgnoreCase(type)) {
      return recipeRepository.findAll().stream()
              .filter(recipe -> recipe.getIngredients().stream()
                      .anyMatch(ingredient ->
                              ingredient.getName() != null &&
                              ingredient.getName().toLowerCase().contains(query.toLowerCase())))
              .map(recipe -> new RecipeDTO(recipe.getId(), recipe.getName()))
              .collect(Collectors.toList());
    } else if ("allergen".equalsIgnoreCase(type)) {
      return recipeRepository.findAll().stream()
              .filter(recipe -> recipe.getIngredients().stream()
                      .anyMatch(ingredient -> ingredient.getIngredientAllergens().stream()
                              .anyMatch(join ->
                                  join.getAllergen() != null &&
                                  join.getAllergen().getName() != null &&
                                  join.getAllergen().getName().toLowerCase().contains(query.toLowerCase()))))
              .map(recipe -> new RecipeDTO(recipe.getId(), recipe.getName()))
              .collect(Collectors.toList());
    } else {
      //default fallback
      return recipeRepository.findByNameContainingIgnoreCase(query)
              .stream()
              .map(recipe -> new RecipeDTO(recipe.getId(), recipe.getName()))
              .collect(Collectors.toList());
    }
  }
}
