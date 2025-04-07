package com.gazpacho.sharedlib.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicUserDTO {
<<<<<<< HEAD
    private Long id;
=======

>>>>>>> 882638b38577b6d64d7af4d05aec0eac50b37214
    private String email;
    private List<Long> savedRecipeIds;
}
