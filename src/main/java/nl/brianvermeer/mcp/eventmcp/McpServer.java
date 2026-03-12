package nl.brianvermeer.mcp.eventmcp;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import nl.brianvermeer.mcp.eventmcp.jira.JiraClient;
import nl.brianvermeer.mcp.eventmcp.jira.model.Content;
import nl.brianvermeer.mcp.eventmcp.jira.model.CustomFieldValue;
import nl.brianvermeer.mcp.eventmcp.jira.model.Description;
import nl.brianvermeer.mcp.eventmcp.jira.model.Fields;
import nl.brianvermeer.mcp.eventmcp.jira.model.IssueType;
import nl.brianvermeer.mcp.eventmcp.jira.model.JiraIssue;
import nl.brianvermeer.mcp.eventmcp.jira.model.Person;
import nl.brianvermeer.mcp.eventmcp.jira.model.Project;
import nl.brianvermeer.mcp.eventmcp.jira.model.Transition;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


import static nl.brianvermeer.mcp.eventmcp.jira.JiraDetails.PROJECT_KEY;

public class McpServer {

    JiraClient jiraClient;

    public McpServer(JiraClient jiraClient) {
        this.jiraClient = jiraClient;
    }

    @Tool(description = "Create a new Event on the Snyk Event board")
    String createEvent(@ToolArg(description = "Title") String title,
                       @ToolArg(description = "Assigned user id (optional, leave blank if not provided)") String assignee,
                       @ToolArg(description = "Start date in format yyyy-MM-dd") String startdate,
                       @ToolArg(description = "End date in format yyyy-MM-dd") String enddate,
                       @ToolArg(description = "Location, should be like Stockholm (Sweden)") String location,
                       @ToolArg(description = "Region, pick EMEA, AMER or APJ") String region,
                       @ToolArg(description = "Event URL or website (very recommended but optional, leave blank if not provided)") String url,
                       @ToolArg(description = "Event type, pick Conference, Meetup, Customer, Webinar or AI Sec Eng Community") String eventType,
                       @ToolArg(description = "Link to the CFP (optional, leave blank if not provided)") String cfpLink,
                       @ToolArg(description = "CFP closes date (optional, leave blank if not provided)") String cfpCloses,
                       @ToolArg(description = "Event format, pick Hybrid, In-person or Virtual") String eventFormat) throws IOException, InterruptedException {

        if (!isValidDateFormat(startdate)) {
            return "Error: Start date must be in format yyyy-MM-dd (e.g., 2026-03-15)";
        }
        if (!isValidDateFormat(enddate)) {
            return "Error: End date must be in format yyyy-MM-dd (e.g., 2026-03-17)";
        }
        var dateError = validateOptionalDate(cfpCloses, "CFP closes");
        if (dateError != null) return dateError;

        var fields = buildFields("Event", title, assignee, startdate, enddate,
                location, region, url, eventType, cfpLink, cfpCloses, eventFormat, null);

        return jiraClient.createTask(fields);
    }

    @Tool(description = "Update an existing Event on the Snyk Event board")
    String updateEvent(@ToolArg(description = "The issue key of the event to update (e.g. BTBFE-123)") String issueKey,
                       @ToolArg(description = "Title (optional, leave blank to keep current)") String title,
                       @ToolArg(description = "Assigned user id (optional, leave blank to keep current)") String assignee,
                       @ToolArg(description = "Start date in format yyyy-MM-dd (optional, leave blank to keep current)") String startdate,
                       @ToolArg(description = "End date in format yyyy-MM-dd (optional, leave blank to keep current)") String enddate,
                       @ToolArg(description = "Location, should be like Stockholm (Sweden) (optional, leave blank to keep current)") String location,
                       @ToolArg(description = "Region, pick EMEA, AMER or APJ (optional, leave blank to keep current)") String region,
                       @ToolArg(description = "Event URL or website (optional, leave blank to keep current)") String url,
                       @ToolArg(description = "Event type, pick Conference, Meetup, Customer or Webinar (optional, leave blank to keep current)") String eventType,
                       @ToolArg(description = "Link to the CFP (optional, leave blank to keep current)") String cfpLink,
                       @ToolArg(description = "CFP closes date (optional, leave blank to keep current)") String cfpCloses,
                       @ToolArg(description = "Event format, pick Hybrid, In-person or Virtual (optional, leave blank to keep current)") String eventFormat) throws IOException, InterruptedException {

        if (issueKey == null || issueKey.isBlank()) {
            return "Error: Issue key is required to update an event";
        }

        var startError = validateOptionalDate(startdate, "Start date");
        if (startError != null) return startError;
        var endError = validateOptionalDate(enddate, "End date");
        if (endError != null) return endError;
        var cfpError = validateOptionalDate(cfpCloses, "CFP closes");
        if (cfpError != null) return cfpError;

        var fields = buildFields(null, title, assignee, startdate, enddate,
                location, region, url, eventType, cfpLink, cfpCloses, eventFormat, null);

        return jiraClient.updateTask(fields, issueKey);
    }

