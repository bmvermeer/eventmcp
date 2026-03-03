package nl.brianvermeer.mcp.eventmcp.jira.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CustomFieldValue(String value, String id) {
    public static final CustomFieldValue EVENT_TYPE_CONFERENCE = new CustomFieldValue("Conference", "17006");
    public static final CustomFieldValue EVENT_TYPE_MEETUP = new CustomFieldValue("Meetup", "17007");
    public static final CustomFieldValue EVENT_TYPE_CUSTOMER = new CustomFieldValue("Customer", "17008");
    public static final CustomFieldValue EVENT_TYPE_WEBINAR = new CustomFieldValue("Webinar", "17009");

    public static final CustomFieldValue EVENT_FORMAT_VIRTUAL = new CustomFieldValue("Virtual", "13832");
    public static final CustomFieldValue EVENT_FORMAT_IN_PERSON = new CustomFieldValue("In-person", "13831");
    public static final CustomFieldValue EVENT_FORMAT_HYBRID = new CustomFieldValue("Hybrid", "13830");

    public static final CustomFieldValue REGION_EMEA = new CustomFieldValue("EMEA", "15786");
    public static final CustomFieldValue REGION_AMER = new CustomFieldValue("AMER", "15787");
    public static final CustomFieldValue REGION_APJ = new CustomFieldValue("APJ", "15788");
    public static final CustomFieldValue REGION_GLOBAL = new CustomFieldValue("GLOBAL", "15789");
    public static final CustomFieldValue REGION_UNKNOWN = new CustomFieldValue("UNKNOWN", "15790");

    public static final CustomFieldValue SUBREGION_NEMEA = new CustomFieldValue("NEMEA", "21487");
    public static final CustomFieldValue SUBREGION_SCE = new CustomFieldValue("SCE", "21488");
    public static final CustomFieldValue SUBREGION_ANZ = new CustomFieldValue("ANZ", "21489");
    public static final CustomFieldValue SUBREGION_ROA = new CustomFieldValue("ROA", "21490");
    public static final CustomFieldValue SUBREGION_JAPAN = new CustomFieldValue("Japan", "21531");
    public static final CustomFieldValue SUBREGION_NORTH_EAST = new CustomFieldValue("North East", "21524");
    public static final CustomFieldValue SUBREGION_WEST_COAST = new CustomFieldValue("West Coast", "21526");
    public static final CustomFieldValue SUBREGION_TOLA = new CustomFieldValue("TOLA", "21527");
    public static final CustomFieldValue SUBREGION_NORTH_CENTRAL = new CustomFieldValue("North Central", "21528");
    public static final CustomFieldValue SUBREGION_NY_METRO = new CustomFieldValue("NY-Metro", "21525");
    public static final CustomFieldValue SUBREGION_SOUTH_EAST = new CustomFieldValue("South East", "21530");
    public static final CustomFieldValue SUBREGION_LATAM = new CustomFieldValue("LATAM", "21529");
    public static final CustomFieldValue SUBREGION_UNKNOWN = new CustomFieldValue("UNKNOWN", "21491");

    public static final CustomFieldValue AUDIENCE_DEVELOPER = new CustomFieldValue("Developer", "16610");
    public static final CustomFieldValue AUDIENCE_SECURITY = new CustomFieldValue("Security", "16611");
    public static final CustomFieldValue AUDIENCE_EXECUTIVE = new CustomFieldValue("Executive", "16612");
    public static final CustomFieldValue AUDIENCE_UNKNOWN = new CustomFieldValue("Unknown", "16613");
    public static final CustomFieldValue AUDIENCE_OTHER = new CustomFieldValue("Other", "16614");

    public static CustomFieldValue getEventType(String eventType) {
        if (eventType == null) { return null; }
        return switch (eventType.toLowerCase()) {
            case "conference" -> EVENT_TYPE_CONFERENCE;
            case "meetup" -> EVENT_TYPE_MEETUP;
            case "customer" -> EVENT_TYPE_CUSTOMER;
            case "webinar" -> EVENT_TYPE_WEBINAR;
            default -> null;
        };
    }

    public static CustomFieldValue getEventFormat(String eventFormat) {
        if (eventFormat == null) { return null; }
        return switch (eventFormat.toLowerCase()) {
            case "virtual" -> EVENT_FORMAT_VIRTUAL;
            case "in-person" -> EVENT_FORMAT_IN_PERSON;
            case "hybrid" -> EVENT_FORMAT_HYBRID;
            default -> null;
        };
    }

    public static CustomFieldValue getRegion(String region) {
        if (region == null) { return null; }
        return switch (region.toLowerCase()) {
            case "emea" -> REGION_EMEA;
            case "amer" -> REGION_AMER;
            case "apj" -> REGION_APJ;
            case "global" -> REGION_GLOBAL;
            default -> REGION_UNKNOWN;
        };
    }

    public static CustomFieldValue getSubregion(String subregion) {
        if (subregion == null) { return null; }
        String lower = subregion.toLowerCase();
        return switch (lower) {
            case "nemea" -> SUBREGION_NEMEA;
            case "sce" -> SUBREGION_SCE;
            case "anz" -> SUBREGION_ANZ;
            case "roa" -> SUBREGION_ROA;
            case "japan" -> SUBREGION_JAPAN;
            case "north east" -> SUBREGION_NORTH_EAST;
            case "west coast" -> SUBREGION_WEST_COAST;
            case "tola" -> SUBREGION_TOLA;
            case "north central" -> SUBREGION_NORTH_CENTRAL;
            case "ny-metro", "ny metro" -> SUBREGION_NY_METRO;
            case "south east" -> SUBREGION_SOUTH_EAST;
            case "latam" -> SUBREGION_LATAM;
            default -> SUBREGION_UNKNOWN;
        };
    }

    public static CustomFieldValue getAudience(String audience) {
        if (audience == null) { return null; }
        return switch (audience.toLowerCase()) {
            case "developer" -> AUDIENCE_DEVELOPER;
            case "security" -> AUDIENCE_SECURITY;
            case "executive" -> AUDIENCE_EXECUTIVE;
            case "unknown" -> AUDIENCE_UNKNOWN;
            case "other" -> AUDIENCE_OTHER;
            default -> AUDIENCE_UNKNOWN;
        };
    }

}
