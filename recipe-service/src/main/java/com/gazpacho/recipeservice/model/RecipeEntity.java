package com.gazpacho.recipeservice.model;

import com.gazpacho.sharedlib.dto.RecipeDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Table(name = "recipes", indexes = { @Index(name = "idx_recipe_name", columnList = "name") })
public class RecipeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String image;

    @Column
    private String description;

    // Many-to-many ingredient-recipe relationship
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "recipe_ingredients",
        joinColumns = @JoinColumn(name = "recipe_id"),
        inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    private Set<IngredientEntity> ingredients = new HashSet<>();

    // Currently assuming steps are a list of strings
    @ElementCollection
    @CollectionTable(name = "recipe_steps", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "step")
    private List<String> steps = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
      name = "recipe_tags",
      joinColumns = @JoinColumn(name = "recipe_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"),
      uniqueConstraints = @UniqueConstraint(columnNames = {"recipe_id","tag_id"})
    )
    private Set<TagEntity> tags = new HashSet<>();

    public RecipeDTO toDto() {
        return new RecipeDTO(
                getId(),
                getName(),
                getImage(),
                getIngredients().stream().map(IngredientEntity::getName).toList(),
                getIngredients().stream()
                        .flatMap(ing -> ing.getAllergens().stream().map(AllergenEntity::getName))
                        .collect(Collectors.toSet()),
                getSteps(),
                getDescription(),
                tags.stream().map(TagEntity::getName).collect(Collectors.toSet())
        );
    }
}
