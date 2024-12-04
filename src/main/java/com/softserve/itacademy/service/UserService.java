package com.softserve.itacademy.service;

import com.softserve.itacademy.config.security.WebAuthenticationToken;
import com.softserve.itacademy.dto.userDto.UpdateUserDto;
import com.softserve.itacademy.model.UserRole;
import com.softserve.itacademy.dto.userDto.UserDto;
import com.softserve.itacademy.dto.userDto.UserDtoConverter;
import com.softserve.itacademy.config.exception.NullEntityReferenceException;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserDtoConverter userDtoConverter;

    public User create(User role) {
        if (role != null) {
            return userRepository.save(role);
        }
        throw new NullEntityReferenceException("User cannot be 'null'");
    }

    public User readById(long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("User with id " + id + " not found"));
    }

    public UserDto update(UpdateUserDto updateUserDto) {
        User user = userRepository.findById(updateUserDto.getId()).orElseThrow(EntityNotFoundException::new);
        if (user.getRole() == UserRole.ADMIN) {
            user.setRole(updateUserDto.getRole());
        }
        userDtoConverter.fillFields(user, updateUserDto);
        userRepository.save(user);
        return userDtoConverter.toDto(user);
    }

    public void delete(long id) {
        User user = readById(id);
        userRepository.delete(user);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByEmail(username);
    }

    public User getCurrentUser() {
        WebAuthenticationToken authentication
                = (WebAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getDetails();
    }

    public Optional<UserDto> findById(long id) {
        return userRepository.findById(id).map(userDtoConverter::toDto);
    }

    public UserDto findByIdThrowing(long id) {
        return userRepository.findById(id).map(userDtoConverter::toDto).orElseThrow(EntityNotFoundException::new);
    }

    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(userDtoConverter::toDto).toList();
    }

}
