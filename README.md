# snow-resorts-shared

Publishable shared libraries consumed by every Snow Resorts microservice. Published to
**GitHub Packages** under the `yurileao` account.

## Modules

| Module | Artifact | Purpose |
|--------|----------|---------|
| `security-lib` | `com.snowresorts:security-lib` | Auto-configured JWT resource-server chain, OWASP security headers, correlation-id filter, RFC 7807 `GlobalExceptionHandler` + base exceptions |
| `contracts` | `com.snowresorts:contracts` | Domain-free async event records (`RunCompletedEvent`, `ReviewCreatedEvent`) + OpenAPI/JSON-schema stubs |

## Versioning & publishing

The artifact version follows the Git tag. To cut a release:

```bash
git tag v1.0.1
git push origin v1.0.1
```

The [`publish.yml`](.github/workflows/publish.yml) workflow builds, tests and runs
`mvn deploy` to GitHub Packages with the tag version.

## Consuming from a service

Add the repository and dependency to the service `pom.xml`:

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/yurileao/snow-resorts-shared</url>
    <snapshots><enabled>false</enabled></snapshots>
  </repository>
</repositories>

<dependency>
  <groupId>com.snowresorts</groupId>
  <artifactId>security-lib</artifactId>
  <version>1.0.0</version>
</dependency>
```

Reading packages requires a `github` server credential in `~/.m2/settings.xml` with a
Personal Access Token that has the `read:packages` scope (see
[`settings.xml.example`](settings.xml.example)). Publishing requires `write:packages`.

## Local build

```bash
./mvnw clean install   # installs 1.0.0 into your local ~/.m2 for offline service builds
```
