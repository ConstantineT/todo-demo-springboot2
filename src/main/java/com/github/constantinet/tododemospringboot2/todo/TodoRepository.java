package com.github.constantinet.tododemospringboot2.todo;

import org.bson.types.ObjectId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoRepository extends ReactiveCrudRepository<Todo, ObjectId> {
}