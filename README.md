# Snyk Event MCP server

----

A Model Context Protocol (MCP) server for managing events on the Snyk Event Board in Jira, built with Quarkus and Java 21.
This MCP server provides tools to create and manage events on the Snyk Event Board through Jira integration.

## Features

- Manage events on the Snyk Event Board (create, update, search)
- Manage CFPs/talks (create, update, search)
- Update issue status using Jira transitions
- Look up Jira users and current user details
- Fetch a specific issue by BTBFE key

----
## Installation

To use this MCP server with your AI Agent (eg, Claude Desktop, Cursor) there are multiple options.
This MCP is build to run locally on the target machine using stdio. 

### Environment Variables

The following environment variables are required for the MCP server to function:

- `JIRA_USERNAME`: Your Jira email address (e.g., `brianvermeer@snyk.io`)
- `JIRA_TOKEN`: Your Jira API token (generate from your Jira account settings [here](https://id.atlassian.com/manage-profile/security/api-tokens))

### Using NPX

```json
{
  "mcpServers": {
    "eventmcp": {
      "command": "npx",
      "args": [
        "@jbangdev/jbang",
        "eventmcp@bmvermeer/eventmcp"
      ],
      "env": {
        "JIRA_USERNAME": "your-email@snyk.io",
        "JIRA_TOKEN": "your-jira-api-token-here"
      }
    }
  }
}
```

### Using [JBang](https://www.jbang.dev/)

Configure in your MCP client (e.g., Cursor, Claude Desktop):

```json
{
  "mcpServers": {
    "eventmcp": {
      "command": "jbang",
      "args": ["eventmcp@bmvermeer/eventmcp"],
      "env": {
        "JIRA_USERNAME": "your-email@snyk.io",
        "JIRA_TOKEN": "your-jira-api-token-here"
      }
    }
  }
}
```

### Using Docker

Run the MCP server in a container. The image is built straight from source, so you don't need
Java or Maven installed locally — only Docker.

Build the image:

```shell script
docker build -t eventmcp:latest .
```

Then configure your MCP client (e.g. Claude Desktop, Claude Code, Cursor):

```json
{
  "mcpServers": {
    "eventmcp": {
      "command": "docker",
      "args": [
        "run", "-i", "--rm",
        "-e", "JIRA_USERNAME",
        "-e", "JIRA_TOKEN",
        "eventmcp:latest"
      ],
      "env": {
        "JIRA_USERNAME": "your-email@snyk.io",
        "JIRA_TOKEN": "your-jira-api-token-here"
      }
    }
  }
}
```

Notes:
- `-i` is required: the MCP stdio transport speaks JSON-RPC over the container's stdin/stdout.
- Do **not** add `-t`; a TTY corrupts the protocol stream.
- `-e JIRA_USERNAME -e JIRA_TOKEN` (no `=value`) forwards the values from the `env` block above
  into the container, so your token stays out of the image and out of the command line.
- `--rm` cleans up the container when the client disconnects.
- Sometimes the command needs the full path to the `docker` executable, e.g. `/usr/local/bin/docker`.

You can verify the container works before wiring it into a client:

```shell script
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"probe","version":"1"}}}' \
  | docker run -i --rm -e JIRA_USERNAME=you@snyk.io -e JIRA_TOKEN=your-token eventmcp:latest
```

This should print a single JSON line containing `"serverInfo":{"name":"eventmcp",...}`. Startup
logging goes to stderr, so stdout stays a clean JSON-RPC stream.

### Download the latest release and run with Java 
- Use Java 21 or higher
- Download [latest release](https://github.com/bmvermeer/eventmcp/releases/latest) from github

```json
{
  "mcpServers": {
    "eventmcp": {
      "command": "java",
      "args": ["-jar", "/path/to/target/eventmcp-runner.jar"],
      "env": {
        "JIRA_USERNAME": "your-email@snyk.io",
        "JIRA_TOKEN": "your-jira-api-token-here"
      }
    }
  }
}
```
- NOTE: sometimes the commands need the full path to the `java` executable e.g. `/Users/brianvermeer/.sdkman/candidates/java/21.0.1-zulu/bin/java`

----
## Build MCP from source

### Prerequisites
- Java 21 or later
- Maven 3.9+ (to build, not to run)

## Build executable jar
Download the source and build an über-jar for production deployment:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

---
## Available Tools

The MCP server provides the following tools:

### Event Management
- **createEvent**: Create a new event on the Snyk Event Board with details like title, date, location, event type (Conference, Meetup, Customer, Webinar, AI Sec Eng Community), format (Hybrid, In-person, Virtual), and optional CFP information
- **updateEvent**: Update an existing event on the Snyk Event Board by its issue key
- **searchEvents**: Search for events on the Snyk Event Board with optional filters by date range, text query, status, region, tier (Tier 1, Tier 2, Tier 3), open CFP only, and technologies

### Issue Details
- **getIssue**: Get a specific issue (event or subtask) from the Snyk Event Board by its BTBFE key. Returns a clean, readable representation with all field names and values resolved (no raw Jira custom field IDs)

### CFP / Report Management
- **createCfp**: Create a new CFP/talk submission for an event on the Snyk Event Board
- **updateCfp**: Update an existing CFP/talk on the Snyk Event Board (title, assignee, session attendance)
- **searchCfps**: Search for CFPs/talks on the Snyk Event Board with optional filters by date range, text query, status, assignee, and parent event key
- **getReportSkills**: Returns instructions for creating or updating a report — call this before creating or updating a report
- **createReport**: Create a new report for an event on the Snyk Event Board
- **updateReport**: Update an existing report on the Snyk Event Board

### User Management
- **findUser**: Find a user by name or email
- **getCurrentUser**: Get the currently logged in Jira user

### Status Management
- **getTransitions**: Get available status transitions for an issue based on its current status and BTBFE key
- **updateStatus**: Update the status of an issue using a transition ID

## Architecture

This project is built with:

- **Quarkus** (Java framework for cloud-native applications)
- **Java 21** (latest LTS release)
- **MCP Server for Quarkus** (quarkiverse-mcp-server)
- **Jira REST API** for event management

---
## Troubleshooting

### Authentication Issues

If you receive authentication errors:
1. Verify your `JIRA_USERNAME` and `JIRA_TOKEN` are correct
2. Ensure the API token has the necessary permissions
3. Check that the Jira instance URL is accessible
4. Make sure environment variables are properly set before starting the server

For Claude Desktop, ensure they are specified in the `env` section of the MCP declaration.

## Resources

- [Quarkus Documentation](https://quarkus.io/)
- [Model Context Protocol](https://modelcontextprotocol.io/)
- [Jira REST API](https://developer.atlassian.com/cloud/jira/rest/)
- [MCP Server for Quarkus](https://docs.quarkiverse.io/quarkus-mcp-server/dev/index.html)

