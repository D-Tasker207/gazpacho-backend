package com.gazpacho.sharedlib.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicUserDTO {

    private String email;
    private List<Long> savedRecipeIds;
}
