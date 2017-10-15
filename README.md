# Introduction
This is a simple service for managing "Todo" tasks. The goal of this project is to demonstrate the Spring reactive stack.

# Stack
  - Spring Boot 2
  - Spring WebFlux
  - Spring Data MongoDB Reactive
  
# How to run
  - `docker-compose up` to run the whole project
  - `mvn test` to run tests using embedded MongoDB instance
  - `mvn spring-boot:run` to run the app using local MongoDB instance

# How to test endpoints
  - get all as JSON stream  
    `curl http://localhost:8080/todo -i -H "Accept: application/stream+json"`
  - get all as JSON  
    `curl http://localhost:8080/todo -i -H "Accept: application/json"`
  - get by ID (please replace {id} with real identifier)  
    `curl http://localhost:8080/todo/{id} -i -H "Accept: application/json"`
  - create  
    `curl http://localhost:8080/todo -i -H "Content-Type: application/json" -H "Accept: application/json" -X POST -d '{"description":"Take a nap"}'`
  - delete (please replace {id} with real identifier)  
    `curl http://localhost:8080/todo/{id} -i -X DELETE`