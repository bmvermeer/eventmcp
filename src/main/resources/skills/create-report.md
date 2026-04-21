---
name: event-report
description: >
  Analyse any input (text, images, PDFs, or mixed content) about an event and produce a structured
  plain-text report, then submit it using the available createReport or updateReport tools. Use this
  skill whenever the user wants to create or update a report from event information — even if they
  just paste raw notes, upload a photo of a whiteboard, or hand over a PDF. Report scope varies
  widely: it may be a full event overview, a single talk or session write-up, a speaker profile, a
  workshop summary, or anything else the content calls for. Trigger phrases include: "create a report
  for", "write up this event/talk/session", "generate a report from", "report on this", "make a
  write-up", "event report", "talk summary", or any time the user provides event-related content
  and wants a deliverable submitted to Atlassian.
---

# Event Report Skill

Produce a polished, context-appropriate plain-text report from any event input and submit it via createReport or updateReport.

## Workflow

### 1. Gather User Input First — MANDATORY

Before doing anything else, always ask the user for their firsthand experience of the event.
Never create or update a report using only system data (event details, CFP statuses, board metadata).
System data is used for context only — not as a substitute for real human input.

Ask the user the following questions before proceeding:

How did the event go overall?
How did your talk(s) perform? (e.g. scores, ratings, audience size, reactions)
Were there any notable conversations, leads, or partnership opportunities?
Any follow-up actions needed?
Anything that went particularly well or could be improved?

Wait for the user's response before moving on. Do not create a skeleton or placeholder report in the meantime.
If the user's answers are sparse, use only what they provided — never pad with assumptions.

The user may provide input in any of the following forms, or a combination:
- Plain text (notes, summaries, agendas, minutes, chat exports)
- Images (photos, screenshots, scanned docs, whiteboards, slides)
- PDFs (programmes, decks, briefing packs, speaker bios)
- Mixed inputs (e.g. a PDF agenda + extra chat notes)

For images, use vision to extract all readable text and structure before writing the report.
For PDFs, extract and parse all content before writing the report.
For mixed inputs, combine all sources and reconcile any conflicts before writing.

---

### 2. Fetch Event Context

Once the user has provided input, fetch the parent event details using getIssue with the BTBFE event key.
Use the event data (name, dates, location, CFPs) to enrich the report with factual context.
Combine the user's input with the system data to build a complete picture.

---

### 3. Detect Report Type

Do not use a fixed template. Infer the most appropriate type from the content and adapt the sections accordingly. The list below is a guide, not an exhaustive list.

Report Types:

Full Event — entire conference, meetup, or multi-session event
Typical sections: Overview, Agenda/Sessions, Speakers, Highlights, Action Items, Stats

Single Talk / Session — one presentation, keynote, or panel
Typical sections: Overview, Speaker(s), Key Takeaways, Demo/Code Highlights, Resources, Q&A Notes

Workshop — hands-on interactive session
Typical sections: Overview, Facilitator(s), Objectives, What Was Covered, Outcomes, Follow-ups

Speaker Profile — bio, introduction, or speaker page
Typical sections: About, Topics/Expertise, Past Talks, Links

Meeting / Roundtable — discussion, debrief, or retrospective
Typical sections: Attendees, Agenda, Decisions, Action Items, Parking Lot

Event Recap — post-event summary or wrap-up
Typical sections: Highlights, What Went Well, Improvements, Attendance/Stats, Next Steps

If the content spans multiple types (e.g. a full day with notes on individual talks), ask whether the user wants one combined report or one report per session before proceeding.

---

### 4. Build the Report

#### Title

Infer a concise, descriptive title using the event name retrieved from the parent item:
- Full event:  [Event Name] - [Year] Report
- Talk:        [Talk Title] - [Speaker Name]
- Workshop:    [Workshop Name] - Workshop Summary
- Recap:       [Event Name] - Recap
- Fallback:    [Event/Topic descriptor] - Report

#### Description (plain text only)

