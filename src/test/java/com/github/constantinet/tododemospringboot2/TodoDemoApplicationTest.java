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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
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
        todo1 = new Todo(ObjectId.get(), "Order pizza");
        todo2 = new Todo(ObjectId.get(), "Eat pizza");

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
                .consumeNextWith(todo -> assertThat(todo, hasProperty("description", is(todo1.getDescription()))))
                .consumeNextWith(todo -> assertThat(todo, hasProperty("description", is(todo2.getDescription()))))
                .thenCancel()
                .verify();
    }

    @Test
    public void testGetTodo_shouldReturnCorrectJson_whenRequestWithExistingIDSent() {
        webTestClient.get().uri("/todo/" + todo2.getId().toHexString())
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Todo.class)
                .consumeWith(todo -> assertThat(todo.getResponseBody(), hasProperty("description", is(todo2.getDescription()))));
    }

    @Test
    public void testCreateTodo_shouldReturnCorrectJsonAndCreateRecord_whenRequestWithCorrectBodySent() {
        final Todo todo = new Todo(ObjectId.get(), "Take a nap");

        webTestClient.post().uri("/todo")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(todo), Todo.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Todo.class)
                .consumeWith(todoResponse -> {
                    assertThat(todoResponse.getResponseBody(),
                            hasProperty("description", is(todo.getDescription())));
                    assertThat(template.findById(todo.getId(), Todo.class).block(),
                            hasProperty("description", is(todo.getDescription())));
                });
    }
}