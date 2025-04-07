package com.gazpacho.recipeservice.controller;

import com.gazpacho.sharedlib.dto.RecipeDTO;
import com.gazpacho.recipeservice.model.RecipeEntity;
import com.gazpacho.recipeservice.service.RecipeService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> viewRecipe(@PathVariable("id") Long recipeId) {
        //unwrapping optional 
        Optional<RecipeEntity> maybeRecipe = recipeService.viewRecipe(recipeId);
        if (maybeRecipe.isPresent()) {
            RecipeEntity recipe = maybeRecipe.get();
            //convert RecipeEntity to RecipeDTO.
            RecipeDTO dto = new RecipeDTO(recipe.getId(), recipe.getName());
            return ResponseEntity.ok(dto);
        } else {
            //Return 404 Not Found with an error message.
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Recipe with id " + recipeId + " not found");
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<RecipeDTO>> searchRecipes(@RequestParam("q") String query) {
        List<RecipeDTO> recipes = recipeService.searchRecipes(query);
        return ResponseEntity.ok(recipes);
    }
}
