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
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    // TODO: for admins only
    @IsAdmin
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("user", new CreateUserDto());
        return "create-user";
    }


    // TODO: for admins only
    @IsAdmin
    @PostMapping("/create")
    public String create(@Validated @ModelAttribute("user") User user, BindingResult result) {
        if (result.hasErrors()) {
            return "create-user";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(UserRole.USER);
        User newUser = userService.create(user);
        return "redirect:/todos/all/users/" + newUser.getId();
    }

    // TODO: for admins and if requested info about current user
    @PreAuthorize("hasAnyAuthority('ADMIN') or @securityService.isCurrentUserAndOwner(#id)")
    @GetMapping("/{id}/read")
    public String read(@PathVariable long id, Model model) {
        User user = userService.readById(id);
        model.addAttribute("user", user);
        return "user-info";
    }

    // TODO: for admins and if requested info about current user
    @PreAuthorize("hasAnyAuthority('ADMIN') or @securityService.isCurrentUserAndOwner(#id)")
    @GetMapping("/{id}/update")
    public String update(@PathVariable long id, Model model) {
        User user = userService.readById(id);
        model.addAttribute("user", user);
        model.addAttribute("roles", UserRole.values());
        return "update-user";
    }

    // TODO: for admins and if updating info about current user
    @PreAuthorize("hasAnyAuthority('ADMIN') or @securityService.isCurrentUserAndOwner(#id)")
    @PostMapping("/{id}/update")
    public String update(@PathVariable long id, Model model,
                         @Validated @ModelAttribute("user") UpdateUserDto updateUserDto, BindingResult result) {
        UserDto oldUser = userService.findByIdThrowing(id);

        if (result.hasErrors()) {
            updateUserDto.setRole(oldUser.getRole());
            model.addAttribute("roles", UserRole.values());
            return "update-user";
        }

        userService.update(updateUserDto);
        return "redirect:/users/" + id + "/read";
    }

    // TODO: for admins or if deleting current user
    @PreAuthorize("hasAnyAuthority('ADMIN') or @securityService.isCurrentUserAndOwner(#id)")
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable("id") long id) {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getId() == id) {
            userService.delete(id);
            SecurityContextHolder.clearContext();
            return "redirect:/login";
        }
        userService.delete(id);
        return "redirect:/users/all";
    }

    // TODO: for admins only
    @IsAdmin
    @GetMapping("/all")
    public String getAll(Model model) {
        model.addAttribute("users", userService.getAll());
        model.addAttribute("success", "Your password was successfully changed!");
        return "users-list";
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        model.addAttribute("error", "You provide a wrong ald password. Please try again.");
        return "change-password";
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            Principal principal,
            Model model) {
        String name = principal.getName();
        var user = userService.findByUsername(name);

        if (user.isPresent()) {
            if (passwordEncoder.matches(oldPassword, user.get().getPassword())) {
                user.get().setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user.get());
                if (user.get().getRole().equals(UserRole.ADMIN)) {
                    return "redirect:/users/all?success=true";
                } else {
                    return "redirect:/todos/all/users/" + user.get().getId() + "?success=true";
                }

            }
        }

        return "redirect:/users/change-password?error=true";
    }
}