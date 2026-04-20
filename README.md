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

**Redis Cache Strategies (Avalanche, Breakdown, Penetration)**

- **Avalanche (Cache Stampede / Snowballing)**: When many cached keys expire at the same time, requests fall back to the database and can overwhelm it. Mitigations:
	- **Randomized TTL**: add a jitter to expiration times so keys don't all expire together.
	- **Cache pre-warming**: proactively load hot keys into cache after deployments or maintenance.
	- **Circuit breakers / rate limiting**: protect the DB during spikes by rejecting or throttling requests at the gateway.

- **Breakdown (Hotspot Key Miss / Cache Penetration to DB)**: A single very hot key expiring causes many concurrent DB hits. Mitigations:
	- **Distributed lock / mutex**: only one request rebuilds the cache while others wait or read stale data (use Redisson locks or similar).
	- **Double-check / cache-aside with single writer**: check cache again after obtaining lock before querying DB.
	- **Logical expiration**: serve slightly stale cached value while a background thread refreshes the cache.

- **Penetration (Requests for Non-existent Keys)**: Repeated queries for invalid IDs bypass cache and hit DB. Mitigations:
	- **Bloom filter**: filter invalid IDs at the gateway or service layer before hitting DB (example: `RBloomFilter` via Redisson). See [mall-product/src/main/java/com/mall/product/component/ProductBloomFilter.java](mall-product/src/main/java/com/mall/product/component/ProductBloomFilter.java) for an example implementation.
	- **Cache empty marker**: store a short-lived placeholder (e.g., empty value or null marker) for non-existent keys to prevent repeated DB lookups.
	- **Input validation**: validate request parameters early and reject obviously invalid IDs.

These combined strategies (TTL jitter, locks/deduplication, Bloom filters, and empty markers) are commonly used together in production to address avalanche, breakdown, and penetration problems when using Redis as a caching layer.

## 权限功能 (Authorization)

- **概述**: 项目采用基于角色/权限的访问控制（RBAC），通过 `mall-auth` 签发的 JWT 在网关和各服务间传播用户身份、角色与权限信息，用于统一鉴权与细粒度授权。
- **实现要点**:
	- **认证与令牌**: `mall-auth` 负责用户认证并签发 JWT，Token 中包含 `userId`、`roles` 与 `permissions` 等声明。
	- **网关校验**: `mall-gateway` 的全局过滤器（例如 `GatewayAuthFilter`）会校验 JWT，有效后将用户上下文（userId/roles/permissions）注入请求头并转发给下游服务。
	- **服务端授权**: 各微服务在 `SecurityConfig` 或控制器方法层面读取请求头或解析 Token，使用 Spring Security 注解（如 `@PreAuthorize`）或自定义拦截器来判断权限并拒绝无权请求。
	- **数据存储**: 用户、角色与权限关系保存在认证模块的数据库（参见 `AuthUser` 实体与对应 Mapper）。
- **简单请求流程**:
	1. 用户在 `mall-auth` 登录，获取包含权限声明的 JWT。
	2. 客户端带 Token 请求 `mall-gateway`，网关校验后注入用户上下文并转发。
	3. 下游服务读取上下文或再次校验 Token，基于权限决定是否允许访问；权限不足返回统一 403 响应。
- **扩展与注意**:
	- 建议在网关做路由/接口级的粗粒度控制，在业务服务做更细粒度的权限校验。
	- 权限变更需考虑 Token 的实时性，可采用短期 Token、刷新机制或权限黑名单以强制失效。
	- 对于高并发和分布式环境，建议将权限中心化并使用缓存或健壮的同步策略以保证一致性与性能。

