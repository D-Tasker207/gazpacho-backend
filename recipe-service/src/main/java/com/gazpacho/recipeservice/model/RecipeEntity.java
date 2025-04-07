package com.gazpacho.recipeservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

@Getter
@Setter
@Entity
@Table(name = "recipes", 
       indexes = { @Index(name = "idx_recipe_name", columnList = "name") })
public class RecipeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    //Many to many ingredient-recipe relationship
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "recipe_ingredients",
        joinColumns = @JoinColumn(name = "recipe_id"),
        inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    private Set<IngredientEntity> ingredients = new HashSet<>();

    //Currently assuming steps are a list of strings
    @ElementCollection
    @CollectionTable(name = "recipe_steps", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "step")
    private List<String> steps = new ArrayList<>();

    //TODO: Join to user entity to allow for users to save recipes.
}
