package com.gazpacho.recipeservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import java.util.Set;
import java.util.HashSet;

@Getter @Setter
@Entity
@Table(name = "tags",
       indexes = @Index(name = "idx_tag_name", columnList = "name"),
       uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class TagEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @ManyToMany(mappedBy = "tags")
  @EqualsAndHashCode.Exclude
  
  //set of recipes that have this tag
  private Set<RecipeEntity> recipes = new HashSet<>();
}
