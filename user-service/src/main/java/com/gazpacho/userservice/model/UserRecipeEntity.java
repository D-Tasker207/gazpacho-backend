package com.gazpacho.userservice.model;

import com.gazpacho.recipeservice.model.RecipeEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_recipes",
        //Unique constraint here to ensure that recipes are only saved to each user once
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "recipe_id"}))
public class UserRecipeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //many-to-one relationship to UserEntity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    //many-to-one relationship to RecipeEntity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private RecipeEntity recipe;
}
