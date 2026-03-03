package nl.brianvermeer.mcp.eventmcp.jira.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Person(
        String self,
        String accountId,
        String emailAddress,
        String displayName,
        boolean active,
        String timeZone,
        String accountType
) {
}
