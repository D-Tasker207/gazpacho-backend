package com.gazpacho.userservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.gazpacho.sharedlib.dto.LoginDTO;
import com.gazpacho.sharedlib.dto.PublicUserDTO;
import com.gazpacho.userservice.model.UserEntity;
import com.gazpacho.userservice.repository.UserRepository;
import com.gazpacho.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class UserServiceTest {

  private UserRepository userRepository;
  private UserService userService;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    userService = new UserService(userRepository);
  }

  @Test
  void registerUser_UniqueEmail() {
    LoginDTO dto = new LoginDTO("test@example.com", "password123");
    when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
    when(userRepository.save(any(UserEntity.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    PublicUserDTO result = userService.registerUser(dto);

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
}
