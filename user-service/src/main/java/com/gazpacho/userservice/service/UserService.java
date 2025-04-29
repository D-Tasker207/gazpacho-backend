package com.gazpacho.userservice.service;

import com.gazpacho.sharedlib.dto.*;
import com.gazpacho.userservice.model.UserEntity;
import com.gazpacho.userservice.repository.UserRepository;
import com.gazpacho.recipeservice.model.RecipeEntity;
import com.gazpacho.recipeservice.repository.RecipeRepository;
import com.gazpacho.userservice.security.TokenGenerator;
import com.gazpacho.userservice.security.TokenValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final TokenValidator tokenValidator;
  private final TokenGenerator tokenGenerator;
  private final BCryptPasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository,
      TokenGenerator tokenGenerator, TokenValidator tokenValidator) {
    this.userRepository = userRepository;
    this.tokenGenerator = tokenGenerator;
    this.tokenValidator = tokenValidator;
    this.passwordEncoder = new BCryptPasswordEncoder();
  
  }

  public PublicUserDTO registerUser(LoginDTO newUser) {
    if (userRepository.existsByEmail(newUser.getEmail()))
      throw new IllegalArgumentException("Email is already in use");

    UserEntity user = new UserEntity();
    user.setEmail(newUser.getEmail());
    String hashedPassword = passwordEncoder.encode(newUser.getPassword());
    user.setPassword(hashedPassword);

    UserEntity savedUser = userRepository.save(user);
    // Adapt the DTO constructor as needed.
    return new PublicUserDTO(savedUser.getId(), savedUser.getEmail(), null);
  }

  public Optional<TokenResponseDTO> loginUser(LoginDTO userDto) {
    if (!userRepository.existsByEmail(userDto.getEmail()))
      return Optional.empty();

    UserEntity user = userRepository.findByEmail(userDto.getEmail()).orElseThrow();
    //compare the users inputed plain text password with the stored hash
    if (!passwordEncoder.matches(userDto.getPassword(), user.getPassword()))
      return Optional.empty();

    String acessToken = tokenGenerator.generateAccessToken(user);
    String refreshToken = tokenGenerator.generateRefreshToken(user);

    return Optional.of(TokenResponseDTO.builder()
        .accessToken(acessToken)
        .refreshToken(refreshToken)
        .build()); // return JWT
  }

  public Optional<TokenResponseDTO> refreshToken(RefreshRequestDTO dto) {
    // TODO: (Potentially) add logging to track invalid refresh requests
    if (!tokenValidator.validateRefreshToken(dto.getRefreshToken())) {
      return Optional.empty();
    }

    UserEntity user = userRepository.findById(tokenValidator.getUserIdFromRefreshToken(dto.getRefreshToken()))
        .orElse(null);
    if (user == null) {
      return Optional.empty();
    }

    return Optional.of(TokenResponseDTO.builder()
        .accessToken(tokenGenerator.generateAccessToken(user))
        .refreshToken(tokenGenerator.generateRefreshToken(user))
        .build());
  }

  public Optional<UserEntity> fetchUserByToken(String token) {
    if (!token.startsWith("Bearer ")) return Optional.empty();
    token = token.substring(7);
    if (!tokenValidator.validateAccessToken(token)) return Optional.empty();

    UserEntity user = userRepository
            .findById(tokenValidator.getUserIdFromAccessToken(token))
            .orElse(null);
    if (user == null) return Optional.empty();
    else return Optional.of(user);
  }

  public Optional<PublicUserDTO> fetchUser(String token) {
    if (!token.startsWith("Bearer ")) return Optional.empty();
    token = token.substring(7);
    if (!tokenValidator.validateAccessToken(token)) return Optional.empty();

    UserEntity user = userRepository
            .findById(tokenValidator.getUserIdFromAccessToken(token))
            .orElse(null);
    if (user == null) return Optional.empty();

    return Optional.of(new PublicUserDTO(
        user.getId(),
        user.getEmail(),
        user.getSavedRecipeIds()
    ));
  }

  // New implementation using join entity for saving a recipe.
  public void saveRecipeForUser(Long userId, Long recipeId) {
    Optional<UserEntity> maybeUser = userRepository.findById(userId);
    if (maybeUser.isEmpty()) {
      throw new RuntimeException("User not found");
    }
    // Check that the recipe exists.
    /*Optional<RecipeEntity> maybeRecipe = recipeRepository.findById(recipeId);
    if (maybeRecipe.isEmpty()) {
      throw new RuntimeException("Recipe not found");
    }*/
    UserEntity user = maybeUser.get();
    // Check if this recipe is already saved (by checking the join collection).
    boolean alreadySaved = user.getSavedRecipeIds().stream()
            .anyMatch(id -> id.equals(recipeId));
    
    if (!alreadySaved) {
      // Create a new join entity.
      user.getSavedRecipeIds().add(recipeId);
      userRepository.save(user);
    }
  }

  public void removeSavedRecipe(Long userId, Long recipeId) {
    Optional<UserEntity> maybeUser = userRepository.findById(userId);
    if (maybeUser.isEmpty()) {
      throw new RuntimeException("User not found");
    }

    UserEntity user = maybeUser.get();

    boolean saved = user.getSavedRecipeIds().stream()
            .anyMatch(id -> id.equals(recipeId));

    if (saved) {
      user.getSavedRecipeIds().remove(recipeId);
      userRepository.save(user);
    }
  }

  public List<Long> getSavedRecipiesById(Long userId) {
    // For the join approach, return recipe IDs from the join table.
    return userRepository.findById(userId)
             .map(UserEntity::getSavedRecipeIds)
             .orElse(Collections.emptyList());
  }
}
