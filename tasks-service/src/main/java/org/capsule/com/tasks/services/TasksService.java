package org.capsule.com.tasks.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.capsule.com.tasks.dtos.errors.CustomError;
import org.capsule.com.tasks.dtos.requests.TaskIdReqst;
import org.capsule.com.tasks.dtos.responses.ListOfTasksResp;
import org.capsule.com.tasks.models.Session;
import org.capsule.com.tasks.models.Task;
import org.capsule.com.tasks.utils.exceptions.FieldsNotValidException;
import org.capsule.com.tasks.utils.mappers.TasksMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
@RequiredArgsConstructor
public class TasksService implements ITasksService<TaskIdReqst, ListOfTasksResp> {

    private final Logger LOGGER = LoggerFactory.getLogger(TasksService.class);
    private final DecodeJwtService decodeJwtService;
    private final TasksDBService tasksDBService;
    private final TasksMapper tasksMapper;

    public HttpStatus startTask(String token, TaskIdReqst request, BindingResult bindingResult) {
        process(bindingResult, token, request, Session.Status.IN_PROGRESS);

        return HttpStatus.CREATED;
    }

    public HttpStatus completeTask(String token, TaskIdReqst request, BindingResult bindingResult) {
        process(bindingResult, token, request, Session.Status.COMPLETED);

        return HttpStatus.OK;
    }

    public HttpStatus skipTask(String token, TaskIdReqst request, BindingResult bindingResult) {
        process(bindingResult, token, request, Session.Status.SKIPPED);

        return HttpStatus.OK;
    }

    //достать все сессии пользователя -> найти сессии на сегодня -> если таких нет создать
    public ResponseEntity<ListOfTasksResp> get(String token) {
        String username = extractUsernameFromToken(token);
        List<Session> sessions = tasksDBService.findAllSessionsByUsername(username);
        List<Session> sessionToday = sessions.stream()
            .filter(session -> {
                LocalDateTime created = session.getCreatedAt();

                LocalDate today = LocalDate.now();
                LocalDateTime at3AM = today.atTime(3, 0);

                return created.isAfter(at3AM);
            })
            .toList();

        if (!sessionToday.isEmpty()) {
            List<Task> tasks = sessionToday.stream()
                .map(Session::getTask)
                .toList();
            return response(tasks);
        }

        List<Task> tasks = tasksDBService.getThreeRandomTasks();
        tasks.forEach(task -> taskManageManipulation(Session.Status.ASSIGNED, username, task));
        return response(tasks);
    }

    private ResponseEntity<ListOfTasksResp> response(List<Task> tasks) {
        return new ResponseEntity<>(new ListOfTasksResp(tasks.stream()
            .map(tasksMapper::toDto)
            .toList()), HttpStatus.OK);
    }

    private void process(BindingResult bindingResult, String token, TaskIdReqst request,
        Session.Status status) {
        validate(bindingResult);
        String username = extractUsernameFromToken(token);
        Task task = tasksDBService.findById(request.taskId());
        taskManageManipulation(status, username, task);
    }

    private void taskManageManipulation(Session.Status status, String username, Task task) {
        Session manager = createTaskManage(username, task, status);
        tasksDBService.save(manager);
    }

    private Session createTaskManage(String username, Task task, Session.Status status) {
        return Session.builder()
            .task(task)
            .username(username)
            .status(status)
            .build();
    }

    private void validate(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<CustomError> errors = bindingResult.getFieldErrors()
                .stream()
                .map(error -> new CustomError(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

            throw new FieldsNotValidException(errors);
        }
    }

    private String extractUsernameFromToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        try {
            return decodeJwtService.extractUsername(token);
        } catch (Exception ex) {
            LOGGER.error("Error extracting username from token", ex);
            throw new RuntimeException("Failed to extract username from token", ex);
        }
    }
}