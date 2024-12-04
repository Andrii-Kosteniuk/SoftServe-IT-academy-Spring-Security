package com.softserve.itacademy.component.user;

import com.softserve.itacademy.config.SpringSecurityTestConfiguration;
import com.softserve.itacademy.config.WithMockCustomUser;
import com.softserve.itacademy.controller.UserController;
import com.softserve.itacademy.dto.userDto.CreateUserDto;
import com.softserve.itacademy.dto.userDto.UserDto;
import com.softserve.itacademy.dto.userDto.UserDtoConverter;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.model.UserRole;
import com.softserve.itacademy.repository.UserRepository;
import com.softserve.itacademy.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {UserController.class, SpringSecurityTestConfiguration.class, UserRepository.class})
@EnableMethodSecurity
public class UserControllerTest {

    @MockBean
    private UserService userService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mvc;

    @Autowired
    @Qualifier("withRoleAdmin")
    private User userWithRoleAdmin;

    @Autowired
    @Qualifier("withRoleUser")
    private User userWithRoleUser;
    @MockBean
    private UserDtoConverter userDtoConverter;

    @Test
    @WithMockCustomUser(email = "mike@mail.com", role = UserRole.ADMIN)
    public void shouldDisplayUserCreationForm() throws Exception {
        mvc.perform(get("/users/create")
                        .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("create-user"))
                .andExpect(model().size(1))
                .andExpect(model().attribute("user", new CreateUserDto()))
                .andDo(print());

        verifyNoInteractions(passwordEncoder, userService);
    }

    @Test
    @WithMockCustomUser(email = "mike@mail.com", role = UserRole.USER)
    public void shouldNotDisplayUserCreationForm() throws Exception {
        mvc.perform(get("/users/create")
                        .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isForbidden())
                .andDo(print());

        verifyNoInteractions(passwordEncoder, userService);
    }

