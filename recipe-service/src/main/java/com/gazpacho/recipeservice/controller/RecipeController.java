package com.gazpacho.recipeservice.controller;

import com.gazpacho.sharedlib.dto.PublicUserDTO;
import com.gazpacho.userservice.service.UserService;
import com.gazpacho.sharedlib.dto.RecipeDTO;
import com.gazpacho.recipeservice.model.RecipeEntity;
import com.gazpacho.recipeservice.service.RecipeService;
import com.gazpacho.sharedlib.dto.RequestRecipeDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/recipes")
public class RecipeController {

  private final RecipeService recipeService;

  //include a UserService instance to check isAdmin flag for privileged actions (Ex. Delete recipe)
  //So long as users must be logged in to access site, this should be sufficient?
  private final UserService userService;

  public RecipeController(RecipeService recipeService, UserService userService) {
    this.recipeService = recipeService;
    this.userService = userService;
  }

  @PutMapping("/batch")
  public ResponseEntity<?> addRecipes(@RequestBody List<RequestRecipeDTO> request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(recipeService.addRecipe(request));
  }

  @GetMapping("/batch")
  public ResponseEntity<?> getRecipesBatch(@RequestParam("ids") List<Long> recipeIds) {
    List<RecipeDTO> recipes = recipeService.getRecipes(recipeIds);
    return ResponseEntity.ok(recipes);
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> viewRecipe(@PathVariable("id") Long recipeId) {
    // unwrapping optional
    Optional<RecipeEntity> maybeRecipe = recipeService.viewRecipe(recipeId);
    if (maybeRecipe.isPresent()) {
      RecipeEntity recipe = maybeRecipe.get();
      // convert RecipeEntity to RecipeDTO.
      RecipeDTO dto = recipe.toDto();
      return ResponseEntity.ok(dto);
    } else {
      // Return 404 Not Found with an error message.
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("Recipe with id " + recipeId + " not found");
    }
  }

    //DELETE endpoint for deleting a recipe:
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
      @PathVariable("id") Long recipeId
      ) {
        //validate bearer token and fetch user to check admin status
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
          return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<PublicUserDTO> user = userService.fetchUser(authHeader);
        if (user.isEmpty() || !Boolean.TRUE.equals(user.get().isAdmin())) {
          return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

      //if user is an admin, perform delete operation
      try {
        recipeService.deleteRecipe(recipeId);
        return ResponseEntity.ok("Recipe deleted successfully");
      } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("Error deleting recipe: " + e.getMessage());
      }
    }

  //segmented search: by recipe, ingredient, or allergen.(Added recipe as default)
  @GetMapping("/search")
  public ResponseEntity<List<RecipeDTO>> searchRecipes(
          @RequestParam("q") String query,
          @RequestParam(name = "type", required = false, defaultValue = "recipe") String type) {
    List<RecipeDTO> recipes = recipeService.searchRecipes(query, type);
    return ResponseEntity.ok(recipes);
  }
}
