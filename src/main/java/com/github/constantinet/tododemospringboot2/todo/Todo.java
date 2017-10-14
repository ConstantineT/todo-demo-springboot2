package com.github.constantinet.tododemospringboot2.todo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "todos")
public final class Todo {

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;

    @Field
    private String description;

    @PersistenceConstructor
    @JsonCreator
    public Todo(@JsonProperty("id") final ObjectId id, @JsonProperty("description") final String description) {
        this.id = id;
        this.description = description;
    }

    public ObjectId getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}