    @Test
    @WithMockCustomUser(email = "mike@mail.com", role = UserRole.ADMIN)
    public void testCorrectCreatePostMethod() throws Exception {
        when(passwordEncoder.encode(anyString())).thenReturn("");
        when(userService.create(any(User.class))).thenReturn(new User());

        mvc.perform(post("/users/create")
                        .param("firstName", userWithRoleAdmin.getFirstName())
                        .param("lastName", userWithRoleAdmin.getLastName())
                        .param("email", userWithRoleAdmin.getEmail())
                        .param("password", userWithRoleAdmin.getPassword())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(model().hasNoErrors())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos/all/users/0"))
                .andDo(print());

        verify(userService, times(1)).create(any(User.class));
    }

    @Test
    @WithMockCustomUser(email = "mike@mail.com", role = UserRole.ADMIN)
    public void testErrorCreatePostMethod() throws Exception {
        User user = new User();
        user.setFirstName("");
        user.setLastName("");
        user.setEmail("");
        user.setPassword("");

        mvc.perform(post("/users/create")
                        .param("firstName", user.getFirstName())
                        .param("lastName", user.getLastName())
                        .param("email", user.getEmail())
                        .param("password", user.getPassword())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(model().hasErrors())
                .andExpect(status().isOk())
                .andExpect(view().name("create-user"))
                .andExpect(model().size(1))
                .andExpect(model().attribute("user", user))
                .andDo(print());

        verifyNoMoreInteractions(passwordEncoder, userService);
    }

    @Test
    @WithMockCustomUser(email = "mike@mail.com", role = UserRole.ADMIN)
    public void testReadGetMethod() throws Exception {
        when(userService.readById(anyLong())).thenReturn(userWithRoleAdmin);

        mvc.perform(get("/users/1/read")
                        .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("user-info"))
                .andExpect(model().size(1))
                .andExpect(model().attribute("user", userWithRoleAdmin))
                .andDo(print());

        verify(userService, times(1)).readById(anyLong());

        verifyNoMoreInteractions(passwordEncoder, userService);
    }

    @Test
    @WithMockCustomUser(email = "mike@mail.com", role = UserRole.ADMIN)
    public void testUpdateGetMethod() throws Exception {
        when(userService.readById(anyLong())).thenReturn(userWithRoleUser);

        mvc.perform(get("/users/1/update")
                        .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("update-user"))
                .andExpect(model().size(2))
                .andExpect(model().attribute("user", userWithRoleUser))
                .andExpect(model().attribute("roles", UserRole.values()))
                .andDo(print());

        verify(userService, times(1)).readById(anyLong());

        verifyNoMoreInteractions(passwordEncoder, userService);
    }

    @Test
    @WithMockCustomUser(email = "nick@mail.com")
    public void testCorrectUpdatePostMethodWithRoleUSERAndCorrectPassword() throws Exception {
        when(userService.findByUsername("nick@mail.com")).thenReturn(Optional.of(userWithRoleUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(userWithRoleUser);

        mvc.perform(post("/users/change-password")
                        .param("firstName", userWithRoleUser.getFirstName())
                        .param("lastName", userWithRoleUser.getLastName())
                        .param("email", userWithRoleUser.getEmail())
                        .param("oldPassword", "2222")
                        .param("newPassword", "newPassword")
                        .param("password", "newPassword")
                        .param("role", "USER")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED));

        verify(userService, times(1)).findByUsername("nick@mail.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @WithMockCustomUser(email = "mike@mail.com", role = UserRole.ADMIN)
    public void testCorrectUpdatePostMethodWithRoleADMINAndCorrectPassword() throws Exception {
        when(userService.findByUsername("mike@mail.com")).thenReturn(Optional.of(userWithRoleAdmin));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(userWithRoleAdmin);

        mvc.perform(post("/users/change-password")
                        .param("firstName", userWithRoleAdmin.getFirstName())
                        .param("lastName", userWithRoleAdmin.getLastName())
                        .param("email", userWithRoleAdmin.getEmail())
                        .param("oldPassword", "1111")
                        .param("newPassword", "newpassword")
                        .param("password", "newpassword")
                        .param("role", "ADMIN")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/all?success=true"))
                .andDo(print());

        verify(userService, times(1)).findByUsername("mike@mail.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @WithMockCustomUser(email = "mike@mail.com", role = UserRole.ADMIN)
    public void testErrorUpdatePostMethodWithRoleADMINAndInvalidPassword() throws Exception {
        when(userService.readById(anyLong())).thenReturn(userWithRoleAdmin);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(userWithRoleAdmin);
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(userWithRoleAdmin));

        mvc.perform(post("/users/change-password")
                        .param("firstName", userWithRoleAdmin.getFirstName())
                        .param("lastName", userWithRoleAdmin.getLastName())
                        .param("email", userWithRoleAdmin.getEmail())
                        .param("oldPassword", "3333")
                        .param("newPassword", "5555")
                        .param("password", userWithRoleAdmin.getPassword())
                        .param("role", "ADMIN")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/change-password?error=true"))
                .andDo(print());

        verify(userService, times(1)).findByUsername(anyString());
    }

    @Test
    @WithMockCustomUser(email = "mike@mail.com", role = UserRole.ADMIN)
    public void testErrorUpdatePostMethod() throws Exception {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setFirstName("");
        user.setLastName("");
        user.setEmail("");
        user.setRole(UserRole.ADMIN);

        when(userService.findByIdThrowing(anyLong())).thenReturn(user);

        mvc.perform(post("/users/1/update")
                        .param("firstName", user.getFirstName())
                        .param("lastName", user.getLastName())
                        .param("email", user.getEmail())
                        .param("role", "ADMIN")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(model().hasErrors())
                .andExpect(status().isOk())
                .andExpect(view().name("update-user"))
                .andExpect(model().size(2))
//                .andExpect(model().attribute("user", user)) // TODO
                .andExpect(model().attribute("roles", UserRole.values()))
                .andDo(print());

        verify(userService, times(1)).findByIdThrowing(anyLong());

        verifyNoMoreInteractions(passwordEncoder, userService);
    }

    @Test
    @WithMockCustomUser(id = 1, email = "mike@mail.com", role = UserRole.ADMIN)
    public void testDeleteGetMethodOneself() throws Exception {
        mvc.perform(get("/users/1/delete")
                        .contentType(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andDo(print());

        verify(userService, times(1)).delete(anyLong());
        verify(userService).getCurrentUser();

        verifyNoMoreInteractions(passwordEncoder, userService);
    }

    @Test
    @WithMockCustomUser(id = 1, email = "mike@mail.com", role = UserRole.ADMIN)
    public void testDeleteGetMethodAnotherUser() throws Exception {
        mvc.perform(get("/users/2/delete")
                        .contentType(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/all"))
                .andDo(print());

        verify(userService, times(1)).delete(anyLong());
        verify(userService).getCurrentUser();

        verifyNoMoreInteractions(passwordEncoder, userService);
    }

    @Test
    @WithMockCustomUser(email = "mike@mail.com", role = UserRole.ADMIN)
    public void testGetAllGetMethod() throws Exception {
        when(userService.getAll()).thenReturn(List.of(new User(), new User(), new User()));

        mvc.perform(get("/users/all")
                        .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("users-list"))
                .andExpect(model().size(2))
                .andExpect(model().attribute("users",
                        List.of(new User(), new User(), new User())))
                .andExpect(model().attribute("success", "Your password was successfully changed!"))
                .andDo(print());

        verify(userService, times(1)).getAll();

        verifyNoMoreInteractions(passwordEncoder, userService);
    }
}
