package nl.brianvermeer.mcp.eventmcp.jira.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventIssue(
        String key,
        String issueType,
        String title,
        String status,
        String assignee,
        String startDate,
        String endDate,
        String location,
        String region,
        String subregion,
        String url,
        String eventType,
        String eventFormat,
        String tier,
        String audience,
        String attendees,
        Integer sessionAttendance,
        List<String> technologies,
        String cfpLink,
        String cfpCloses,
        String parentKey
) {
    public static EventIssue from(JiraIssue issue) {
        var f = issue.fields();
        if (f == null) {
            return new EventIssue(issue.key(), null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null, null, null, null);
        }
        return new EventIssue(
                issue.key(),
                f.issuetype() != null ? f.issuetype().name() : null,
                f.summary(),
                f.status() != null ? f.status().name() : null,
                f.assignee() != null ? f.assignee().displayName() : null,
                f.startDate(),
                f.duedate(),
                f.location(),
                f.region() != null ? f.region().value() : null,
                f.subregion() != null ? f.subregion().value() : null,
                f.url(),
                f.eventType() != null ? f.eventType().value() : null,
                f.eventFormat() != null ? f.eventFormat().value() : null,
                f.tier() != null ? f.tier().value() : null,
                f.audience() != null ? f.audience().value() : null,
                f.attendees(),
                f.sessionAttendance(),
                f.technologies(),
                f.cfpLink(),
                f.cfpCloses(),
                f.parent() != null ? f.parent().key() : null
        );
    }
}
