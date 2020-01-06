package com.github.constantinet.tododemospringboot2;

import com.github.constantinet.tododemospringboot2.todo.Todo;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TodoDemoApplicationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReactiveMongoTemplate template;

    private Todo todo1;
    private Todo todo2;

    @Before
    public void setUp() {
        todo1 = new Todo(ObjectId.get(), 1, "Order a pizza", LocalDate.of(2020, 1, 1));
        todo2 = new Todo(ObjectId.get(), 2, "Eat the pizza", LocalDate.of(2020, 1, 2));

        template.collectionExists(Todo.class)
                .flatMap(exists -> exists ? template.dropCollection(Todo.class) : Mono.just(exists))
                .flatMap(exists -> template.createCollection(Todo.class))
                .then()
                .block();
        template.insertAll(Flux.just(todo1, todo2).collectList())
                .then()
                .block();
    }

    @Test
    public void testGetTodos_shouldReturnCorrectJsonStream_whenRequestForJsonStreamSent() {
        final FluxExchangeResult<Todo> result = webTestClient.get().uri("/todo")
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Todo.class);
        StepVerifier.create(result.getResponseBody())
                .consumeNextWith(todo -> assertThat(todo, hasProperty("id", is(todo1.getId()))))
                .consumeNextWith(todo -> assertThat(todo, hasProperty("id", is(todo2.getId()))))
                .thenCancel()
                .verify();
    }

    @Test
    public void testGetTodo_shouldReturnCorrectJson_whenRequestWithExistingIdSent() {
        webTestClient.get().uri("/todo/" + todo2.getId().toHexString())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Todo.class)
                .consumeWith(todo -> assertThat(todo.getResponseBody(), hasProperty("id", is(todo2.getId()))));
    }

    @Test
    public void testCreateTodo_shouldReturnCorrectJsonAndCreateRecord_whenRequestWithCorrectBodySent() {
        final Todo todo = new Todo(ObjectId.get(), 2, "Take a pizza", LocalDate.of(2020, 1, 3));

        webTestClient.post().uri("/todo")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(todo), Todo.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Todo.class)
                .consumeWith(todoResponse -> {
                    assertThat(todoResponse.getResponseBody(),
                            hasProperty("id", is(todo.getId())));
                    assertThat(template.findById(todo.getId(), Todo.class).block(),
                            hasProperty("id", is(todo.getId())));
                });
    }

    @Test
    public void testDeleteTodo_shouldDeleteRecord_whenRequestWithExistingIdSent() {
        webTestClient.delete().uri("/todo/" + todo1.getId().toHexString())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> assertThat(template.findAll(Todo.class).toIterable(),
                        contains(hasProperty("id", is(todo2.getId())))));
    }
}