package com.gazpacho.userservice.service;

import com.gazpacho.sharedlib.dto.LoginDTO;
import com.gazpacho.sharedlib.dto.PublicUserDTO;
import com.gazpacho.sharedlib.dto.TokenResponseDTO;
import com.gazpacho.userservice.model.UserEntity;
import com.gazpacho.userservice.repository.UserRepository;
import com.gazpacho.userservice.security.TokenGenerator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final TokenGenerator tokenGenerator;

  public UserService(UserRepository userRepository,
      TokenGenerator tokenGenerator) {
    this.userRepository = userRepository;
    this.tokenGenerator = tokenGenerator;
  }

  public PublicUserDTO registerUser(LoginDTO newUser) {
    if (userRepository.existsByEmail(newUser.getEmail()))
      throw new IllegalArgumentException("Email is already in use");

    UserEntity user = new UserEntity();
    user.setEmail(newUser.getEmail());
    user.setPassword(newUser.getPassword()); // TODO: Add password hashing

    UserEntity savedUser = userRepository.save(user);

    return new PublicUserDTO(savedUser.getId(), savedUser.getEmail(), savedUser.getSavedRecipeIds());
  }

  public Optional<TokenResponseDTO> loginUser(LoginDTO userDto) {
    if (!userRepository.existsByEmail(userDto.getEmail()))
      return Optional.empty();

    UserEntity user = userRepository.findByEmail(userDto.getEmail()).orElseThrow();
    if (!user.getPassword().equals(
        userDto.getPassword())) // TODO: Add password hashing
      return Optional.empty();

    String acessToken = tokenGenerator.generateAccessToken(user);
    String refreshToken = tokenGenerator.generateRefreshToken(user);

    return Optional.of(TokenResponseDTO.builder()
        .accessToken(acessToken)
        .refreshToken(refreshToken)
        .build()); // return JWT
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