    @Tool(description = "Search for Events on the Snyk Event Board. Filter by date range, text query, status and/or region. Date range prevents large result sets so actively ask for that.")
    List<JiraIssue> searchEvents(@ToolArg(description = "Start date in format yyyy-MM-dd (optional, leave blank to not filter on date)") String startdate,
                              @ToolArg(description = "End date in format yyyy-MM-dd (optional, leave blank to not filter on date)") String enddate,
                              @ToolArg(description = "Optional text search query to match against event fields. Leave blank to skip text search.") String query,
                              @ToolArg(description = "Optional filter on status: To review, Considering, In progress, Not doing, Done or Duplicate. Leave blank for all except Not doing.") String status,
                              @ToolArg(description = "Optional filter on region: EMEA, AMER or APJ. Leave blank for all regions.") String region) throws IOException, InterruptedException {
        startdate = normalizeBlank(startdate);
        enddate = normalizeBlank(enddate);
        query = normalizeBlank(query);
        status = normalizeBlank(status);
        region = normalizeBlank(region);

        if (startdate != null && !isValidDateFormat(startdate)) {
            return List.of();
        }
        if (enddate != null && !isValidDateFormat(enddate)) {
            return List.of();
        }

        var jql = new StringBuilder("issuetype = Event");

        if (startdate != null && enddate != null) {
            jql.append(" AND due >= ").append(startdate).append(" AND due <= ").append(enddate);
        }

        if (query != null) {
            jql.append(" AND text ~ \"").append(query).append("\"");
        }

        if (status != null) {
            jql.append(" AND status = \"").append(status).append("\"");
        } else {
            jql.append(" AND status != \"NOT DOING\"");
        }

        if (region != null) {
            jql.append(" AND cf[12620] = \"").append(region).append("\"");
        }

        jql.append(" ORDER BY duedate ASC");

        return jiraClient.getJiraBoardContent(jql.toString());
    }

    @Tool(description = "Get a specific issue (event or subtask) from the Snyk Event Board by its BTBFE key")
    JiraIssue getIssue(@ToolArg(description = "The BTBFE issue key (e.g. BTBFE-123)") String issueKey) throws IOException, InterruptedException {
        if (!isValidIssueKey(issueKey)) {
            throw new IllegalArgumentException("Error: Issue key must be in format BTBFE-<number> (e.g. BTBFE-123)");
        }
        return jiraClient.getIssue(issueKey);
    }

    @Tool(description = "Search for CFPs / talks on the Snyk Event Board. Filter by date range, text query, status, assignee and/or parent event key.")
    List<JiraIssue> searchCfps(@ToolArg(description = "Start date in format yyyy-MM-dd (optional, leave blank to not filter on date)") String startdate,
                               @ToolArg(description = "End date in format yyyy-MM-dd (optional, leave blank to not filter on date)") String enddate,
                               @ToolArg(description = "Optional text search query to match against CFP fields. Leave blank to skip text search.") String query,
                               @ToolArg(description = "Optional filter on status: CFP Submitted, Accepted, Not Doing or Done. Leave blank for all.") String status,
                               @ToolArg(description = "Optional filter on assignee user id. Leave blank for all users.") String assignee,
                               @ToolArg(description = "Optional BTBFE event key to filter CFPs for a specific event (e.g. BTBFE-2362). Leave blank for all events.") String eventKey) throws IOException, InterruptedException {
        startdate = normalizeBlank(startdate);
        enddate = normalizeBlank(enddate);
        query = normalizeBlank(query);
        status = normalizeBlank(status);
        assignee = normalizeBlank(assignee);
        eventKey = normalizeBlank(eventKey);

        if (startdate != null && !isValidDateFormat(startdate)) {
            return List.of();
        }
        if (enddate != null && !isValidDateFormat(enddate)) {
            return List.of();
        }

        if (eventKey != null && !isValidIssueKey(eventKey)) {
            throw new IllegalArgumentException("Error: Event key must be in format BTBFE-<number> (e.g. BTBFE-2362)");
        }

        var jql = new StringBuilder("issuetype = CFP");

        if (startdate != null && enddate != null) {
            jql.append(" AND due >= ").append(startdate).append(" AND due <= ").append(enddate);
        }

        if (eventKey != null) {
            jql.append(" AND parent = \"").append(eventKey).append("\"");
        }

        if (query != null) {
            jql.append(" AND text ~ \"").append(query).append("\"");
        }

        if (status != null) {
            jql.append(" AND status = \"").append(status).append("\"");
        }

        if (assignee != null) {
            jql.append(" AND assignee = \"").append(assignee).append("\"");
        }

        jql.append(" ORDER BY duedate ASC");

        return jiraClient.getJiraBoardContent(jql.toString());
    }


