package com.github.constantinet.tododemospringboot2.todo;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
public class TodoController {

    private final TodoRepository todoRepository;

    @Autowired
    public TodoController(final TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @GetMapping(value = "/todo")
    public Flux<Todo> getTodos() {
        return todoRepository.findAll()
                .zipWith(Flux.interval(Duration.ZERO, Duration.ofSeconds(1)), (todo, l) -> todo);// simulates a delay
    }

    @GetMapping(value = "/todo/{id}")
    public Mono<Todo> getTodo(@PathVariable("id") final String id) {
        return todoRepository.findById(new ObjectId(id));
    }

    @PostMapping(value = "/todo", consumes = APPLICATION_JSON_UTF8_VALUE)
    public Mono<Todo> createTodo(@RequestBody final Todo todo) {
        return todoRepository.save(todo);
    }
}