The description field accepts plain text only. This is a hard constraint from the Atlassian tool.

Rules:
- No markdown syntax of any kind (no *, **, #, -, |, >, backticks, etc.)
- No bullet point symbols or list markers
- No table syntax
- No bold, italic, or underline markers
- No special characters used for formatting

Structure content using:
- Section labels on their own line, followed by a blank line, then the content
- Blank lines between sections
- Plain prose sentences and paragraphs
- For list-like content, write each item on its own line as a plain sentence or phrase
- For tabular data, write each row as a plain line, e.g.:
  Speaker: Jane Doe | Topic: Zero Trust | Time: 10:00
  or in prose:
  Jane Doe presented on Zero Trust at 10:00.

Example of correct plain-text structure:

Overview
This was a 3-day developer conference held in Amsterdam on 12 to 14 March 2026, focused on platform engineering and developer tooling.

Speakers
Jane Doe - Zero Trust Security
John Smith - AI Observability at Scale
Marta Kowalski - Developer Experience Metrics

Key Takeaways
Platform engineering is becoming a core discipline rather than an ops afterthought.
AI observability tooling is still immature but moving fast.
Developer experience metrics are hard to define but teams are converging on DORA.

Action Items
Follow up with Sarah regarding the demo environment by 20 March.
John to share his slide deck with the team by end of week.
Review the workshop materials before the next sprint.

Only include sections for which actual content exists. Never fabricate or pad with placeholder content.

---

### 5. Create or Update

The createReport and updateReport tools are available. Use them directly — do not ask the user to submit the report manually.

If the user has not specified create vs update, ask:
"Should I create a new report, or update an existing one? If updating, please share the BTBFE issue key (e.g. BTBFE-2400)."

Create a new report:
createReport(
title       = "<report title>",
eventKey    = "<parent event BTBFE key, e.g. BTBFE-2362>",
description = "<plain text report body>"
)

Update an existing report:
updateReport(
issueKey    = "<report BTBFE key, e.g. BTBFE-2400>",
title       = "<updated title, or blank to keep current>",
description = "<updated plain text body, or blank to keep current>"
)

---

## After Submission

MANDATORY — do not skip any of these steps, in this exact order.

Step 1 — SHOW THE FULL REPORT
Immediately after the tool call completes, output the full report in the chat exactly as submitted.
Use this format:

Title: <report title>

<full description body, every line, nothing omitted>

Do not summarise. Do not say "the report has been submitted". Do not move on. Show the complete text first.

Step 2 — LIST THE SECTIONS
After the report body, list which sections were included, one per line.

Step 3 — ASK FOR CONFIRMATION — STOP AND WAIT
After listing the sections, ask the user explicitly:
"Does this look correct? If so, I'll mark the report as Done in Jira. Please confirm before I proceed."

Do not call any further tools until the user replies.
Do not assume confirmation. Do not proceed on silence.
If the user requests changes, apply them via updateReport, then repeat from Step 1.

Step 4 — TRANSITION TO DONE (only after explicit confirmation)
Once the user has confirmed, call getTransitions with the report issue key.
Find the transition whose name matches "Done" (case-insensitive).
Call updateStatus with the report issue key and the matching transition id.

Step 5 — CONFIRM COMPLETION
Tell the user the report has been marked as Done in Jira.

---

## Edge Cases

Input has no dates or times
The parent event item is the first place to look — dates and location are typically stored there. Only omit time-based content if it is absent from both the parent item and the user input. Never fabricate.

Input is image-only
Use vision to extract all readable text and structure before writing the report.

Content spans multiple talks or sessions
Ask whether user wants one combined report or one per session.

Partial update requested (e.g. "just add action items")
Build only the new or changed sections and call updateReport.

Speaker info only, no talk content
Use the Speaker Profile type.

Content is very sparse
The parent event item may provide enough context to produce a useful overview. If content is still minimal after combining both sources, produce a short honest report and note at the end what information was unavailable.

