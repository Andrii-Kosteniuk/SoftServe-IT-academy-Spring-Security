package com.softserve.itacademy.controller;

import com.softserve.itacademy.config.security.annotations.IsAdmin;
import com.softserve.itacademy.dto.userDto.CreateUserDto;
import com.softserve.itacademy.dto.userDto.UpdateUserDto;
import com.softserve.itacademy.dto.userDto.UserDto;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.model.UserRole;
import com.softserve.itacademy.repository.UserRepository;
import com.softserve.itacademy.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    // TODO: for admins only
    @IsAdmin
    @GetMapping("/create")
    public String create(Model model) {
        log.info("Accessing create user form.");
        model.addAttribute("user", new CreateUserDto());
        return "create-user";
    }


    // TODO: for admins only
    @IsAdmin
    @PostMapping("/create")
    public String create(@Validated @ModelAttribute("user") User user, BindingResult result) {
        log.info("Creating a new user: {}", user);
        if (result.hasErrors()) {
            log.warn("Validation errors while creating user: {}", result.getAllErrors());
            return "create-user";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(UserRole.USER);
        User newUser = userService.create(user);
        log.info("User created successfully with ID: {}", newUser.getId());
        return "redirect:/todos/all/users/" + newUser.getId();
    }

    // TODO: for admins and if requested info about current user
    @PreAuthorize("hasAnyAuthority('ADMIN') or @securityService.isCurrentUserAndOwner(#id)")
    @GetMapping("/{id}/read")
    public String read(@PathVariable long id, Model model) {
        log.info("Fetching details for user ID: {}", id);
        User user = userService.readById(id);
        model.addAttribute("user", user);
        log.debug("Fetched user details: {}", user);
        return "user-info";
    }

    // TODO: for admins and if requested info about current user
    @PreAuthorize("hasAnyAuthority('ADMIN') or @securityService.isCurrentUserAndOwner(#id)")
    @GetMapping("/{id}/update")
    public String update(@PathVariable long id, Model model) {
        log.info("Accessing update form for user ID: {}", id);
        User user = userService.readById(id);
        model.addAttribute("user", user);
        model.addAttribute("roles", UserRole.values());
        log.debug("Prepared model for update with roles: {}", UserRole.values());
        return "update-user";
    }

    // TODO: for admins and if updating info about current user
    @PreAuthorize("hasAnyAuthority('ADMIN') or @securityService.isCurrentUserAndOwner(#id)")
    @PostMapping("/{id}/update")
    public String update(@PathVariable long id, Model model,
                         @Validated @ModelAttribute("user") UpdateUserDto updateUserDto, BindingResult result) {
        log.info("Updating user with ID: {}", id);
        UserDto oldUser = userService.findByIdThrowing(id);

        if (result.hasErrors()) {
            updateUserDto.setRole(oldUser.getRole());
            model.addAttribute("roles", UserRole.values());

            return "update-user";
        }

        userService.update(updateUserDto);
        log.info("User updated successfully with ID: {}", id);
        return "redirect:/users/" + id + "/read";
    }

    // TODO: for admins or if deleting current user
    @PreAuthorize("hasAnyAuthority('ADMIN') or @securityService.isCurrentUserAndOwner(#id)")
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable("id") long id) {
        log.info("Deleting user with ID: {}", id);
        User currentUser = userService.getCurrentUser();
        if (currentUser.getId() == id) {
            log.info("Deleting currently logged-in user with ID: {}", id);
            userService.delete(id);
            SecurityContextHolder.clearContext();
            return "redirect:/login";
        }
        userService.delete(id);
        log.info("User deleted successfully with ID: {}", id);
        return "redirect:/users/all";
    }

    // TODO: for admins only
    @IsAdmin
    @GetMapping("/all")
    public String getAll(Model model) {
        log.info("Fetching all users.");
        model.addAttribute("users", userService.getAll());
        log.debug("All users fetched and added to model.");
        model.addAttribute("success", "Your password was successfully changed!");
        return "users-list";
    }


    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        log.info("Accessing change password form.");
        model.addAttribute("error", "You provide a wrong old password. Please try again.");
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            Principal principal) {

        String name = principal.getName();
        log.info("Processing password change request for user: {}", name);
        var user = userService.findByUsername(name);

        if (user.isPresent()) {
            log.debug("User found: {}", user.get().getId());
            if (passwordEncoder.matches(oldPassword, user.get().getPassword())) {
                user.get().setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user.get());
                if (user.get().getRole().equals(UserRole.ADMIN)) {
                    log.info("Password changed successfully for user: {}", name);
                    return "redirect:/users/all?success=true";
                } else if (user.get().getRole().equals(UserRole.USER)) {
                    log.info("Password changed successfully for user: {}", name);
                    return "redirect:/todos/all/users/" + user.get().getId() + "?success=true";
                }

            }
        }

        return "redirect:/users/change-password?error=true";
    }
}