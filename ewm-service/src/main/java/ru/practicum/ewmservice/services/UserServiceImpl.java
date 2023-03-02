package ru.practicum.ewmservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewmservice.FromSizeRequest;
import ru.practicum.ewmservice.exceptions.DataConflictException;
import ru.practicum.ewmservice.exceptions.EntityNotFoundException;
import ru.practicum.ewmservice.models.user.User;
import ru.practicum.ewmservice.models.user.dto.NewUserRequest;
import ru.practicum.ewmservice.models.user.dto.UserDto;
import ru.practicum.ewmservice.models.user.dto.UserMapper;
import ru.practicum.ewmservice.repositories.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto addNewUser(NewUserRequest newUser) {
        Optional<User> userOptional = userRepository.getUserByName(newUser.getName());
        if (userOptional.isPresent()) {
            throw new DataConflictException("Пользователь с именем " + newUser.getName() + " уже существует");
        }
        User user = UserMapper.mapToUserFromNewUserRequest(newUser);
        User savedUser = userRepository.save(user);
        log.debug("Пользователь {} сохранён в базе данных", savedUser);
        return UserMapper.mapToUserDtoFromUser(savedUser);
    }

    @Override
    public List<UserDto> getUsers(int from, int size, Long[] ids) {
        Pageable pageable = FromSizeRequest.of(from, size);
        List<UserDto> result = userRepository.getUsers(ids, pageable).stream()
                .map(UserMapper::mapToUserDtoFromUser)
                .collect(Collectors.toList());
        log.debug("Получен список пользователей из базы данных {}", result);
        return result;
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        checkUserInDatabase(userId);
        userRepository.deleteById(userId);
        log.debug("Пользователь с id {} удалён из базы данных", userId);
    }

    public User checkUserInDatabase(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new EntityNotFoundException("Пользователь с id " + userId + " не найден в базе данных");
        }
        return userOptional.get();
    }
}