    @Tool(description = "Create a new CFP / talk submission for an event on the Snyk Event Board. CFP will be created with status 'To review'. Ask if the CFP is submitted already and update the status if needed.")
    String createCfp(@ToolArg(description = "Title of the talk") String title,
                     @ToolArg(description = "The BTBFE key of the parent event (e.g. BTBFE-2362)") String eventKey,
                     @ToolArg(description = "Assigned user id (optional, leave blank if not provided)") String assignee) throws IOException, InterruptedException {
        if (!isValidIssueKey(eventKey)) {
            return "Error: Event key must be in format BTBFE-<number> (e.g. BTBFE-2362)";
        }
        if (title == null || title.isBlank()) {
            return "Error: Title is required";
        }

        var fields = buildFields("CFP", title, assignee,
                null, null, null, null, null, null, null, null, null, eventKey);

        return jiraClient.createTask(fields);
    }

    @Tool(description = "Update an existing CFP / talk on the Snyk Event Board")
    String updateCfp(@ToolArg(description = "The BTBFE issue key of the CFP to update (e.g. BTBFE-2400)") String issueKey,
                     @ToolArg(description = "Title of the talk (optional, leave blank to keep current)") String title,
                     @ToolArg(description = "Assigned user id (optional, leave blank to keep current)") String assignee) throws IOException, InterruptedException {
        if (!isValidIssueKey(issueKey)) {
            return "Error: Issue key must be in format BTBFE-<number> (e.g. BTBFE-2400)";
        }

        var fields = buildFields(null, title, assignee,
                null, null, null, null, null, null, null, null, null, null);

        return jiraClient.updateTask(fields, issueKey);
    }

    @Tool(description = "Returns instructions on how to create a report on the Snyk Event Board. Call this before createReport.")
    String getReportSkills() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/skills/create-report.md")) {
            if (is == null) return "No skills file found.";
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Tool(description = "Create a new Report for an event on the Snyk Event Board. The report is automatically assigned to the person creating it. Call getReportSkills first for guidance on how to fill in this report.")
    String createReport(@ToolArg(description = "Title of the report") String title,
                        @ToolArg(description = "The BTBFE key of the parent event (e.g. BTBFE-2362)") String eventKey,
                        @ToolArg(description = "Description / content of the report. Plain text only, no markdown or special formatting.") String description) throws IOException, InterruptedException {
        if (!isValidIssueKey(eventKey)) {
            return "Error: Event key must be in format BTBFE-<number> (e.g. BTBFE-2362)";
        }
        if (title == null || title.isBlank()) {
            return "Error: Title is required";
        }
        if (description == null || description.isBlank()) {
            return "Error: Description is required";
        }

        var currentUser = jiraClient.findCurrentUser();
        var fields = buildReportFields("Report", title, currentUser.accountId(), description, eventKey);
        return jiraClient.createTask(fields);
    }

    @Tool(description = "Update an existing Report on the Snyk Event Board")
    String updateReport(@ToolArg(description = "The BTBFE issue key of the report to update (e.g. BTBFE-2400)") String issueKey,
                        @ToolArg(description = "Title of the report (optional, leave blank to keep current)") String title,
                        @ToolArg(description = "Description / content of the report. Plain text only, no markdown or special formatting. (optional, leave blank to keep current)") String description) throws IOException, InterruptedException {
        if (!isValidIssueKey(issueKey)) {
            return "Error: Issue key must be in format BTBFE-<number> (e.g. BTBFE-2400)";
        }

        var fields = buildReportFields(null, title, null, description, null);
        return jiraClient.updateTask(fields, issueKey);
    }

