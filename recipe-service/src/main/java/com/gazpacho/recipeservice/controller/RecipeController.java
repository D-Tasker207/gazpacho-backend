package com.gazpacho.recipeservice.controller;

import com.gazpacho.sharedlib.dto.RecipeDTO;
import com.gazpacho.recipeservice.model.RecipeEntity;
import com.gazpacho.recipeservice.service.RecipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeEntity> viewRecipe(@PathVariable("id") Long recipeId) {
        RecipeEntity recipe = recipeService.viewRecipe(recipeId);
        return ResponseEntity.ok(recipe);
    }

    @GetMapping("/search")
    public ResponseEntity<List<RecipeDTO>> searchRecipes(@RequestParam("q") String query) {
        List<RecipeDTO> recipes = recipeService.searchRecipes(query);
        return ResponseEntity.ok(recipes);
    }
}
