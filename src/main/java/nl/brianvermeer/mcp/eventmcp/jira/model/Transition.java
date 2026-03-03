package nl.brianvermeer.mcp.eventmcp.jira.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Transition(
        String id,
        String name,
        Status to
) {}
