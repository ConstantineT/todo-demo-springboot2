package com.github.constantinet.tododemospringboot2.todo;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
public class TodoController {

    private final TodoRepository todoRepository;

    @Autowired
    public TodoController(final TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @GetMapping(value = "/todo")
    public Flowable<Todo> getTodos() {
        return todoRepository.findAll()
                .zipWith(Flowable.interval(1, TimeUnit.SECONDS), (todo, l) -> todo);// simulates a delay
    }

    @GetMapping(value = "/todo/{id}")
    public Maybe<Todo> getTodo(@PathVariable("id") final String id) {
        return todoRepository.findById(new ObjectId(id));
    }

    @PostMapping(value = "/todo", consumes = APPLICATION_JSON_UTF8_VALUE)
    public Single<Todo> createTodo(@RequestBody final Todo todo) {
        return todoRepository.save(todo);
    }

    @DeleteMapping(value = "/todo/{id}")
    public Completable deleteTodo(@PathVariable("id") final String id) {
        return todoRepository.deleteById(new ObjectId(id));
    }
}