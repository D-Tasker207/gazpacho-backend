package com.gazpacho.recipeservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "ingredients", indexes = { @Index(name = "idx_ingredient_name", columnList = "name") })
public class IngredientEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //overridden equals and hashcode here to allow for matching ingredient on ID instead of reference
    @EqualsAndHashCode.Include
    private Long id;

    private String name;

    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<IngredientAllergenEntity> ingredientAllergens = new HashSet<>();
}
