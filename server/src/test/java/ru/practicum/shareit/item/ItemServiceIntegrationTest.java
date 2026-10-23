package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Test
    void testCreateAndGetItem() {
        UserDto userDto = new UserDto();
        userDto.setName("Владелец");
        userDto.setEmail("owner@mail.ru");
        UserDto owner = userService.create(userDto);

        ItemCreateDto createDto = new ItemCreateDto();
        createDto.setName("Дрель");
        createDto.setDescription("Мощная");
        createDto.setAvailable(true);

        ItemDto created = itemService.create(createDto, owner.getId());
        assertThat(created.getId()).isNotNull();

        ItemDto found = itemService.getById(created.getId());
        assertThat(found.getName()).isEqualTo("Дрель");
    }
}