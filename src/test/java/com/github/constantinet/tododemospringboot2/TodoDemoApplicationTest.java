package com.github.constantinet.tododemospringboot2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.constantinet.tododemospringboot2.todo.Todo;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TodoDemoApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate template;

    private Todo todo1;
    private Todo todo2;

    @Before
    public void setUp() {
        todo1 = new Todo(ObjectId.get(), 1, "Order a pizza", LocalDate.of(2020, 1, 1));
        todo2 = new Todo(ObjectId.get(), 2, "Eat the pizza", LocalDate.of(2020, 1, 2));

        template.dropCollection(Todo.class);
        template.createCollection(Todo.class);
        template.insertAll(Arrays.asList(todo1, todo2));
    }

    @Test
    public void testGetTodos_shouldReturnCorrectJsonStream_whenRequestPassed() throws Exception {
        this.mockMvc
                .perform(get("/todo"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(is(todo1.getId().toHexString())))
                .andExpect(jsonPath("$[1].id").value(is(todo2.getId().toHexString())));
    }

    @Test
    public void testGetTodo_shouldReturnCorrectJson_whenRequestWithExistingIdSent() throws Exception {
        this.mockMvc
                .perform(get("/todo/" + todo2.getId().toHexString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(is(todo2.getId().toHexString())));
    }

    @Test
    public void testCreateTodo_shouldReturnCorrectJsonAndCreateRecord_whenRequestWithCorrectBodySent() throws Exception {
        final Todo todo = new Todo(ObjectId.get(), 2, "Take a pizza", LocalDate.of(2020, 1, 3));

        this.mockMvc
                .perform(post("/todo")
                        .content(new ObjectMapper().writeValueAsString(todo))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(is(todo.getId().toHexString())));

        assertThat(template.findById(todo.getId(), Todo.class),
                hasProperty("id", is(todo.getId())));
    }

    @Test
    public void testDeleteTodo_shouldDeleteRecord_whenRequestWithExistingIdSent() throws Exception {
        this.mockMvc
                .perform(delete("/todo/" + todo2.getId().toHexString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}