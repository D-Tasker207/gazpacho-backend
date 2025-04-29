package com.gazpacho.recipeservice.repository;

import com.gazpacho.recipeservice.model.AllergenEntity;
import com.gazpacho.recipeservice.model.IngredientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AllergenRepository extends JpaRepository<AllergenEntity, Long> {
  Optional<AllergenEntity> findByName(String name);

  List<AllergenEntity> findByNameContainingIgnoreCase(String query);
  // findByID auto included in JPA
}
