package com.gazpacho.userservice.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@Entity
@Table(name = "users",
       indexes = { @Index(name = "idx_email", columnList = "email") })
public class UserEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  @Column(unique = true) private String email;
  private String password;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_saved_recipes",
                   joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "recipe_id")
  private List<Long> savedRecipeIds = new ArrayList<>();
}
