package com.gazpacho.recipeservice.repository;

import com.gazpacho.recipeservice.model.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<RecipeEntity, Long> {
    //Custom query here
}
