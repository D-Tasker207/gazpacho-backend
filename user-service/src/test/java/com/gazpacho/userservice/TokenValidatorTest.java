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

    private final String testAccessSecret = "thisisnottheactualsecurekey12345"; // 32+ chars
    private final String testRefreshSecret = "thisisalsoafakekeybutforrefreshing"; // 32+ chars

    private UserEntity user;

    @BeforeEach
    public void setup() {
        tokenGenerator = new TokenGenerator();
        tokenValidator = new TokenValidator();

        // Inject secret key manually (since @Value won't work outside Spring context)
        ReflectionTestUtils.setField(tokenGenerator, "accessSecret", testAccessSecret);
        ReflectionTestUtils.setField(tokenGenerator, "accessExpTimeMillis", 3600000L);
        ReflectionTestUtils.setField(tokenGenerator, "refreshSecret", testRefreshSecret);
        ReflectionTestUtils.setField(tokenGenerator, "refreshExpTimeMillis", 604800000L); // 7 days

        ReflectionTestUtils.setField(tokenValidator, "refreshSecret", testRefreshSecret);
        ReflectionTestUtils.setField(tokenValidator, "accessSecret", testAccessSecret);

        tokenGenerator.init();
        tokenValidator.init();

        user = new UserEntity();
        user.setId(1L);
    }

    @Test
    public void testValidAccessToken() {
        String token = tokenGenerator.generateAccessToken(user);

        assertTrue(tokenValidator.validateAccessToken(token));
        assertEquals(1L, tokenValidator.getUserIdFromAccessToken(token));
    }

    @Test
    public void testInvalidAccessToken() {
        String invalidToken = "not.a.valid.token";

        assertFalse(tokenValidator.validateAccessToken(invalidToken));
        assertNull(tokenValidator.getUserIdFromAccessToken(invalidToken));
    }

    @Test
    public void testExpiredAccessToken() throws InterruptedException {
        // Short-lived token generator
        TokenGenerator shortTokenGenerator = new TokenGenerator();
        ReflectionTestUtils.setField(shortTokenGenerator, "accessSecret", testAccessSecret);
        ReflectionTestUtils.setField(shortTokenGenerator, "accessExpTimeMillis", 100);
        ReflectionTestUtils.setField(shortTokenGenerator, "refreshSecret", testRefreshSecret);
        shortTokenGenerator.init();

        String token = shortTokenGenerator.generateAccessToken(user);

        Thread.sleep(200); // wait for token to expire

        assertFalse(tokenValidator.validateAccessToken(token));
        assertNull(tokenValidator.getUserIdFromAccessToken(token));
    }

    @Test
    public void testTamperedAccessToken() {
        String token = tokenGenerator.generateAccessToken(user);
        String tampered = token.substring(0, token.length() - 1) + "X";

        assertFalse(tokenValidator.validateAccessToken(tampered));
        assertNull(tokenValidator.getUserIdFromAccessToken(tampered));
    }

    @Test
    public void testAccessTokenWithWrongSecret() {
        TokenValidator otherValidator = new TokenValidator();
        ReflectionTestUtils.setField(otherValidator, "accessSecret", "thisisadifferentsecretkey1234567");
        ReflectionTestUtils.setField(otherValidator, "refreshSecret", testRefreshSecret);
        otherValidator.init();

        String token = tokenGenerator.generateAccessToken(user);
        assertFalse(otherValidator.validateAccessToken(token));
        assertNull(otherValidator.getUserIdFromAccessToken(token));
    }

    @Test
    public void testJustExpiredAccessToken() throws InterruptedException {
        // Short-lived token generator
        TokenGenerator otherGenerator = new TokenGenerator();
        ReflectionTestUtils.setField(otherGenerator, "accessSecret", testAccessSecret);
        ReflectionTestUtils.setField(otherGenerator, "accessExpTimeMillis", 100); // 100ms
        ReflectionTestUtils.setField(otherGenerator, "refreshSecret", testRefreshSecret);
        otherGenerator.init();
        String token = otherGenerator.generateAccessToken(user);

        Thread.sleep(101); // wait past expiration
        assertFalse(tokenValidator.validateAccessToken(token));
        assertNull(tokenValidator.getUserIdFromAccessToken(token));
    }

    // ==== REFRESH TOKEN TESTS ====

    @Test
    public void testValidRefreshToken() {
        String refreshToken = tokenGenerator.generateRefreshToken(user);

        assertTrue(tokenValidator.validateRefreshToken(refreshToken));
        assertEquals(1L, tokenValidator.getUserIdFromRefreshToken(refreshToken));
    }

    @Test
    public void testInvalidRefreshToken() {
        String invalidToken = "not.a.valid.token";

        assertFalse(tokenValidator.validateRefreshToken(invalidToken));
        assertNull(tokenValidator.getUserIdFromRefreshToken(invalidToken));
    }

    @Test
    public void testTamperedRefreshToken() {
        String token = tokenGenerator.generateRefreshToken(user);
        String tampered = token.substring(0, token.length() - 1) + "X";

        assertFalse(tokenValidator.validateRefreshToken(tampered));
        assertNull(tokenValidator.getUserIdFromRefreshToken(tampered));
    }

    @Test
    public void testRefreshTokenWithWrongSecret() {
        TokenValidator otherValidator = new TokenValidator();
        ReflectionTestUtils.setField(otherValidator, "refreshSecret", "differentrefreshsecretkey1234567");
        ReflectionTestUtils.setField(otherValidator, "accessSecret", testAccessSecret);
        otherValidator.init();

        String token = tokenGenerator.generateRefreshToken(user);
        assertFalse(otherValidator.validateRefreshToken(token));
        assertNull(otherValidator.getUserIdFromRefreshToken(token));
    }

    @Test
    public void testExpiredRefreshToken() throws InterruptedException {
        TokenGenerator shortTokenGenerator = new TokenGenerator();
        ReflectionTestUtils.setField(shortTokenGenerator, "accessSecret", testAccessSecret);
        ReflectionTestUtils.setField(shortTokenGenerator, "refreshSecret", testRefreshSecret);
        ReflectionTestUtils.setField(shortTokenGenerator, "refreshExpTimeMillis", 100);
        shortTokenGenerator.init();

        String token = shortTokenGenerator.generateRefreshToken(user);

        Thread.sleep(150);
        assertFalse(tokenValidator.validateRefreshToken(token));
        assertNull(tokenValidator.getUserIdFromRefreshToken(token));
    }

    @Test
    public void testRefreshTokenWithAccessKey() {
        String token = tokenGenerator.generateAccessToken(user);
        assertFalse(tokenValidator.validateRefreshToken(token));
        assertNull(tokenValidator.getUserIdFromRefreshToken(token));
    }

    @Test
    public void testAccessTokenWithRefreshKey() {
        String token = tokenGenerator.generateRefreshToken(user);
        assertFalse(tokenValidator.validateAccessToken(token));
        assertNull(tokenValidator.getUserIdFromAccessToken(token));
    }
}