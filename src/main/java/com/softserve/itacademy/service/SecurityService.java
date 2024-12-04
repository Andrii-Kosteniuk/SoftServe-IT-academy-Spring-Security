package com.softserve.itacademy.service;

import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityService {
    private final ToDoService toDoService;
    private final UserService userService;

    public boolean isTodoOwner(Long id) {
        User currentUser = userService.getCurrentUser();
        ToDo toDo = toDoService.readById(id);
        return toDo.getOwner().getId() == currentUser.getId();
    }

    public boolean isOwnerOrCollaborator(Long todoId) {
        User currentUser = userService.getCurrentUser();
        ToDo toDo = toDoService.readById(todoId);
        return toDo.getOwner().getId() == currentUser.getId()
               || toDo.getCollaborators().stream()
                       .anyMatch(collaborator -> collaborator.getId() == currentUser.getId());
    }

    public boolean isCurrentUserAndOwner(Long userId) {
        User currentUser = userService.getCurrentUser();
        return currentUser.getId() == userId;
    }

}
