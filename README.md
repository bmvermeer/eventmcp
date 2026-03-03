Snyk Event MCP server
==========

A Model Context Protocol (MCP) server for managing events on the Snyk Event Board in Jira, built with Quarkus and Java 21.
This MCP server provides tools to create and manage events on the Snyk Event Board through Jira integration.

## Features

- Manage events on the Snyk Event Board (create, update, search)
- Manage CFPs/talks (create, update, search)
- Update issue status using Jira transitions
- Look up Jira users and current user details
- Fetch a specific issue by BTBFE key

## Prerequisites

- Java 21 or later
- Maven 3.9+ (to build, not to run)
- Jira API token (from your Jira account)
- Claude Desktop or another MCP client that supports stdio transport

## Configuration

### Environment Variables

The following environment variables are required for the MCP server to function:

- `JIRA_USERNAME`: Your Jira email address (e.g., `brianvermeer@snyk.io`)
- `JIRA_API_TOKEN`: Your Jira API token (generate from your Jira account settings [here](https://id.atlassian.com/manage-profile/security/api-tokens)) 


## Download latest build
You can also download the latest build from the releases page.

The server will start on stdio transport by default.

## Build your own from source

Build an über-jar for production deployment:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```


## Integration with Claude Desktop

To use this MCP server with Claude Desktop, add it to your `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "eventmcp": {
      "command": "java",
      "args": ["-jar", "/path/to/target/*-runner.jar"],
      "env": {
        "JIRA_USERNAME": "your-email@example.com",
        "JIRA_API_TOKEN": "your-jira-api-token-here"
      }
    }
  }
}
```

**Important:** When declaring the MCP server in your client configuration, you must provide the Jira credentials in the `env` section. These environment variables are required for the server to authenticate with Jira:

- `JIRA_USERNAME`: Required. Your Jira account email address
- `JIRA_API_TOKEN`: Required. Your Jira API token for authentication

The server reads these from the environment variables specified in the MCP declaration, so they must be set before the server starts.

## Available Tools

The MCP server provides the following tools:

### Event Management
- **createEvent**: Create a new event on the Snyk Event Board with details like title, date, location, event type, format, and optional CFP information
- **updateEvent**: Update an existing event on the Snyk Event Board by its issue key
- **searchEvents**: Search for events on the Snyk Event Board with optional filters by date range, text query, status, and region

### Issue Details
- **getIssue**: Get a specific issue (event or subtask) from the Snyk Event Board by its BTBFE key

### CFP Management
- **createCfp**: Create a new CFP/talk submission for an event on the Snyk Event Board
- **updateCfp**: Update an existing CFP/talk on the Snyk Event Board
- **searchCfps**: Search for CFPs/talks on the Snyk Event Board with optional filters by date range, text query, status, assignee, and parent event key

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

## Building and Testing

### Build the project:

```shell script
./mvnw clean package
```

### Check code quality:

```shell script
./mvnw verify
```

## Troubleshooting

### Authentication Issues

If you receive authentication errors:
1. Verify your `JIRA_USERNAME` and `JIRA_API_TOKEN` are correct
2. Ensure the API token has the necessary permissions
3. Check that the Jira instance URL is accessible
4. Make sure environment variables are properly set before starting the server

For Claude Desktop, ensure they are specified in the `env` section of the MCP declaration.

## Resources

- [Quarkus Documentation](https://quarkus.io/)
- [Model Context Protocol](https://modelcontextprotocol.io/)
- [Jira REST API](https://developer.atlassian.com/cloud/jira/rest/)
- [MCP Server for Quarkus](https://docs.quarkiverse.io/quarkus-mcp-server/dev/index.html)

