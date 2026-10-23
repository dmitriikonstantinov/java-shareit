package ru.practicum.shareit.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class BaseClientTest {

    private BaseClient baseClient;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        baseClient = new BaseClient(restTemplate);
    }

    @Test
    void testGet() {
        server.expect(requestTo("/test"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        var response = baseClient.get("/test");
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testPost() {
        server.expect(requestTo("/test"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        var response = baseClient.post("/test", "body");
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testPatch() {
        server.expect(requestTo("/test"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        var response = baseClient.patch("/test", "body");
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testDelete() {
        server.expect(requestTo("/test"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        var response = baseClient.delete("/test");
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testGetWithError() {
        server.expect(requestTo("/test"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).body("Not found"));

        var response = baseClient.get("/test");
        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void testPut() {
        server.expect(requestTo("/test"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        var response = baseClient.put("/test", 1L, "body");
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testDeleteWithError() {
        server.expect(requestTo("/test"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).body("Not found"));

        var response = baseClient.delete("/test");
        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void testGetWithParams() {
        server.expect(requestTo("/test?param=value"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        var response = baseClient.get("/test?param={param}", 1L, java.util.Map.of("param", "value"));
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testGetWithUserIdAndParams() {
        server.expect(requestTo("/test?param=value"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        var response = baseClient.get("/test?param={param}", 1L, java.util.Map.of("param", "value"));
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testPostWithUserIdAndParams() {
        server.expect(requestTo("/test?param=value"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        var response = baseClient.post("/test?param={param}", 1L, java.util.Map.of("param", "value"), "body");
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testPatchWithUserIdAndParams() {
        server.expect(requestTo("/test?param=value"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        var response = baseClient.patch("/test?param={param}", 1L, java.util.Map.of("param", "value"), "body");
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testDeleteWithUserIdAndParams() {
        server.expect(requestTo("/test?param=value"))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess());

        var response = baseClient.delete("/test?param={param}", 1L, java.util.Map.of("param", "value"));
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testGetWithoutUserIdAndParams() {
        server.expect(requestTo("/test"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        var response = baseClient.get("/test");
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testPostWithoutUserIdAndParams() {
        server.expect(requestTo("/test"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        var response = baseClient.post("/test", "body");
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testGetWithoutUserId() {
        server.expect(requestTo("/test"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        var response = baseClient.get("/test");
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testGetWithErrorWithoutBody() {
        server.expect(requestTo("/test"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)); // Без body!

        var response = baseClient.get("/test");
        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }
}