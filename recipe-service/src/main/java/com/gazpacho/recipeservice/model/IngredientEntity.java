package com.gazpacho.recipeservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

//TODO: Finalize actual ingredient entity, this is a placeholder for recipe entity
@Getter
@Setter
@Entity
@Table(name = "ingredients")
public class IngredientEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // A minimal placeholder field
  private String name;
}
