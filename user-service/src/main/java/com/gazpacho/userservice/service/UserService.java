package com.gazpacho.userservice.service;

import com.gazpacho.sharedlib.dto.LoginDTO;
import com.gazpacho.sharedlib.dto.PublicUserDTO;
import com.gazpacho.sharedlib.dto.RefreshRequestDTO;
import com.gazpacho.sharedlib.dto.TokenResponseDTO;
import com.gazpacho.userservice.model.UserEntity;
import com.gazpacho.userservice.repository.UserRepository;
import com.gazpacho.recipeservice.repository.RecipeRepository;
import com.gazpacho.userservice.security.TokenGenerator;
import com.gazpacho.userservice.security.TokenValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final TokenValidator tokenValidator;
  private final TokenGenerator tokenGenerator;
  private final RecipeRepository recipeRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository,
      TokenGenerator tokenGenerator, TokenValidator tokenValidator, RecipeRepository recipeRepository) {
    this.userRepository = userRepository;
    this.tokenGenerator = tokenGenerator;
    this.tokenValidator = tokenValidator;
    this.recipeRepository = recipeRepository;
    this.passwordEncoder = new BCryptPasswordEncoder();
  
  }

  public PublicUserDTO registerUser(LoginDTO newUser) {
    if (userRepository.existsByEmail(newUser.getEmail()))
      throw new IllegalArgumentException("Email is already in use");

    UserEntity user = new UserEntity();
    user.setEmail(newUser.getEmail());
    //hash plain text password before saving on our end 
    String hashedPassword = passwordEncoder.encode(newUser.getPassword());
    user.setPassword(hashedPassword);

    UserEntity savedUser = userRepository.save(user);

    return new PublicUserDTO(savedUser.getId(), savedUser.getEmail(), savedUser.getSavedRecipeIds());
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

  public Optional<PublicUserDTO> getUserById(Long id) {
    return null;
  }

  public void saveRecipeForUser(Long userId, Long recipeId) {
    //look for user, add optional in case of error with user ID
    Optional<UserEntity> maybeUser = userRepository.findById(userId);
    if (maybeUser.isPresent()) {
      //ensure recipe exists in the recipe table before adding to user's saved list
      if (!recipeRepository.existsById(recipeId)) {
        throw new RuntimeException("Recipe not found");
      }
      UserEntity user = maybeUser.get();
      //ensure recipe isnt already in user's saved list and add
      if (!user.getSavedRecipeIds().contains(recipeId)) {
        //grab id adn add to user's saved list
        user.getSavedRecipeIds().add(recipeId);
        userRepository.save(user);
      }
    } else {
      //other case, some error occurs with the user ID
      throw new RuntimeException("User not found");
    }
  }

  public void removeSavedRecipe(Long userId, Long recipeId) {
    return;
  }

  public List<Long> getSavedRecipiesById(Long userId) {
    //Get saved recipes by ID so they can be fetched in Users' saved recipes.
    //Temporary solution to Users saving recipes. 
    //Currently, would need additional logic to remove recipes from Users lists that are removed from the actual recipes table
    //TODO: Aim to implement a join table between recipes and users so we can save other metadata about saved items down the line?
    return userRepository.findById(userId)
             .map(UserEntity::getSavedRecipeIds)
             .orElse(Collections.emptyList());
  }
}
