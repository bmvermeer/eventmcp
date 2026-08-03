####
# Dockerfile for running the Event MCP server as a stdio MCP server inside a container.
#
# The MCP stdio transport speaks JSON-RPC over the container's stdin/stdout, so the
# container must be started with `-i` (interactive) and *without* `-t` (no TTY), and the
# entrypoint must be the JVM itself -- any wrapper script that echoes to stdout would
# corrupt the protocol stream.
#
# Build (no local JDK/Maven needed, the first stage does the build):
#
#   docker build -t eventmcp:latest .
#
# Run (handshake over stdin/stdout):
#
#   docker run -i --rm -e JIRA_USERNAME -e JIRA_TOKEN eventmcp:latest
#
###

FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

# Resolve dependencies in their own layer so source-only changes don't re-download them.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -ntp quarkus:go-offline || true

COPY src/ src/
RUN ./mvnw -B -ntp package -DskipTests \
    && cp target/eventmcp-runner.jar /build/eventmcp.jar


FROM eclipse-temurin:21-jre

LABEL org.opencontainers.image.title="Snyk Event MCP server" \
      org.opencontainers.image.description="MCP (stdio) server for managing events on the Snyk Event Board in Jira" \
      org.opencontainers.image.source="https://github.com/bmvermeer/eventmcp"

RUN groupadd -r mcp && useradd -r -g mcp -u 1001 -m -d /home/mcp mcp

COPY --from=build --chown=mcp:mcp /build/eventmcp.jar /app/eventmcp.jar

USER mcp

# JIRA_USERNAME and JIRA_TOKEN are required at startup; pass them with `docker run -e`.
ENTRYPOINT ["java", "-jar", "/app/eventmcp.jar"]
