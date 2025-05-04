package com.gazpacho.recipeservice.repository;

import com.gazpacho.recipeservice.model.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, Long> {
  Optional<TagEntity> findByName(String name);
  List<TagEntity> findByNameContainingIgnoreCase(String query);
}
