package com.gazpacho.recipeservice.service;

import com.gazpacho.recipeservice.model.AllergenEntity;
import com.gazpacho.recipeservice.model.IngredientEntity;
import com.gazpacho.recipeservice.repository.IngredientRepository;
import com.gazpacho.sharedlib.dto.RecipeDTO;
import com.gazpacho.recipeservice.model.RecipeEntity;
import com.gazpacho.recipeservice.repository.AllergenRepository;
import com.gazpacho.recipeservice.repository.RecipeRepository;
import com.gazpacho.sharedlib.dto.RequestRecipeDTO;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final AllergenRepository allergenRepository;
    private final IngredientRepository ingredientRepository;

    public RecipeService(RecipeRepository recipeRepository,
                         AllergenRepository allergenRepository,
                         IngredientRepository ingredientRepository) {
        this.recipeRepository = recipeRepository;
        this.allergenRepository = allergenRepository;
        this.ingredientRepository = ingredientRepository;
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
                    .map(RecipeEntity::toDto)
                    .collect(Collectors.toList());
        } else if ("ingredient".equalsIgnoreCase(type)) {
            return recipeRepository.findAll().stream()
                    .filter(recipe -> recipe.getIngredients().stream()
                            .anyMatch(ingredient ->
                                    ingredient.getName() != null &&
                                            ingredient.getName().toLowerCase().contains(query.toLowerCase())))
                    .map(RecipeEntity::toDto)
                    .collect(Collectors.toList());
        } else if ("allergen".equalsIgnoreCase(type)) {
            return recipeRepository.findAll().stream()
                    .filter(recipe -> recipe.getIngredients().stream()
                            .anyMatch(ingredient -> ingredient.getAllergens().stream()
                                    .anyMatch(all ->
                                            all != null &&
                                                    all.getName() != null &&
                                                    all.getName().toLowerCase().contains(query.toLowerCase()))))
                    .map(RecipeEntity::toDto)
                    .collect(Collectors.toList());
        } else {
            //default fallback
            return recipeRepository.findByNameContainingIgnoreCase(query)
                    .stream()
                    .map(RecipeEntity::toDto)
                    .collect(Collectors.toList());
        }
    }

    public List<RecipeDTO> addRecipe(List<RequestRecipeDTO> requests) {
        return requests.stream().map(request -> {
            RecipeEntity recipe = new RecipeEntity();
            recipe.setName(request.name());
            recipe.setDescription(request.description());
            recipe.setImage(request.image());
            request.steps().forEach(r -> recipe.getSteps().add(r));

            Set<IngredientEntity> ingredients = request.ingredients().stream().map(i -> {
                Set<AllergenEntity> allergens = i.allergens().stream()
                        .map(a -> allergenRepository.findByName(a)
                                .orElseGet(() -> {
                                    AllergenEntity allergen = new AllergenEntity();
                                    allergen.setName(a);
                                    return allergenRepository.save(allergen);
                                })
                        ).collect(Collectors.toSet());

                return ingredientRepository.findByName(i.name())
                        .orElseGet(() -> {
                            IngredientEntity newIngredient = new IngredientEntity();
                            newIngredient.setName(i.name());
                            newIngredient.setAllergens(allergens);
                            return ingredientRepository.save(newIngredient);
                        });

            }).collect(Collectors.toSet());
            recipe.setIngredients(ingredients);
            return recipeRepository.save(recipe).toDto();
        }).toList();
    }

    public List<RecipeDTO> getRecipes(List<Long> recipeIds) {
        return recipeIds.stream().map(recipeRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(RecipeEntity::toDto)
                .collect(Collectors.toList());
    }
}
