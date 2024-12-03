package com.softserve.itacademy.component.todo;

import com.softserve.itacademy.config.SpringSecurityTestConfiguration;
import com.softserve.itacademy.config.WithMockCustomUser;
import com.softserve.itacademy.controller.ToDoController;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.model.UserRole;
import com.softserve.itacademy.service.TaskService;
import com.softserve.itacademy.service.ToDoService;
import com.softserve.itacademy.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {ToDoController.class, SpringSecurityTestConfiguration.class})
public class ToDoControllerTest {

    @MockBean
    private ToDoService toDoService;
    @MockBean
    private TaskService taskService;
    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockCustomUser(email = "owner@mail.com", role = UserRole.USER)
    public void shouldDisplayCreateToDoFormForOwner() throws Exception {
        long ownerId = 1L;

        mvc.perform(get("/todos/create/users/{owner_id}", ownerId)
                        .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("create-todo"))
                .andExpect(model().size(2))
                .andExpect(model().attributeExists("todo", "ownerId"))
                .andDo(print());

        verifyNoInteractions(toDoService, userService, taskService);
    }

    @Test
    @WithMockCustomUser(email = "user@mail.com", role = UserRole.USER)
    public void shouldCreateToDoSuccessfully() throws Exception {
        long ownerId = 1L;
        ToDo todo = new ToDo();
        todo.setId(1L);
        todo.setTitle("New ToDo");
        todo.setCreatedAt(LocalDateTime.now());

        when(userService.readById(ownerId)).thenReturn(new User());
        when(toDoService.create(Mockito.any(ToDo.class))).thenReturn(todo);

        mvc.perform(post("/todos/create/users/{owner_id}", ownerId)
                        .param("title", todo.getTitle())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos/all/users/" + ownerId))
                .andDo(print());

        verify(userService, times(1)).readById(ownerId);
        verify(toDoService, times(1)).create(Mockito.any(ToDo.class));
        verifyNoMoreInteractions(toDoService, userService, taskService);
    }

    @Test
    @WithMockCustomUser(email = "user@mail.com", role = UserRole.USER)
    public void shouldFailToCreateToDoWithoutTitle() throws Exception {
        long ownerId = 1L;

        mvc.perform(post("/todos/create/users/{owner_id}", ownerId)
                        .param("title", "")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("create-todo"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeErrorCount("todo", 1))
                .andDo(print());

        verifyNoInteractions(toDoService, userService, taskService);
    }


    @Test
    @WithMockCustomUser(email = "owner@mail.com", role = UserRole.USER)
    public void shouldDeleteToDoForOwner() throws Exception {
        long todoId = 1L;
        long ownerId = 1L;

        mvc.perform(get("/todos/{todo_id}/delete/users/{owner_id}", todoId, ownerId)
                        .contentType(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos/all/users/" + ownerId))
                .andDo(print());

        verify(toDoService, times(1)).delete(todoId);
        verifyNoMoreInteractions(toDoService);
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void whenCsrfTokenIsMissing_thenPostRequestFails() throws Exception {
        mvc.perform(post("/todos/create/users/1")
                        .param("title", "New Todo"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void whenCsrfTokenIsProvided_thenPostRequestSucceeds() throws Exception {
        User owner = new User();
        owner.setId(1L);

        when(userService.readById(1L)).thenReturn(owner);

        mvc.perform(post("/todos/create/users/1")
                        .with(csrf())
                        .param("title", "New Todo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos/all/users/1"));
    }
}
