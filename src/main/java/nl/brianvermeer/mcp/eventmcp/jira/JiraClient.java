package nl.brianvermeer.mcp.eventmcp.jira;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.brianvermeer.mcp.eventmcp.jira.issue.IssuePayload;
import nl.brianvermeer.mcp.eventmcp.jira.model.Fields;
import nl.brianvermeer.mcp.eventmcp.jira.model.JiraIssue;
import nl.brianvermeer.mcp.eventmcp.jira.model.Person;
import nl.brianvermeer.mcp.eventmcp.jira.model.Transition;
import nl.brianvermeer.mcp.eventmcp.jira.model.TransitionsResponse;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.brianvermeer.mcp.eventmcp.jira.JiraDetails.JIRA_BASE_URL;
import static nl.brianvermeer.mcp.eventmcp.jira.JiraDetails.PROJECT_KEY;

@ApplicationScoped
public class JiraClient {

    private static final Logger Log = Logger.getLogger(JiraClient.class.getName());
    public static final String AUTHORIZATION = "Authorization";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APP_JSON = "application/json";

    private static final String JIRA_ISSUES_URL = JIRA_BASE_URL + "issue";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String JQL_QUERY = "project = " + PROJECT_KEY;
    private static final String JIRA_SEARCH_JQL_URL = JIRA_BASE_URL + "search/jql";

    @ConfigProperty(name = "JIRA_USERNAME")
    public String username;

    @ConfigProperty(name = "JIRA_TOKEN")
    public String apiToken;

    public String createTask(Fields fields) throws IOException, InterruptedException {
        var payload = new IssuePayload(fields);
        var jsonPayload = objectMapper.writeValueAsString(payload);

        var request = HttpRequest.newBuilder()
                .uri(URI.create(JIRA_ISSUES_URL))
                .header(AUTHORIZATION, authHeader())
                .header(CONTENT_TYPE, APP_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            return "Issue created successfully for: "+fields.issuetype()+" " + fields.summary() + " " + response.body();
        } else {
            return "Failed to create issue for: " + fields.summary() + " " + response.statusCode() + " - " + response.body();
        }
    }

    public String updateTask(Fields fields, String issueKey) throws IOException, InterruptedException {
        var payload = new IssuePayload(fields);
        var jsonPayload = objectMapper.writeValueAsString(payload);

        var request = HttpRequest.newBuilder()
                .uri(URI.create(JIRA_ISSUES_URL + "/" + issueKey))
                .header(AUTHORIZATION, authHeader())
                .header(CONTENT_TYPE, APP_JSON)
                .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 204) {
            return "Issue updated successfully: " + response.body();
        } else {
            return "Failed to update issue: " + issueKey + " " + response.statusCode() + " - " + response.body();

        }

    }

    public Person findCurrentUser() throws IOException, InterruptedException {
        return findUser(username);
    }

    public Person findUser(String name) throws IOException, InterruptedException {
        String encoded = URLEncoder.encode(name, StandardCharsets.UTF_8);
        String uri = JIRA_BASE_URL + "user/search?query=" + encoded;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header(AUTHORIZATION, authHeader())
                .header(CONTENT_TYPE, APP_JSON)
                .GET()
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("User search failed: HTTP " + response.statusCode() + " - " + response.body());
        }

        Person[] users = objectMapper.readValue(response.body(), Person[].class);
        if (users.length == 0) {
            throw new IOException("No user found for query: " + name);
        }
        return users[0];
    }

    public List<JiraIssue> getJiraBoardContent(String jql) throws IOException, InterruptedException {
        List<JiraIssue> allIssues = new ArrayList<>();
        String jqlQuery = JQL_QUERY + " AND " + jql;
        String nextPageToken = null;

        while (true) {
            // Create JSON request body for enhanced search API using ObjectMapper
            var response = getHttpResponse(jqlQuery, nextPageToken);

            var responseBody = response.body();
            var jsonNode = objectMapper.readTree(responseBody);
            var issues = jsonNode.get("issues");

            if (issues == null || issues.isEmpty()) {
                break;
            }

            for (JsonNode issue : issues) {
                var newIssue = objectMapper.treeToValue(issue, JiraIssue.class);
                allIssues.add(newIssue);
            }

            // Check if this is the last page
            var isLast = jsonNode.get("isLast");
            if (isLast != null && isLast.asBoolean()) {
                break;
            }

            // Get nextPageToken for next iteration
            var nextToken = jsonNode.get("nextPageToken");
            if (nextToken == null || nextToken.isNull()) {
                break;
            }
            nextPageToken = nextToken.asText();
        }
        return allIssues;
    }

    private String fieldAsJson(Object fieldValue) {
        try {
            return objectMapper.writeValueAsString(fieldValue);
        } catch (JsonProcessingException e) {
            Log.error("Failed to convert field value to JSON: ", e);
            return "\"\"";
        }
    }

    private HttpResponse<String> getHttpResponse(String jqlQuery, String nextPageToken) throws IOException, InterruptedException {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("jql", jqlQuery);
        requestData.put("fields", new String[]{"*all"});
        requestData.put("fieldsByKeys", false);

        // Add nextPageToken if we have one from previous request
        if (nextPageToken != null) {
            requestData.put("nextPageToken", nextPageToken);
        }

        String requestBody = objectMapper.writeValueAsString(requestData);

        var request = HttpRequest.newBuilder()
                .uri(URI.create(JIRA_SEARCH_JQL_URL))
                .header(AUTHORIZATION, authHeader())
                .header(CONTENT_TYPE, APP_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            Log.error("Failed to fetch data: " + response.statusCode() + " " + response.body());
            throw new IOException("Failed to fetch data: " + response.statusCode() + " " + response.body());
        }
        return response;
    }


    public JiraIssue getIssue(String btbfeId) throws IOException, InterruptedException {
        return getJiraBoardContent("key = " + btbfeId).get(0);
    }

    public List<Transition> getTransitions(String issueKey) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(JIRA_ISSUES_URL + "/" + issueKey + "/transitions"))
                .header(AUTHORIZATION, authHeader())
                .header(CONTENT_TYPE, APP_JSON)
                .GET()
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to get transitions for " + issueKey + ": " + response.statusCode() + " - " + response.body());
        }

        return objectMapper.readValue(response.body(), TransitionsResponse.class).transitions();
    }

    public String transitionIssue(String issueKey, int transitionId) throws IOException, InterruptedException {
        Map<String, Object> payload = Map.of("transition", Map.of("id", transitionId));
        var jsonPayload = objectMapper.writeValueAsString(payload);

        var request = HttpRequest.newBuilder()
                .uri(URI.create(JIRA_ISSUES_URL + "/" + issueKey + "/transitions"))
                .header(AUTHORIZATION, authHeader())
                .header(CONTENT_TYPE, APP_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 204) {
            return "Issue " + issueKey + " transitioned successfully";
        } else {
            return "Failed to transition " + issueKey + ": " + response.statusCode() + " - " + response.body();
        }
    }


    private String authHeader() {
        var auth = username + ":" + apiToken;
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }

}
