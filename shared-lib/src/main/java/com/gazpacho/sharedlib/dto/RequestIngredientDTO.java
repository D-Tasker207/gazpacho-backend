package com.gazpacho.sharedlib.dto;

import java.util.List;

public record RequestIngredientDTO(String name, List<String> allergens) {
}
