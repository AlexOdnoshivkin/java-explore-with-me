package ru.practicum.ewmservice.services;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.exceptions.DataConflictException;
import ru.practicum.ewmservice.models.user.dto.NewUserRequest;
import ru.practicum.ewmservice.models.user.dto.UserDto;

import javax.persistence.EntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplTest {
    private final EntityManager em;

    private final UserService userService;

    @AfterEach
    void afterEach() {
        em.createNativeQuery("truncate table users");
    }

    @Test
    void addNewUserWhenValidData() {
        // Проверка корректного сценария
        NewUserRequest newUserRequest = new NewUserRequest();
        newUserRequest.setName("Test User");
        newUserRequest.setEmail("Test@gmail.com");

        UserDto userDto = userService.addNewUser(newUserRequest);

        assertEquals(newUserRequest.getName(), userDto.getName());
        assertEquals(newUserRequest.getEmail(), userDto.getEmail());
    }

    @Test
    void addNewUserWhenUserNameIsUsedThenThrowException() {
        // Проверка случая, когда имя пользователя уже занято
        NewUserRequest newUserRequest = new NewUserRequest();
        newUserRequest.setName("Test User");
        newUserRequest.setEmail("Test@gmail.com");
        userService.addNewUser(newUserRequest);

        NewUserRequest anotherNewUserRequest = new NewUserRequest();
        anotherNewUserRequest.setName("Test User");
        anotherNewUserRequest.setEmail("AnotherMail@gmail.com");

        DataConflictException thrown = Assertions
                .assertThrows(DataConflictException.class, () ->
                        userService.addNewUser(anotherNewUserRequest));

        assertEquals("Пользователь с именем Test User уже существует", thrown.getMessage());
    }

    @Test
    void getUsers() {
        NewUserRequest newUserRequest = new NewUserRequest();
        newUserRequest.setName("Test User");
        newUserRequest.setEmail("Test@gmail.com");
        UserDto userDto = userService.addNewUser(newUserRequest);

        List<UserDto> result = userService.getUsers(0, 10, new Long[]{userDto.getId()});

        assertEquals(1, result.size());
        assertEquals(userDto, result.get(0));
    }

    @Test
    void deleteUser() {
        NewUserRequest newUserRequest = new NewUserRequest();
        newUserRequest.setName("Test User");
        newUserRequest.setEmail("Test@gmail.com");
        UserDto userDto = userService.addNewUser(newUserRequest);

        userService.deleteUser(userDto.getId());

        List<UserDto> result = userService.getUsers(0, 10, new Long[]{userDto.getId()});

        assertEquals(0, result.size());
    }


}