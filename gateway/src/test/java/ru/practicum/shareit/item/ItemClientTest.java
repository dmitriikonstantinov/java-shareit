package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(ItemClient.class)
class ItemClientTest {

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void testCreateItem() {
        ItemCreateDto dto = new ItemCreateDto();
        dto.setName("Дрель");
        dto.setDescription("Мощная");
        dto.setAvailable(true);

        server.expect(requestTo("http://localhost:9090/items"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess("{\"id\":1,\"name\":\"Дрель\"}", MediaType.APPLICATION_JSON));

        var response = itemClient.createItem(1L, dto);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testGetItem() {
        server.expect(requestTo("http://localhost:9090/items/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        var response = itemClient.getItem(1L, 1L);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testGetAllItems() {
        server.expect(requestTo("http://localhost:9090/items"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        var response = itemClient.getAllItems(1L);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testUpdateItem() {
        ItemCreateDto dto = new ItemCreateDto();
        dto.setName("Новое имя");

        server.expect(requestTo("http://localhost:9090/items/1"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        var response = itemClient.updateItem(1L, 1L, dto);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testDeleteItem() {
        server.expect(requestTo("http://localhost:9090/items/1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        var response = itemClient.deleteItem(1L, 1L);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testSearchItems() {
        server.expect(requestTo("http://localhost:9090/items/search?text=drill"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        var response = itemClient.searchItems(1L, "drill");
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testAddComment() {
        CommentDto dto = new CommentDto();
        dto.setText("Отличная дрель");

        server.expect(requestTo("http://localhost:9090/items/1/comment"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        var response = itemClient.addComment(1L, 1L, dto);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testGetItemNotFound() {
        server.expect(requestTo("http://localhost:9090/items/999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).body("Not found"));

        var response = itemClient.getItem(1L, 999L);
        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void testGetItemServerError() {
        server.expect(requestTo("http://localhost:9090/items/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error"));

        var response = itemClient.getItem(1L, 1L);
        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }
}