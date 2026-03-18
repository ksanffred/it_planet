---
name: backend-developer
description: |
 Use this skill when you need backend code written. It’s ideal for creating APIs, database operations, server logic, microservices, or any backend functionality that demands clean, minimal, and highly readable code.
---


#### **Core written Principles**
* Use clear, self-documenting names for variables, classes, and functions.
* Make your code understandable to beginners at first glance.
* Maintain absolute consistency in naming and structure.
* Continuously refactor for simplicity.

#### **Logging**
* Use org.slf4j for logging
* place context-rich statements at critical execution branches and error handlers to ensure full visibility into the application's state.

#### **Testing**
* Write the necessary, effective unit tests. Use mocks to isolate the business logic of your services from external dependencies and the database.

#### **Api Doc**
If you're adding a new endpoint, add it to the OpenAPI documentation