package com.gazpacho.userservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.gazpacho.sharedlib.dto.LoginDTO;
import com.gazpacho.sharedlib.dto.PublicUserDTO;
import com.gazpacho.sharedlib.dto.RefreshRequestDTO;
import com.gazpacho.sharedlib.dto.TokenResponseDTO;
import com.gazpacho.userservice.model.UserEntity;
import com.gazpacho.userservice.repository.UserRepository;
import com.gazpacho.recipeservice.repository.RecipeRepository;
import com.gazpacho.userservice.security.TokenGenerator;
import com.gazpacho.userservice.security.TokenValidator;
import com.gazpacho.userservice.service.UserService;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class UserServiceTest {

  private UserRepository userRepository;
  private UserService userService;
  private TokenGenerator tokenGenerator;
  private TokenValidator tokenValidator;
  private BCryptPasswordEncoder encoder;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    tokenGenerator = mock(TokenGenerator.class);
    tokenValidator = mock(TokenValidator.class);
    userService = new UserService(userRepository, tokenGenerator, tokenValidator);
    encoder = new BCryptPasswordEncoder();
  }

  @Test
  void registerUser_UniqueEmail() {
    LoginDTO dto = new LoginDTO("test@example.com", "password123");
    when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
    when(userRepository.save(any(UserEntity.class)))
        .thenAnswer(inv -> {
          UserEntity user = inv.getArgument(0);
          user.setId(1L); // Simulate auto-generated ID
          return user;
        });

    userService.registerUser(dto);

    ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(userCaptor.capture());

    UserEntity savedUser = userCaptor.getValue();

    assertEquals(1L, savedUser.getId());
    assertEquals("test@example.com", savedUser.getEmail());
    //Check that the raw password when hashed matches the stored hash
    assertTrue(encoder.matches("password123", savedUser.getPassword()));
  }

  @Test
  void registerUser_NonUniqueEmail() {
    LoginDTO dto = new LoginDTO("existingEmail@example.com", "password123");
    when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);
    Exception ex = assertThrows(IllegalArgumentException.class,
        () -> {
          userService.registerUser(dto);
        });

    assertEquals("Email is already in use", ex.getMessage());
    verify(userRepository, never()).save(any(UserEntity.class));
  }

  @Test
  void loginUser_ValidCredentials() {
    LoginDTO dto = new LoginDTO("test@example.com", "password123");

    UserEntity user = new UserEntity();
    user.setId(1L);
    user.setEmail(dto.getEmail());

    //simulate stored hash:
    String hashedPassword = encoder.encode("password123");
    user.setPassword(hashedPassword);

    when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);
    when(userRepository.findByEmail(dto.getEmail()))
        .thenReturn(Optional.of(user));
    when(tokenGenerator.generateAccessToken(any(UserEntity.class)))
        .thenReturn("jwt-token-abc123");
    when(tokenGenerator.getAccessExpTimeMillis()).thenReturn(0L);

    Optional<TokenResponseDTO> result = userService.loginUser(dto);

    assertTrue(result.isPresent());
    TokenResponseDTO tokenResponse = result.get();
    System.out.println("Token: " + tokenResponse.getAccessToken());
    assertNotNull(tokenResponse.getAccessToken());
    assertEquals("jwt-token-abc123", tokenResponse.getAccessToken());
    assertEquals("Bearer", tokenResponse.getTokenType());
  }

  @Test
  void loginUser_InValidCredentials() {
    LoginDTO dto = new LoginDTO("test@example.com", "wrongpassword");

    UserEntity user = new UserEntity();
    user.setEmail(dto.getEmail());

    //store fake hash for the password
    String hashedPassword = encoder.encode("password123");
    user.setPassword(hashedPassword);

    when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);
    when(userRepository.findByEmail(dto.getEmail()))
        .thenReturn(Optional.of(user));

    Optional<TokenResponseDTO> result = userService.loginUser(dto);

    assertFalse(result.isPresent());
    verifyNoInteractions(tokenGenerator);
  }

  @Test
  void loginUser_NonExistentUser() {
    LoginDTO dto = new LoginDTO("nonexistent@example.com", "password123");

    when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
    when(userRepository.findByEmail(dto.getEmail()))
        .thenReturn(Optional.empty());

    Optional<TokenResponseDTO> result = userService.loginUser(dto);

    assertFalse(result.isPresent());
    verifyNoInteractions(tokenGenerator);
  }

  @Test
  void testRefreshToken_InvalidToken_ReturnsEmpty() {
    String invalidRefreshToken = "invalid.token";

    when(tokenValidator.validateRefreshToken(invalidRefreshToken)).thenReturn(false);

    RefreshRequestDTO request = new RefreshRequestDTO();
    request.setRefreshToken(invalidRefreshToken);

    Optional<TokenResponseDTO> result = userService.refreshToken(request);

    assertTrue(result.isEmpty());
    verify(tokenValidator).validateRefreshToken(invalidRefreshToken);
    verifyNoInteractions(userRepository, tokenGenerator);
  }

  @Test
  void testRefreshToken_ValidToken_UserNotFound_ReturnsEmpty() {

    String validRefreshToken = "valid.refresh.token";
    Long userId = 42L;

    when(tokenValidator.validateRefreshToken(validRefreshToken)).thenReturn(true);
    when(tokenValidator.getUserIdFromRefreshToken(validRefreshToken)).thenReturn(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    RefreshRequestDTO request = new RefreshRequestDTO();
    request.setRefreshToken(validRefreshToken);

    Optional<TokenResponseDTO> result = userService.refreshToken(request);

    assertTrue(result.isEmpty());
    verify(tokenValidator).validateRefreshToken(validRefreshToken);
    verify(userRepository).findById(userId);
  }

  @Test
  void testRefreshToken_ValidToken_UserFound_ReturnsTokens() {
    String validRefreshToken = "valid.refresh.token";
    Long userId = 42L;
    UserEntity user = new UserEntity();
    String newAccessToken = "new.access.token";
    String newRefreshToken = "new.refresh.token";

    when(tokenValidator.validateRefreshToken(validRefreshToken)).thenReturn(true);
    when(tokenValidator.getUserIdFromRefreshToken(validRefreshToken)).thenReturn(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(tokenGenerator.generateAccessToken(user)).thenReturn(newAccessToken);
    when(tokenGenerator.generateRefreshToken(user)).thenReturn(newRefreshToken);

    RefreshRequestDTO request = new RefreshRequestDTO();
    request.setRefreshToken(validRefreshToken);

    Optional<TokenResponseDTO> result = userService.refreshToken(request);

    assertTrue(result.isPresent());
    TokenResponseDTO response = result.get();
    assertEquals(newAccessToken, response.getAccessToken());
    assertEquals(newRefreshToken, response.getRefreshToken());

    verify(tokenValidator).validateRefreshToken(validRefreshToken);
    verify(tokenValidator).getUserIdFromRefreshToken(validRefreshToken);
    verify(userRepository).findById(userId);
    verify(tokenGenerator).generateAccessToken(user);
    verify(tokenGenerator).generateRefreshToken(user);
  }

  @Test
  void fetchUser_ValidToken() {
    String validAccessToken = "valid.access.token";
    Long userId = 1L;
    UserEntity user = new UserEntity();
    user.setId(userId);
    user.setEmail("testemail@gmail.com");
    user.setPassword("password");
    when(tokenValidator.validateAccessToken(validAccessToken)).thenReturn(true);
    when(tokenValidator.getUserIdFromAccessToken(validAccessToken)).thenReturn(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    String authHeader = "Bearer " + validAccessToken;
    PublicUserDTO result = userService.fetchUser(authHeader).orElseThrow();

    assertEquals(1L, result.getId());
    assertEquals("testemail@gmail.com", result.getEmail());
  }

  @Test
  void fetchUser_InvalidToken() {
    String invalidAccessToken = "invalid.access.token";
    when(tokenValidator.validateAccessToken(invalidAccessToken)).thenReturn(false);

    String authHeader = "Bearer " + invalidAccessToken;
    Optional<PublicUserDTO> result = userService.fetchUser(authHeader);

    assertTrue(result.isEmpty());
  }

  @Test
  void fetchUser_InvalidTokenPrefix() {
    String validAccessToken = "valid.access.token";
    when(tokenValidator.validateAccessToken(validAccessToken)).thenReturn(true); // to ensure this is not the cause of the error
    String authHeader = "Bearr " + validAccessToken;
    Optional<PublicUserDTO> result = userService.fetchUser(authHeader);

    assertTrue(result.isEmpty());
  }
}
