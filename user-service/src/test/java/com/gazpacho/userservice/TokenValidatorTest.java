package com.gazpacho.userservice;

import com.gazpacho.userservice.security.TokenGenerator;
import com.gazpacho.userservice.security.TokenValidator;

import com.gazpacho.userservice.model.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TokenValidatorTest {

    private TokenGenerator tokenGenerator;
    private TokenValidator tokenValidator;

    private final String testSecret = "thisisnottheactualsecurekey12345"; // 32+ chars

    private UserEntity user;

    @BeforeEach
    public void setup() {
        tokenGenerator = new TokenGenerator();
        tokenValidator = new TokenValidator();

        // Inject secret key manually (since @Value won't work outside Spring context)
        ReflectionTestUtils.setField(tokenGenerator, "secretKey", testSecret);
        ReflectionTestUtils.setField(tokenGenerator, "expirationTimeMillis", 3600000L); // 👈 1 hour

        ReflectionTestUtils.setField(tokenValidator, "secretKey", testSecret);

        tokenGenerator.init();
        tokenValidator.init();

        user = new UserEntity();
        user.setId(1L);
        user.setEmail("user@example.com");
    }

    @Test
    public void testValidToken() {
        String token = tokenGenerator.generateToken(user);

        assertTrue(tokenValidator.validateToken(token));
        assertEquals(1L, tokenValidator.getUserId(token));
        assertEquals("user@example.com", tokenValidator.getEmail(token));
    }

    @Test
    public void testInvalidToken() {
        String invalidToken = "not.a.valid.token";

        assertFalse(tokenValidator.validateToken(invalidToken));
        assertNull(tokenValidator.getUserId(invalidToken));
        assertNull(tokenValidator.getEmail(invalidToken));
    }

    @Test
    public void testExpiredToken() throws InterruptedException {
        // Short-lived token generator
        TokenGenerator shortTokenGenerator = new TokenGenerator();
        ReflectionTestUtils.setField(shortTokenGenerator, "secretKey", testSecret);
        ReflectionTestUtils.setField(shortTokenGenerator, "expirationTimeMillis", 100);
        shortTokenGenerator.init();

        String token = shortTokenGenerator.generateToken(user);

        Thread.sleep(200); // wait for token to expire

        assertFalse(tokenValidator.validateToken(token));
        assertNull(tokenValidator.getUserId(token));
        assertNull(tokenValidator.getEmail(token));
    }

    @Test
    public void testMissingEmail() {
        UserEntity noEmailUser = new UserEntity();
        noEmailUser.setId(3L);
        // No email set

        String token = tokenGenerator.generateToken(noEmailUser);

        assertTrue(tokenValidator.validateToken(token));
        assertEquals(3L, tokenValidator.getUserId(token));
        assertNull(tokenValidator.getEmail(token)); // Email should be null
    }

    @Test
    public void testTamperedToken() {
        String token = tokenGenerator.generateToken(user);
        String tampered = token.substring(0, token.length() - 1) + "X";

        assertFalse(tokenValidator.validateToken(tampered));
        assertNull(tokenValidator.getUserId(tampered));
        assertNull(tokenValidator.getEmail(tampered));
    }

    @Test
    public void testTokenWithWrongSecret() {
        TokenValidator otherValidator = new TokenValidator();
        ReflectionTestUtils.setField(otherValidator, "secretKey", "thisisadifferentsecretkey1234567");
        otherValidator.init();

        String token = tokenGenerator.generateToken(user);
        assertFalse(otherValidator.validateToken(token));
        assertNull(otherValidator.getUserId(token));
        assertNull(otherValidator.getEmail(token));
    }

    @Test
    public void testJustExpiredToken() throws InterruptedException {
        ReflectionTestUtils.setField(tokenGenerator, "expirationTimeMillis", 100); // 100ms
        tokenGenerator.init();
        String token = tokenGenerator.generateToken(user);

        Thread.sleep(101); // wait past expiration
        assertFalse(tokenValidator.validateToken(token));
        assertNull(tokenValidator.getUserId(token));
        assertNull(tokenValidator.getEmail(token));
    }
}