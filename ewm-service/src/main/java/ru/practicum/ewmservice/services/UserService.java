package ru.practicum.ewmservice.services;

import ru.practicum.ewmservice.models.user.dto.NewUserRequest;
import ru.practicum.ewmservice.models.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto addNewUser(NewUserRequest newUser);

    List<UserDto> getUsers(int from, int size, Long[] ids);

    void deleteUser(Long userId);
}
