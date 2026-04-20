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

## Authorization (Permissions)

- **Overview**: The project uses role-based access control (RBAC). JWTs issued by `mall-auth` propagate user identity, roles, and permission claims across the gateway and downstream services to enable unified authentication and fine-grained authorization.
- **Implementation highlights**:
	- **Authentication & tokens**: `mall-auth` handles user authentication and issues JWTs that include claims such as `userId`, `roles`, and `permissions`.
	- **Gateway validation**: The global filter in `mall-gateway` (for example, `GatewayAuthFilter`) validates the JWT; when valid, it injects the user context (`userId/roles/permissions`) into request headers and forwards the request to downstream services.
	- **Server-side authorization**: Each microservice reads the injected headers or parses the token in `SecurityConfig` or controller layers and enforces authorization via Spring Security annotations (e.g., `@PreAuthorize`) or custom interceptors to reject unauthorized requests.
	- **Data storage**: User, role, and permission relationships are stored in the authentication module database (see the `AuthUser` entity and its Mapper).
- **Request flow**:
	1. A user authenticates with `mall-auth` and receives a JWT containing permission claims.
	2. The client sends requests to `mall-gateway` with the token; the gateway validates the token and forwards the request with the user context.
	3. Downstream services read the context or re-validate the token and decide access based on permissions; insufficient permissions result in a unified `403` response.
- **Notes & extensions**:
	- Use coarse-grained route/interface checks at the gateway and fine-grained permission checks inside business services.
	- Consider token freshness for permission changes; use short-lived tokens, refresh tokens, or a permission blacklist to force revocation when needed.
	- In high-concurrency or distributed environments, centralize permission management and use caching or robust synchronization to ensure consistency and performance.

## Token Handling and Service Access Restrictions

- **Gateway forwards tokens only**: The gateway validates incoming JWTs and forwards the token (or user context) to downstream services for further processing. Downstream services are responsible for parsing and validating tokens themselves rather than trusting the gateway implicitly.
- **Key management best practices**:
	- **Environment variables / secrets**: Keep JWT signing keys or verification secrets in environment variables or the platform's secret store (do not hard-code them in source code).
	- **Rotation & KMS**: Rotate keys regularly and consider using a Key Management Service (KMS) to store and rotate keys securely to avoid single-point failures or loss.
	- **Prevent forged tokens**: Proper key management and short-lived tokens reduce the risk of malicious actors forging tokens to bypass the gateway.
- **Restrict direct access to business services**:
	- **Bind to localhost**: In deployment, services can listen only on localhost (or internal network interfaces) so they are not accessible directly from the public network.
	- **IP whitelist / filter**: Add an IP whitelist filter at the service level to accept requests only from the gateway's IP(s), or implement a lightweight IP-filtering middleware inside services.
	- **Network-level controls**: Configure environment firewalls, security groups, or network policies in production to allow traffic to business services only from the gateway.

These measures together (secure key storage, token rotation, per-service token validation, and network restrictions) help ensure tokens cannot be trivially forged or used to bypass the gateway. Future work: enforce that business services accept traffic only from the gateway by combining binding, IP whitelisting, and production firewall rules.

