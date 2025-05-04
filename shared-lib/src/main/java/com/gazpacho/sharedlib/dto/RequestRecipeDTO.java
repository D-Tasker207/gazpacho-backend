package com.gazpacho.sharedlib.dto;

import java.util.List;

public record RequestRecipeDTO(String name, String image, String description, List<RequestIngredientDTO> ingredients, List<String> steps) {
}
