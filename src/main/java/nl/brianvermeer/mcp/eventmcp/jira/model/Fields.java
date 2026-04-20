package nl.brianvermeer.mcp.eventmcp.jira.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Fields(
        Project project,
        Description description,
        Person assignee,
        IssueType issuetype,
        Status status,
        String summary, //name
        @JsonProperty("customfield_10831") String startDate, //startdate
        String duedate, //enddate
        @JsonProperty("customfield_12301") String cfpLink, //CFPLink
        @JsonProperty("customfield_12302") String cfpCloses, //CFPCloses
        @JsonProperty("customfield_12333") String location, //location
        @JsonProperty("customfield_12620") CustomFieldValue region, //region
        @JsonProperty("customfield_17736") CustomFieldValue subregion, //subregion
        @JsonProperty("customfield_12086") String url, //URL
        @JsonProperty("customfield_13373") CustomFieldValue eventType,
        @JsonProperty("customfield_12089") CustomFieldValue eventFormat,
        @JsonProperty("customfield_18821") CustomFieldValue tier,

        @JsonProperty("customfield_12977") CustomFieldValue audience,
        @JsonProperty("customfield_12978") String attendees,
        @JsonProperty("customfield_12979") List<String> technologies,
        @JsonProperty("customfield_12980") String importSource,
        @JsonProperty("customfield_18680") Integer sessionAttendance,
        JiraIssue parent
) {
}