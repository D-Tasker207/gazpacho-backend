package com.gazpacho.recipeservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "allergens", indexes = { @Index(name = "idx_allergen_name", columnList = "name") })
public class AllergenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    //One to many relationship to IngredientAllergen join entity, commented out until I add the join entity
    //@OneToMany(mappedBy = "allergen", cascade = CascadeType.ALL, orphanRemoval = true)
    //private Set<IngredientAllergen> ingredientAllergens = new HashSet<>();
}
