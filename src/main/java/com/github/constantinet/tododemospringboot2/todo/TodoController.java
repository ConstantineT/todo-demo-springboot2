package com.github.constantinet.tododemospringboot2.todo;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class TodoController {

    private final TodoRepository todoRepository;

    @Autowired
    public TodoController(final TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @GetMapping(value = "/todo")
    public Iterable<Todo> getTodos() {
        return todoRepository.findAll();
    }

    @GetMapping(value = "/todo/{id}")
    public Optional<Todo> getTodo(@PathVariable("id") final String id) {
        return todoRepository.findById(new ObjectId(id));
    }

    @PostMapping(value = "/todo", consumes = APPLICATION_JSON_VALUE)
    public Todo createTodo(@RequestBody final Todo todo) {
        return todoRepository.save(todo);
    }

    @DeleteMapping(value = "/todo/{id}")
    public void deleteTodo(@PathVariable("id") final String id) {
        todoRepository.deleteById(new ObjectId(id));
    }
}