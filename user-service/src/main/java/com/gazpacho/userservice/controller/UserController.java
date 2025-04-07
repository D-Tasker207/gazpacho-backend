package com.gazpacho.userservice.controller;

import com.gazpacho.sharedlib.dto.LoginDTO;
import com.gazpacho.sharedlib.dto.PublicUserDTO;
import com.gazpacho.userservice.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
}
