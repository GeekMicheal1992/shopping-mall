# Simple Mall Microservices E-commerce Project

This project is a distributed e-commerce system based on Spring Cloud, designed for interview demonstrations and as proof of practical development skills. It covers core business modules such as user, product, order, authentication, and gateway, featuring high availability, scalability, and maintainability.

## Tech Stack

- Spring Boot 3.x
- Spring Cloud Gateway
- Spring Cloud OpenFeign
- Spring Security + JWT
- Redis (rate limiting, caching)
- MyBatis-Plus
- Docker (containerized deployment supported)
- Maven multi-module management

## Project Structure

- mall-gateway: API gateway, unified entry point, responsible for routing, authentication, rate limiting, etc.
- mall-auth: Authentication center, JWT login, token validation, user registration
- mall-user: User service, user info, shipping address, etc.
- mall-product: Product service, product management, categories, inventory
- mall-order: Order service, order creation, query, payment
- mall-common: Common module, utilities, global exceptions, unified response

## Highlights & Engineering Practices

- **Microservices Architecture**: Each business module is independently deployable, services communicate via Feign, easy to scale and maintain.
- **Unified Authentication & Authorization**: JWT-based SSO, global filter in gateway for user context propagation and API security.
- **Global Exception Handling**: Custom exception system, unified error codes and response format, improving front-end/back-end collaboration.
- **API Rate Limiting**: Redis + Gateway rate limiter plugin to prevent abuse and ensure system stability.
- **Whitelist Mechanism**: Route-level API whitelist configuration for flexible control of public endpoints.
- **Distributed Config & Logging**: Multi-environment support, log level configuration, easy troubleshooting.
- **Containerization**: Optional Docker deployment for fast delivery in local and cloud environments.
- **Readable Code**: Well-commented, clear structure, suitable for team collaboration and secondary development.

## Utility Libraries

- [Hutool](https://hutool.cn/): Java utility library used for simplifying common development tasks in this project.

