package com.gazpacho.userservice.controller;

import com.gazpacho.sharedlib.dto.LoginDTO;
import com.gazpacho.sharedlib.dto.PublicUserDTO;
import com.gazpacho.sharedlib.dto.TokenResponseDTO;
import com.gazpacho.sharedlib.dto.RefreshRequestDTO;
import com.gazpacho.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

  //endpoint for saving a recipe to a user
  @PostMapping("/{userId}/recipes/{recipeId}")
  public ResponseEntity<?> saveRecipe(@PathVariable("userId") Long userId,
                                      @PathVariable("recipeId") Long recipeId) {
    try {
      userService.saveRecipeForUser(userId, recipeId);
      return ResponseEntity.ok("Recipe saved successfully");
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }
}
