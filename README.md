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


### Using [JBang](https://www.jbang.dev/) (Recommended)

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

