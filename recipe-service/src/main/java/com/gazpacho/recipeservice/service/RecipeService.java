package com.gazpacho.recipeservice.service;

import com.gazpacho.sharedlib.dto.RecipeDTO;
import com.gazpacho.recipeservice.model.RecipeEntity;
import com.gazpacho.recipeservice.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public RecipeEntity viewRecipe(Long recipeId) {
        return recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
    }

    public List<RecipeDTO> searchRecipes(String query) {
        return recipeRepository.findAll().stream()
                .filter(recipe -> recipe.getName() != null &&
                                  recipe.getName().toLowerCase().contains(query.toLowerCase()))
                .map(recipe -> new RecipeDTO(recipe.getId(), recipe.getName()))
                .collect(Collectors.toList());
    }
}
