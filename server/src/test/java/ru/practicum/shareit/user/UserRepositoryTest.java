package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindByEmail() {
        User user = new User();
        user.setName("Дима");
        user.setEmail("dima@mail.ru");
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("dima@mail.ru");
        assertThat(exists).isTrue();
    }
}