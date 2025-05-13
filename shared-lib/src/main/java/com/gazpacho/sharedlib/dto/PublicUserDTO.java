package com.gazpacho.sharedlib.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicUserDTO {
  private Long id;
  private String email;
  //new admin flag
  private boolean admin; 
  private List<Long> savedRecipeIds;
}
