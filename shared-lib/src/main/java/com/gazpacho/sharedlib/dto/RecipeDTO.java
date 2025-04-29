package com.gazpacho.sharedlib.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDTO {
    private Long id;
    private String name;
    private String image;
    private List<String> ingredients;
    private Set<String> allergens;
    private List<String> steps;
    private String description;
}
