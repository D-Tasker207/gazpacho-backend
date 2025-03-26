package com.gazpacho.userservice.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gazpacho.userservice.service.UserService;

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
}
