package com.gazpacho.apigateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.junit.jupiter.api.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class ApiGatewayApplicationTest {
    @Autowired
    private WebTestClient webTestClient;

    private static MockWebServer userServiceMock;
    private static MockWebServer recipeServiceMock;

    @BeforeAll
    static void setUp() throws Exception {
        userServiceMock = new MockWebServer();
        recipeServiceMock = new MockWebServer();

        userServiceMock.start(8081);
        recipeServiceMock.start(8082);

        userServiceMock.enqueue(new MockResponse().setResponseCode(200).setBody("User Ok"));

        recipeServiceMock.enqueue(new MockResponse().setResponseCode(200).setBody("Recipe Ok"));
    }

    @AfterAll
    static void tearDown() throws Exception {
        userServiceMock.shutdown();
        recipeServiceMock.shutdown();
    }

    @Test
    void shouldRouteToUserService() {
        webTestClient.get()
                .uri("/users/test")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("User Ok");
    }

    @Test
    void shouldRouteToRecipeService() {
        webTestClient.get()
                .uri("/recipes/test")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Recipe Ok");
    }

    @Test
    void shouldReturnNotFoundForUnknownRoute() {
        webTestClient.get()
                .uri("/invalid-route")
                .exchange()
                .expectStatus().isNotFound();
    }
}
