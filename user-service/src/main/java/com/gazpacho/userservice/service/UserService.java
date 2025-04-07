package com.gazpacho.userservice.service;

import com.gazpacho.sharedlib.dto.LoginDTO;
import com.gazpacho.sharedlib.dto.PublicUserDTO;
import com.gazpacho.userservice.model.UserEntity;
import com.gazpacho.userservice.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public PublicUserDTO registerUser(LoginDTO newUser) {
    if (userRepository.existsByEmail(newUser.getEmail()))
      throw new IllegalArgumentException("Email is already in use");

    UserEntity user = new UserEntity();
    user.setEmail(newUser.getEmail());
    user.setPassword(newUser.getPassword()); // TODO: Add password hashing

    userRepository.save(user);

    return new PublicUserDTO(user.getEmail(), user.getSavedRecipeIds());
  }

  public Optional<String> loginUser(LoginDTO userDto) {
    return null; // return JWT
  }

  public Optional<PublicUserDTO> getUserByEmail(String email) {
    return null;
  }

  public Optional<PublicUserDTO> getUserById(Long id) {
    return null;
  }

  public void saveRecipeForUser(Long userId, Long recipeId) {
    return;
  }

  public void removeSavedRecipe(Long userId, Long recipeId) {
    return;
  }

  public List<Long> getSavedRecipiesById(Long userId) {
    return null;
  }
}
