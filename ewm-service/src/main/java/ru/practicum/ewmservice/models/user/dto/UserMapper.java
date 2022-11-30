package ru.practicum.ewmservice.models.user.dto;

import ru.practicum.ewmservice.models.user.User;

public class UserMapper {
    //Подавление конструктора по умолчанию для достижения неинстанцируемости
    private UserMapper() {
        throw new AssertionError();
    }

    public static User mapToUserFromNewUserRequest(NewUserRequest newUserRequest) {
        User user = new User();
        user.setEmail(newUserRequest.getEmail());
        user.setName(newUserRequest.getName());
        return user;
    }

    public static UserDto mapToUserDtoFromUser(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setName(user.getName());
        return userDto;
    }

    public static UserShortDto mapToUserShortDtoFromUser(User user) {
        UserShortDto userShortDto = new UserShortDto();
        userShortDto.setId(user.getId());
        userShortDto.setName(user.getName());
        return userShortDto;
    }
}
