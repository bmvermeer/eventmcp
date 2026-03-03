package nl.brianvermeer.mcp.eventmcp.jira.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Description(
        String type,
        int version,
        List<Content> content
) {
}