    @Tool(description = "Find a user by name or email")
    Person findUser(@ToolArg(description = "The name or email of the user") String name) throws IOException, InterruptedException {
        return jiraClient.findUser(name);
    }

    @Tool(description = "Get the currently logged in Jira user")
    Person getCurrentUser() throws IOException, InterruptedException {
        return jiraClient.findCurrentUser();
    }

    @Tool(description = "Get the available status transitions for an issue on the Snyk Event board based on the current status and the BTBFE issue key")
    List<Transition> getTransitions(@ToolArg(description = "The BTBFE issue key of the CFP to update (e.g. BTBFE-2400)") String issueKey) throws IOException, InterruptedException {
        if (!isValidIssueKey(issueKey)) {
            throw new IllegalArgumentException("Error: Issue key must be in format BTBFE-<number> (e.g. BTBFE-123)");
        }
        return jiraClient.getTransitions(issueKey);
    }

    @Tool(description = "Update the status of an issue on the Snyk Event board, check available transitions with getTransitions first")
    String updateStatus(@ToolArg(description = "The BTBFE issue key of the CFP to update (e.g. BTBFE-2400)\"") String issueKey,
                        @ToolArg(description = "The transition id") int transitionId) throws IOException, InterruptedException {
        if (!isValidIssueKey(issueKey)) {
            return "Error: Issue key must be in format BTBFE-<number> (e.g. BTBFE-123)";
        }
        return jiraClient.transitionIssue(issueKey, transitionId);
    }



    private Fields buildReportFields(String issueTypeName, String title, String assigneeId, String description, String parentKey) {
        title = normalizeBlank(title);
        assigneeId = normalizeBlank(assigneeId);
        description = normalizeBlank(description);
        parentKey = normalizeBlank(parentKey);

        var user = assigneeId != null ? new Person(null, assigneeId, null, null, true, null, null) : null;
        var parent = parentKey != null ? new JiraIssue(null, null, null, parentKey, null) : null;
        var desc = description != null ? buildDescription(description) : null;

        return new Fields(new Project(PROJECT_KEY),
                desc, user,
                issueTypeName != null ? new IssueType(issueTypeName) : null,
                null,
                title, null, null, null, null, null, null, null, null, null, null, null, null, null, null, parent);
    }

    private Description buildDescription(String text) {
        return new Description("doc", 1,
                List.of(new Content("paragraph",
                        List.of(Map.of("type", "text", "text", text)))));
    }

    private Fields buildFields(String issueTypeName, String title, String assignee,
                                String startdate, String enddate, String location, String region,
                                String url, String eventType, String cfpLink, String cfpCloses,
                                String eventFormat, String parentKey) {
        title = normalizeBlank(title);
        assignee = normalizeBlank(assignee);
        startdate = normalizeBlank(startdate);
        enddate = normalizeBlank(enddate);
        location = normalizeBlank(location);
        region = normalizeBlank(region);
        url = normalizeBlank(url);
        eventType = normalizeBlank(eventType);
        cfpLink = normalizeBlank(cfpLink);
        cfpCloses = normalizeBlank(cfpCloses);
        eventFormat = normalizeBlank(eventFormat);
        parentKey = normalizeBlank(parentKey);

        var user = assignee != null ? new Person(null, assignee, null, null, true, null, null) : null;
        var parent = parentKey != null ? new JiraIssue(null, null, null, parentKey, null) : null;

        return new Fields(new Project(PROJECT_KEY),
                null,
                user,
                issueTypeName != null ? new IssueType(issueTypeName) : null,
                null,
                title, startdate, enddate, cfpLink, cfpCloses, location,
                region != null ? CustomFieldValue.getRegion(region) : null, null, url,
                eventType != null ? CustomFieldValue.getEventType(eventType) : null,
                eventFormat != null ? CustomFieldValue.getEventFormat(eventFormat) : null,
                null, null, null, "Event".equals(issueTypeName) ? "MCP" : null, parent);
    }

    private String validateOptionalDate(String date, String fieldName) {
        if (date != null && !date.isBlank() && !isValidDateFormat(date)) {
            return "Error: " + fieldName + " date must be in format yyyy-MM-dd (e.g., 2026-03-15)";
        }
        return null;
    }

    private String normalizeBlank(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private boolean isValidIssueKey(String key) {
        return key != null && !key.isBlank() && key.matches("BTBFE-\\d+");
    }

    private boolean isValidDateFormat(String date) {
        if (date == null || date.isBlank()) {
            return false;
        }
        try {
            java.time.LocalDate.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
