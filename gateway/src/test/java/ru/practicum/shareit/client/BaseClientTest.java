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
        server.expect(requestTo("http://localhost:9090/test"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        var response = baseClient.get("http://localhost:9090/test");
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testPost() {
        server.expect(requestTo("http://localhost:9090/test"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        var response = baseClient.post("http://localhost:9090/test", "body");
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testPatch() {
        server.expect(requestTo("http://localhost:9090/test"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        var response = baseClient.patch("http://localhost:9090/test", "body");
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testDelete() {
        server.expect(requestTo("http://localhost:9090/test"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        var response = baseClient.delete("http://localhost:9090/test");
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testGetWithError() {
        server.expect(requestTo("http://localhost:9090/test"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).body("Not found"));

        var response = baseClient.get("http://localhost:9090/test");
        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }
}