package com.gazpacho.recipeservice.repository;

import com.gazpacho.recipeservice.model.RecipeEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<RecipeEntity, Long> {
  Optional<RecipeEntity> findByName(String name);

  List<RecipeEntity> findByNameContainingIgnoreCase(String query);
  // findByID auto included in JPA
}
