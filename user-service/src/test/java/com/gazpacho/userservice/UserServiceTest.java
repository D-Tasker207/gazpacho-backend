package com.gazpacho.userservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.gazpacho.sharedlib.dto.LoginDTO;
import com.gazpacho.sharedlib.dto.TokenResponseDTO;
import com.gazpacho.userservice.security.JWTTokenGenerator;
import com.gazpacho.userservice.security.TokenGenerator;
import com.gazpacho.userservice.model.UserEntity;
import com.gazpacho.userservice.repository.UserRepository;
import com.gazpacho.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.Optional;

class UserServiceTest {

  private UserRepository userRepository;
  private UserService userService;
  private TokenGenerator tokenGenerator;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    tokenGenerator = mock(JWTTokenGenerator.class);
    userService = new UserService(userRepository);
  }

  @Test
  void registerUser_UniqueEmail() {
    LoginDTO dto = new LoginDTO("test@example.com", "password123");
    when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
    when(userRepository.save(any(UserEntity.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    userService.registerUser(dto);

    ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(userCaptor.capture());

    UserEntity savedUser = userCaptor.getValue();
    assertEquals("test@example.com", savedUser.getEmail());
    assertEquals("password123", savedUser.getPassword());
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
    user.setEmail(dto.getEmail());
    user.setPassword("password123");

    when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);
    when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(user));
    when(tokenGenerator.generateToken(user)).thenReturn("jwt-token-abc123");

    Optional<TokenResponseDTO> result = userService.loginUser(dto);

    assertTrue(result.isPresent());
    assertEquals("jwt-token-abc123", result.get());
  }

  @Test
  void loginUser_InValidCredentials() {
    LoginDTO dto = new LoginDTO("test@example.com", "wrongpassword");

    UserEntity user = new UserEntity();
    user.setEmail(dto.getEmail());
    user.setPassword("password123");

    when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);
    when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(user));

    Optional<TokenResponseDTO> result = userService.loginUser(dto);

    assertFalse(result.isPresent());
    verifyNoInteractions(tokenGenerator);
  }

  @Test
  void loginUser_NonExistentUser() {
    LoginDTO dto = new LoginDTO("nonexistent@example.com", "password123");

    when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
    when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());

    Optional<TokenResponseDTO> result = userService.loginUser(dto);

    assertFalse(result.isPresent());
    verifyNoInteractions(tokenGenerator);
  }
}
