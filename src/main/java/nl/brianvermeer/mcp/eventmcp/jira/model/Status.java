package nl.brianvermeer.mcp.eventmcp.jira.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Status(
        String self,
        String description,
        String iconUrl,
        String name,
        String id
) {
}
