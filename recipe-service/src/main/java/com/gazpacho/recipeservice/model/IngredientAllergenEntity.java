package com.gazpacho.recipeservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ingredient_allergens")
public class IngredientAllergenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Many toone relationship to IngredientEntity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private IngredientEntity ingredient;

    //Many to one relationship to AllergenEntity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allergen_id")
    private AllergenEntity allergen;
}
