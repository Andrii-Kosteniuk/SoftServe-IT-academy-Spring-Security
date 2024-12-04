package com.softserve.itacademy.controller;

import com.softserve.itacademy.config.security.WebAuthenticationToken;
import com.softserve.itacademy.model.Task;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.TaskService;
import com.softserve.itacademy.service.ToDoService;
import com.softserve.itacademy.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequestMapping("/todos")
@RequiredArgsConstructor
public class ToDoController {

    private final ToDoService todoService;
    private final TaskService taskService;
    private final UserService userService;

    // TODO: only if is owner
    @PreAuthorize("@securityService.isCurrentUserAndOwner(#ownerId)")
    @GetMapping("/create/users/{owner_id}")
    public String createToDoForm(@PathVariable("owner_id") long ownerId, Model model) {
        log.info("Accessing createToDoForm for ownerId: {}", ownerId);
        model.addAttribute("todo", new ToDo());
        model.addAttribute("ownerId", ownerId);
        log.debug("Model attributes set for createToDoForm");
        return "create-todo";
    }

    // TODO: only if is owner
    @PreAuthorize("@securityService.isCurrentUserAndOwner(#ownerId)")
    @PostMapping("/create/users/{owner_id}")
    public String createToDo(@PathVariable("owner_id") long ownerId,
                             @Validated @ModelAttribute("todo") ToDo todo, BindingResult result) {
        log.info("Creating ToDo for ownerId: {}, ToDo details: {}", ownerId, todo);
        if (result.hasErrors()) {
            log.warn("Validation errors in createToDo: {}", result.getAllErrors());
            return "create-todo";
        }
        todo.setCreatedAt(LocalDateTime.now());
        todo.setOwner(userService.readById(ownerId));
        todoService.create(todo);
        log.info("ToDo created successfully with id: {}", todo.getId());
        return "redirect:/todos/all/users/" + ownerId;
    }

    // TODO: only if is owner or collaborator
    @PreAuthorize("@securityService.isOwnerOrCollaborator(#id)")
    @GetMapping("/{id}/read")
    public String read(@PathVariable long id, Model model) {
        log.info("Reading ToDo with id: {}", id);
        ToDo todo = todoService.readById(id);
        List<Task> tasks = taskService.getByTodoId(id);
        List<User> users = userService.getAll().stream()
                .filter(user -> user.getId() != todo.getOwner().getId())
                .filter(user -> todo.getCollaborators().stream().allMatch((collaborator)
                        -> collaborator.getId() != user.getId()))
                .collect(Collectors.toList());
        model.addAttribute("todo", todo);
        model.addAttribute("tasks", tasks);
        model.addAttribute("users", users);
        log.debug("Read ToDo details: {}, tasks: {}, users: {}", todo, tasks, users);
        return "read-todo";
    }

    // TODO: only if is owner
    @PreAuthorize("@securityService.isCurrentUserAndOwner(#ownerId)")
    @GetMapping("/{todo_id}/update/users/{owner_id}")
    public String update(@PathVariable("todo_id") long todoId, @PathVariable("owner_id") long ownerId, Model model) {
        log.info("Accessing update form for ToDo with id: {}, ownerId: {}", todoId, ownerId);
        ToDo todo = todoService.readById(todoId);
        model.addAttribute("todo", todo);
        log.debug("Model attributes set for update: {}", todo);
        return "update-todo";
    }

    // TODO: only if is owner
    @PreAuthorize("@securityService.isCurrentUserAndOwner(#ownerId)")
    @PostMapping("/{todo_id}/update/users/{owner_id}")
    public String update(@PathVariable("todo_id") long todoId, @PathVariable("owner_id") long ownerId,
                         @Validated @ModelAttribute("todo") ToDo todo, BindingResult result, Model model) {
        log.info("Updating ToDo with id: {}, ownerId: {}", todoId, ownerId);
        if (result.hasErrors()) {
            todo.setOwner(userService.readById(ownerId));
            return "update-todo";
        }
        ToDo oldTodo = todoService.readById(todoId);
        todo.setOwner(oldTodo.getOwner());
        todo.setCollaborators(oldTodo.getCollaborators());
        todoService.update(todo);
        model.addAttribute(todo);
        log.info("ToDo updated successfully with id: {}", todo.getId());
        return "redirect:/todos/all/users/" + ownerId;
    }

    // TODO: only if is owner
    @PreAuthorize("@securityService.isCurrentUserAndOwner(#ownerId)")
    @GetMapping("/{todo_id}/delete/users/{owner_id}")
    public String delete(@PathVariable("todo_id") long todoId, @PathVariable("owner_id") long ownerId) {
        log.info("Deleting ToDo with id: {}, ownerId: {}", todoId, ownerId);
        todoService.delete(todoId);
        log.info("ToDo deleted successfully with id: {}", todoId);
        return "redirect:/todos/all/users/" + ownerId;
    }

    // TODO: only for currently log in user
    @PreAuthorize("@securityService.isCurrentUserAndOwner(#userId)")
    @GetMapping("/all/users/{user_id}")
    public String getAll(@PathVariable("user_id") long userId, Model model) {
        log.info("Fetching all ToDos for userId: {}", userId);
        List<ToDo> todos = todoService.getByUserId(userId);
        model.addAttribute("todos", todos);
        model.addAttribute("user", userService.readById(userId));
        model.addAttribute("success", "Your password was successfully changed!");
        log.debug("Fetched todos: {}", todos);
        return "read-user";
    }

    // TODO: only if is owner
    @PreAuthorize("@securityService.isTodoOwner(#todoId)")
    @GetMapping("/{todoId}/add")
    public String addCollaborator(@PathVariable long todoId, @RequestParam("user_id") long userId) {
        log.info("Adding collaborator with userId: {} to ToDo with id: {}", userId, todoId);
        ToDo todo = todoService.readById(todoId);
        List<User> collaborators = todo.getCollaborators();
        collaborators.add(userService.readById(userId));
        todo.setCollaborators(collaborators);
        todoService.update(todo);
        log.info("Collaborator added successfully");
        return "redirect:/todos/" + todoId + "/read";
    }

    // TODO: only if is owner
    @PreAuthorize("@securityService.isTodoOwner(#todoId)")
    @GetMapping("/{todoId}/remove")
    public String removeCollaborator(@PathVariable long todoId, @RequestParam("user_id") long userId) {
        log.info("Removing collaborator with userId: {} from ToDo with id: {}", userId, todoId);
        ToDo todo = todoService.readById(todoId);
        List<User> collaborators = todo.getCollaborators();
        collaborators.remove(userService.readById(userId));
        todo.setCollaborators(collaborators);
        todoService.update(todo);
        log.info("Collaborator removed successfully");
        return "redirect:/todos/" + todoId + "/read";
    }

    // Auxiliary method to be used in authorization rules
    public boolean canReadToDo(long todo_id) {
        WebAuthenticationToken authentication
                = (WebAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        User user = authentication.getUser();
        ToDo todo = todoService.readById(todo_id);
        boolean isCollaborator = todo.getCollaborators().stream().anyMatch((collaborator)
                -> collaborator.getId() == user.getId());
        return user.getId() == todo.getOwner().getId() || isCollaborator;
    }
}
