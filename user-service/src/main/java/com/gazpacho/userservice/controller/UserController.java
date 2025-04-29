package com.gazpacho.userservice.controller;

import com.gazpacho.sharedlib.dto.*;
import com.gazpacho.userservice.model.UserEntity;
import com.gazpacho.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  /*
   * example route:
   *
   * @PostMapping("/register")
   * public ResponseEntity<LoginDTO> registerUser(@RequestBody LoginDTO dto) {
   * return null;
   * }
   */

  /**
   * @param LoginDTO dto - This is the email and passwordHash for the newly
   *                 registered user
   *
   * @return ResponseEntity<Void> - no response required as the frontend only
   *         needs to forward the user to the login page
   */
  @PostMapping("/register")
  public ResponseEntity<PublicUserDTO> registerUser(@Valid @RequestBody LoginDTO dto) {
    PublicUserDTO newUser = userService.registerUser(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponseDTO> loginUser(@Valid @RequestBody LoginDTO dto) {
    Optional<TokenResponseDTO> token = userService.loginUser(dto);

    return token
        .map(t -> ResponseEntity.ok(t))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN).build());
  }

  @PostMapping("/refresh")
  public ResponseEntity<TokenResponseDTO> loginUser(@Valid @RequestBody RefreshRequestDTO dto) {
    Optional<TokenResponseDTO> token = userService.refreshToken(dto);

    return token
        .map(t -> ResponseEntity.ok(t))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN).build());
  }

  @GetMapping("")
  public ResponseEntity<PublicUserDTO> fetchUser(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION) String authHeader
  ) {
    Optional<PublicUserDTO> user = userService.fetchUser(authHeader);
    return user
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  //endpoint for saving a recipe to a user
  @PostMapping("/recipes/{recipeId}")
  public ResponseEntity<?> saveRecipe(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION) String authHeader,
          @PathVariable("recipeId") Long recipeId) {
    Optional<UserEntity> userOptional = userService.fetchUserByToken(authHeader);
    if (userOptional.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    try {
      userService.saveRecipeForUser(userOptional.get().getId(), recipeId);
      return ResponseEntity.ok("Recipe saved successfully");
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }

  @DeleteMapping("/recipes/{recipeId}")
  public ResponseEntity<?> deleteSavedRecipe(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION) String authHeader,
          @PathVariable("recipeId") Long recipeId) {
    Optional<UserEntity> userOptional = userService.fetchUserByToken(authHeader);
    if (userOptional.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    try {
      userService.removeSavedRecipe(userOptional.get().getId(), recipeId);
      return ResponseEntity.ok("Recipe saved successfully");
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }
}
