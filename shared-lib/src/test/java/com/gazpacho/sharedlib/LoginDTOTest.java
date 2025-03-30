package com.gazpacho.sharedlib;

import static org.junit.jupiter.api.Assertions.*;

import com.gazpacho.sharedlib.dto.LoginDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LoginDTOTest { // Create a Validator instance (this can be reused across
                     // tests)
  private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
  private static final Validator validator = factory.getValidator();

  @ParameterizedTest(name = "email: \"{0}\", password: \"{1}\" should be valid? {2}")
  @MethodSource("loginDtoProvider")
  void testLoginDTOValidation(String email, String password, boolean expectedValid) {
    LoginDTO dto = new LoginDTO(email, password);
    Set<ConstraintViolation<LoginDTO>> violations = validator.validate(dto);

    if (expectedValid) {
      assertTrue(violations.isEmpty(),
          "Expected no validation errors for a valid DTO, but got: " +
              violations);
    } else {
      assertFalse(violations.isEmpty(),
          "Expected validation errors for an invalid DTO.");
    }
  }

  private static Stream<Arguments> loginDtoProvider() {
    return Stream.of(
        // Valid cases
        Arguments.of("test@example.com", "password123", true),
        Arguments.of("user.name+tag@example.co.uk", "12345678", true),
        Arguments.of("email@example.io", "abcdefgh", true),

        // Invalid cases for email:
        Arguments.of(null, "password123", false), // email is null
        Arguments.of("", "password123", false), // email is empty
        Arguments.of("   ", "password123",
            false), // email is blank (whitespace)
        Arguments.of("invalidEmail", "password123",
            false), // email is not well-formed

        // Invalid cases for password:
        Arguments.of("test@example.com", null, false), // password is null
        Arguments.of("test@example.com", "", false), // password is empty
        Arguments.of("test@example.com", "short",
            false), // password is less than 8 characters
        // A password longer than 64 characters (65 characters in this example)
        Arguments.of(
            "test@example.com",
            "12345678901234567890123456789012345678901234567890123456789012345",
            false));
  }
}
