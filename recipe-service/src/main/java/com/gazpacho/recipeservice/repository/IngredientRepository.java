package com.gazpacho.recipeservice.repository;

import com.gazpacho.recipeservice.model.IngredientEntity;
import com.gazpacho.recipeservice.model.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<IngredientEntity, Long> {
  Optional<IngredientEntity> findByName(String name);

  List<IngredientEntity> findByNameContainingIgnoreCase(String query);
  // findByID auto included in JPA
}
