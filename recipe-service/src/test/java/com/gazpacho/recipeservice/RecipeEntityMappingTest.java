package com.gazpacho.recipeservice;

import com.gazpacho.recipeservice.model.AllergenEntity;
import com.gazpacho.recipeservice.model.IngredientEntity;
import com.gazpacho.recipeservice.model.RecipeEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
//Added sql init.mode = never to avoid empty db initializer errors. (Remove once actually using db. Will throw errors so long as data.sql is empty.)
@DataJpaTest(
    properties = "spring.sql.init.mode=never"
)
public class RecipeEntityMappingTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    public void testIngredientAllergenEntityMapping() {
        //Create/persist AllergenEntity
        AllergenEntity allergen = new AllergenEntity();
        allergen.setName("Gluten");
        entityManager.persist(allergen);

        //Create/persist IngredientEntity
        IngredientEntity ingredient = new IngredientEntity();
        ingredient.setName("Wheat Flour");
        ingredient.getAllergens().add(allergen);
        entityManager.persist(ingredient);

        //Create/persist a join entity linking ingredient to allergen to test joins

        entityManager.flush();
        entityManager.clear();

        //get ingredient and verify its allergen mapping is as expected 
        IngredientEntity foundIngredient = entityManager.find(IngredientEntity.class, ingredient.getId());
        Set<AllergenEntity> joins = foundIngredient.getAllergens();
        assertNotNull(joins, "Ingredient join collection should not be null");
        assertEquals(1, joins.size(), "Ingredient should have one associated allergen join");
        AllergenEntity foundJoin = joins.iterator().next();
        assertEquals("Gluten", foundJoin.getName(), "Allergen name should match");
    }

    /*@Test
    public void testAllergenMappingBackToIngredients() {
        //Create/persist AllergenEntity
        AllergenEntity allergen = new AllergenEntity();
        allergen.setName("Peanuts");
        entityManager.persist(allergen);

        //Create/persist two IngredientEntities
        IngredientEntity ingredient1 = new IngredientEntity();
        ingredient1.setName("Peanut Butter");
        entityManager.persist(ingredient1);

        IngredientEntity ingredient2 = new IngredientEntity();
        ingredient2.setName("Peanuts");
        entityManager.persist(ingredient2);

        //Create a join for both ingredients linking them to an allergen
        //(Testing joins in both directions)
        IngredientAllergenEntity join1 = new IngredientAllergenEntity();
        join1.setIngredient(ingredient1);
        join1.setAllergen(allergen);
        entityManager.persist(join1);

        IngredientAllergenEntity join2 = new IngredientAllergenEntity();
        join2.setIngredient(ingredient2);
        join2.setAllergen(allergen);
        entityManager.persist(join2);

        entityManager.flush();
        entityManager.clear();

        //get allergen and check it maps back to both ingredients as expected
        AllergenEntity foundAllergen = entityManager.find(AllergenEntity.class, allergen.getId());
        Set<IngredientAllergenEntity> ingredientAllergens = foundAllergen.getIngredientAllergens();
        assertNotNull(ingredientAllergens, "Allergen join collection should not be null");
        assertEquals(2, ingredientAllergens.size(), "Allergen should be associated with two ingredients");

        //additional checking of the join entities fields (name here)
        for (IngredientAllergenEntity join : ingredientAllergens) {
            String ingredientName = join.getIngredient().getName();
            assertTrue(ingredientName.toLowerCase().contains("peanut"),
                "Ingredient name should contain 'peanut'");
        }
    }*/

    @Test
    public void testRecipeEntityMapping() {
        //Create/persist IngredientEntity
        IngredientEntity ingredient = new IngredientEntity();
        ingredient.setName("Tomato");
        entityManager.persist(ingredient);

        //Create/persist a RecipeEntity with an ingredient and steps
        //(Small integration test of recipe service feature and recipe entity feature)
        RecipeEntity recipe = new RecipeEntity();
        recipe.setName("Tomato Soup");
        recipe.getIngredients().add(ingredient);
        recipe.getSteps().add("Boil water");
        recipe.getSteps().add("Add tomatoes");
        recipe.getSteps().add("Simmer for 20 minutes");
        entityManager.persist(recipe);

        entityManager.flush();
        entityManager.clear();

        //get entry and check all fields, ensuring all is correctly mapped.
        RecipeEntity foundRecipe = entityManager.find(RecipeEntity.class, recipe.getId());
        assertNotNull(foundRecipe, "Recipe should be persisted and found");
        assertEquals("Tomato Soup", foundRecipe.getName(), "Recipe name should match");
        assertEquals(1, foundRecipe.getIngredients().size(), "Recipe should have one ingredient");
        assertEquals(3, foundRecipe.getSteps().size(), "Recipe should have three steps");
        List<String> steps = foundRecipe.getSteps();
        assertEquals("Boil water", steps.get(0));
        assertEquals("Add tomatoes", steps.get(1));
        assertEquals("Simmer for 20 minutes", steps.get(2));
    }

    //Border/Edge case testing below:

    @Test
    public void testRecipeWithNoIngredientsOrSteps() {
        RecipeEntity recipe = new RecipeEntity();
        recipe.setName("Empty Recipe");
        //keep steps and ingredients blank
        entityManager.persist(recipe);
        entityManager.flush();
        entityManager.clear();

        RecipeEntity foundRecipe = entityManager.find(RecipeEntity.class, recipe.getId());
        assertNotNull(foundRecipe, "Recipe should be persisted and found");
        assertEquals("Empty Recipe", foundRecipe.getName(), "Recipe name should match");
        assertTrue(foundRecipe.getIngredients().isEmpty(), "Expected no ingredients");
        assertTrue(foundRecipe.getSteps().isEmpty(), "Expected no steps");
    }

    @Test
    public void testRecipeEntityUpdateSteps() {
        //Create/persist a recipe with an initial data
        RecipeEntity recipe = new RecipeEntity();
        recipe.setName("Updatable Recipe");
        recipe.getSteps().add("Initial Step");
        entityManager.persist(recipe);
        entityManager.flush();
        entityManager.clear();

        //get the recipe, try to update-- persist again
        RecipeEntity foundRecipe = entityManager.find(RecipeEntity.class, recipe.getId());
        foundRecipe.getSteps().add("Second Step");
        entityManager.persist(foundRecipe);
        entityManager.flush();
        entityManager.clear();

        //Checkt for presence of first and added second step
        RecipeEntity updatedRecipe = entityManager.find(RecipeEntity.class, recipe.getId());
        List<String> steps = updatedRecipe.getSteps();
        assertEquals(2, steps.size(), "Recipe should have two steps after update");
        assertEquals("Initial Step", steps.get(0), "First step should remain unchanged");
        assertEquals("Second Step", steps.get(1), "Second step should be added");
    }

    @Test
    public void testRemoveIngredientFromRecipe() {
        //Create/persist ingredient.
        IngredientEntity ingredient = new IngredientEntity();
        ingredient.setName("Cheese");
        entityManager.persist(ingredient);
    
        //create recipe and add ingredient to it 
        RecipeEntity recipe = new RecipeEntity();
        recipe.setName("Cheesy Recipe");
        recipe.getIngredients().add(ingredient);
        entityManager.persist(recipe);
        entityManager.flush();
        entityManager.clear();
    
        //get recipe, try to remove ingredient
        RecipeEntity foundRecipe = entityManager.find(RecipeEntity.class, recipe.getId());
        assertEquals(1, foundRecipe.getIngredients().size(), "Recipe should initially have one ingredient");
        foundRecipe.getIngredients().remove(ingredient);
        entityManager.merge(foundRecipe);
        entityManager.flush();
        entityManager.clear();
    
        //check if ingredient is correctly removed
        RecipeEntity updatedRecipe = entityManager.find(RecipeEntity.class, recipe.getId());
        assertTrue(updatedRecipe.getIngredients().isEmpty(), "Ingredient should be removed from recipe");
    }
    

    /*@Test
    public void testIngredientDeletionCascadesJoinEntity() {
        //create allergen
        AllergenEntity allergen = new AllergenEntity();
        allergen.setName("Soy");
        entityManager.persist(allergen);

        //create ingredient
        IngredientEntity ingredient = new IngredientEntity();
        ingredient.setName("Soy Sauce");
        entityManager.persist(ingredient);

        //create a join of the two
        IngredientAllergenEntity join = new IngredientAllergenEntity();
        join.setIngredient(ingredient);
        join.setAllergen(allergen);
        entityManager.persist(join);

        entityManager.flush();
        entityManager.clear();

        //get the ingredient and check the join entity is present
        IngredientEntity toDelete = entityManager.find(IngredientEntity.class, ingredient.getId());
        //remove the join
        entityManager.remove(toDelete);
        entityManager.flush();
        entityManager.clear();

        //check that the join is no longer present 
        IngredientAllergenEntity deletedJoin = entityManager.find(IngredientAllergenEntity.class, join.getId());
        assertNull(deletedJoin, "Join entity should be deleted when ingredient is removed");
    }*/
}
