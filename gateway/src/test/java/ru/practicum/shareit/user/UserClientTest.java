package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(UserClient.class)
class UserClientTest {

    @Autowired
    private UserClient userClient;

    @Autowired
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        server.reset();
    }

    @Test
    void testCreateUser() {
        UserDto dto = new UserDto();
        dto.setName("Дима");
        dto.setEmail("dima@mail.ru");

        server.expect(requestTo("http://localhost:9090/users"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Дима"))
                .andRespond(withSuccess("{\"id\":1,\"name\":\"Дима\",\"email\":\"dima@mail.ru\"}", MediaType.APPLICATION_JSON));

        var response = userClient.createUser(dto);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void testGetUser() {
        server.expect(requestTo("http://localhost:9090/users/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":1,\"name\":\"Дима\"}", MediaType.APPLICATION_JSON));

        var response = userClient.getUser(1L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testGetAllUsers() {
        server.expect(requestTo("http://localhost:9090/users"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        var response = userClient.getAllUsers();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testUpdateUser() {
        UserDto dto = new UserDto();
        dto.setName("Новое имя");

        server.expect(requestTo("http://localhost:9090/users/1"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("{\"id\":1,\"name\":\"Новое имя\"}", MediaType.APPLICATION_JSON));

        var response = userClient.updateUser(1L, dto);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testDeleteUser() {
        server.expect(requestTo("http://localhost:9090/users/1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        var response = userClient.deleteUser(1L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }
}