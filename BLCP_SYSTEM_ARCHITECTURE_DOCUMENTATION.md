# BLCP System Architecture Documentation

**Banking Licensing & Compliance Platform**
National Bank of Rwanda (NBR)

---

## C4 Model: Level 1 - System Context

### What the system is

BLCP is bank licensing applications. Banks submit applications, NBR officers review them, and administrators manage officer accounts.

---

### Roles

#### APPLICANT

Representatives of a bank or financial institution applying for an operating license.

**Can:**
- Submit a new license application with supporting documents
- Resubmit an application after an officer requests corrections
- View their own applications

**Cannot:**
- See any other applicant's applications
- Take any review action
- Access the admin panel

**Why:**  An applicant should only see what belongs to them. This is enforced at API level.

---

#### OFFICER (Level 1)

NBR review officers responsible for the initial screening of applications.

**Can:**
- View applications at `processingLevel = LEVEL_1`
- On `SUBMITTED` and `RESUBMITTED` applications:
  - `APPROVE`: escalates the application to `PENDING_FINAL_APPROVAL`
  - `REJECT`: closes the application (comment required)
  - `REQUEST_FOR_ACTION`: returns the application to the applicant (comment required)

**Cannot:**
- Act on `PENDING_FINAL_APPROVAL` applications
- Grant a license (final approval)
- Create or manage officer accounts
- Review an application they already acted on at Level 1

**Why:** Level 1 handles initial screening only. Final approval requires a second, independent reviewer. The state machine enforces this through `OFFICER_LEVEL_VALIDATION`, which checks the officer's level on every state transition.

---

#### SENIOR_OFFICER (Level 2)

Senior NBR officers with final approval authority.

**Can:**
- View applications at `processingLevel = LEVEL_2` and applications in `RESUBMITTED` state
- On `PENDING_FINAL_APPROVAL` applications:
  - `APPROVE`: grants the license (terminal state)
  - `REJECT`: closes the application (comment required, terminal state)
  - `REQUEST_FOR_ACTION`: returns the application to the applicant (comment required)

**Cannot:**
- Act on freshly `SUBMITTED` applications; those go through Level 1 first
- Approve an application they also reviewed at Level 1
- Create or manage officer accounts

**Why:** Final approval requires two independent reviewers. This is enforced at the state machine level.

---

#### ADMIN

NBR system administrators who manage reviewer accounts.

**Can:**
- Create new Officer or Senior Officer accounts
- Assign an officer role to an existing user
- View all applications

**Cannot:**
- Submit or resubmit applications
- Take any review action (`APPROVE`, `REJECT`, `REQUEST_FOR_ACTION`)

**Why:** Admins manage who reviews applications; they do not participate in reviewing.

---
### Why I chose these roles

The requirements did not define roles explicitly, so I derived them from what the system actually does. The applicant submits, the officer reviews, the senior officer gives the final approval, and the admin manages accounts. Each role does one thing, and no single person can move an application through the entire pipeline alone.


**However, as we will see in the state machine documentation, the state machine does not depend on role names. Rather, I introduced a level-based mechanism. This means that in the state machine, it is not the role that dictates what someone can do; it is their officer level. This was done considering the scale possibility. we wouldn't want to change our state machine everytime we introduce a new role**

---

## C4 Model: Level 2 - Containers

### Overview

The system is made up of three containers. The Angular SPA runs in the user's browser, the Spring Boot API handles all business logic, and PostgreSQL stores all data. 

---

### Containers

#### Angular (Frontend / client)
The frontend application, served as a static build and runs entirely in the browser.

---

#### Spring Boot API (Backend / Server)
The backend application. All business logic lives here.

---

#### PostgreSQL
The only data store in the system.

---

### What is not here yet

1. **File storage:** Document files are currently not uploaded to any storage backend. The system current saves file metadata (name, size, mime type) to the database but does not write the file bytes anywhere.
2. **Caching:** Backend-level caching is usually a must for any backend api to improve performance. but I intentionally left it out because it would introduce an
   additional infrastructure component that must be managed. For the current scope, this is acceptable.
---

## Authentication: Dual-Token (JWT Access + Stateful Refresh) 

### How it works

Login returns two tokens:

**Access token:** A signed JWT (HMAC-SHA256), valid for 5 minutes. Returned in the response body and sent by the client as a `Bearer` token in the `Authorization` header on every request. The token embeds the user's UUID, email, roles, and expiry.

**Refresh token:** A random UUID, valid for 8 hours. The raw token is never stored; only its SHA-256 hash is saved in the `user_sessions` table. The raw value is delivered to the browser as an `HttpOnly`, `SameSite=Strict` cookie scoped to `/api/v1/public/auth`. JavaScript cannot read it.

When the access token expires, the client calls `POST /api/v1/public/auth/refresh`. The server looks up the hash, checks the session is not expired and not already used, marks it `used = true`, and issues a new access and refresh token pair (token rotation). On logout, all `user_sessions` rows for that user are deleted.

---

### Token flow

---

### What it protects against

| Threat | How |
|---|---|
| XSS token theft | Refresh token is HttpOnly; JS cannot read it. Access token is short-lived (5 min). |
| CSRF on refresh | SameSite=Strict prevents the cookie from being sent on cross-origin requests. |
| Stolen access token used after logout | Session existence check in the filter (step 5 above) blocks it. |
| Refresh token replay | The `used` flag detects a second use. On detection, all sessions for that user are deleted and they are forced to re-authenticate. |
| Stale role or deactivated account | User row is loaded from the database on every request (step 3), so any state change takes effect immediately. |

---

### Trade-offs

|                           | Detail                                                                                                                                                                                                                  |
|---------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Statefulness**          | The refresh layer is stateful, the server holds session rows. This is a deliberate trade-off for revocation capability. The access layer remains stateless (no DB hit per API call beyond the user-load in the filter). |
| **DB load**               | Each authenticated request loads the User row (filter) and each refresh rotates a session row. Acceptable at this scale.                                                                                                |
| **Depenency on Browsers** | Http cookies are managed by browsers alone. this coulf be an issue there is a browsers that doesn't manage them the way we are expecing.                                                                                |

### What we could consider for a production product
Although Dual-Token is slightly safer than plain JWT, we could consider a much safer mechanism which is DPoP (Demonstrating Proof of Possession). on this one, On login, the client generates a key pair and embeds the public key in the JWT. On every request, the client signs a small proof (method, URL, timestamp) with the private key. The server verifies both the token and the proof.
### Trade-offs

| | Detail |
|---|---|
| **Security** | Significantly stronger. Stolen tokens are useless without the corresponding private key. |
| **Complexity** | The client must generate and manage a key pair. Every request requires a signing operation. The server must verify two separate signatures per request. |


### However!!
**There is no such a thing a 100% security measures. all these design do is just to reduce the chances of vulnerability**




---

## State Machine

### Overview

The application lifecycle is managed by a custom state machine. When an actor triggers an event on an application, the engine looks up the transition for that `(event, current status)` pair, runs a chain of actions, updates the application status, and writes an audit log entry.

The state machine is configured as a static lookup table. There is no external library; each transition is a plain data record that lists the event, the starting status, the target status, the processing level to set, and two lists of actions to run.

Here is a sample of our state machine configuration: 
```aiignore
   {
    "APPLY:NEW": {
    "event": "APPLY",
    "fromState": "NEW",
    "toState": "SUBMITTED",
    "processingLevel": "LEVEL_1",
    "breakingActions": [{
      "type": "SET_APPLICATION_ATTACHMENTS"
    }],
    "nonBreakingActions": [
      { "type": "NOTIFICATION" }
    ]
  },
  "APPROVE:SUBMITTED": {
    "event": "APPROVE",
    "fromState": "SUBMITTED",
    "toState": "PENDING_FINAL_APPROVAL",
    "processingLevel": "LEVEL_2",
    "breakingActions": [
      {
        "type": "OFFICER_LEVEL_VALIDATION",
        "args": { "requiredLevel": "LEVEL_1" }
      },
      {
        "type": "RECORD_OFFICER_REVIEW",
        "args": {
          "officerIdKey": "LEVEL1_OFFICER_ID",
          "commentKey": "LEVEL1_OFFICER_COMMENT"
        }
      }
    ],
    "nonBreakingActions": [
      { "type": "NOTIFICATION" }
    ]
    }
   }
```

---

### Why level-based, not role-based

As mentioned in the roles section, the state machine does not check role names. It checks the actor's **officer level**. This was a deliberate design choice: if a new role is introduced later, the state machine requires no changes. Only the level assigned to that role matters for workflow purposes.

---

### Application statuses

| Status | Meaning |
|---|---|
| `NEW` | Application created, not yet submitted |
| `SUBMITTED` | Applicant has submitted; waiting for Level 1 review |
| `PENDING_RESUBMISSION` | An officer requested corrections; waiting for the applicant to resubmit |
| `RESUBMITTED` | Applicant has resubmitted; back in the Level 1 queue |
| `PENDING_FINAL_APPROVAL` | Level 1 approved; waiting for Level 2 final decision |
| `APPROVED` | License granted. Terminal state. |
| `REJECTED` | Application denied. Terminal state. |

---

### Events

| Event | Who triggers it | Meaning |
|---|---|---|
| `APPLY` | APPLICANT | Submit the application |
| `APPROVE` | OFFICER (L1) or SENIOR_OFFICER (L2) | Move the application forward |
| `REJECT` | OFFICER (L1) or SENIOR_OFFICER (L2) | Deny the application |
| `REQUEST_FOR_ACTION` | OFFICER (L1) or SENIOR_OFFICER (L2) | Request corrections from the applicant |
| `RESUBMIT` | APPLICANT (owner only) | Submit updated application after corrections |

---

### Transition map

| Event | From | To | Processing level set after execution | Who can trigger |
|---|---|---|--------------------------------------|---|
| `APPLY` | `NEW` | `SUBMITTED` | `LEVEL_1`                            | APPLICANT |
| `APPROVE` | `SUBMITTED` | `PENDING_FINAL_APPROVAL` | `LEVEL_2`                            | OFFICER (L1) |
| `APPROVE` | `RESUBMITTED` | `PENDING_FINAL_APPROVAL` | `LEVEL_2`                            | OFFICER (L1) |
| `APPROVE` | `PENDING_FINAL_APPROVAL` | `APPROVED` | (unchanged)                          | SENIOR_OFFICER (L2) |
| `REJECT` | `SUBMITTED` | `REJECTED` | (unchanged)                          | OFFICER (L1) |
| `REJECT` | `RESUBMITTED` | `REJECTED` | (unchanged)                          | OFFICER (L1) |
| `REJECT` | `PENDING_FINAL_APPROVAL` | `REJECTED` | (unchanged)                          | SENIOR_OFFICER (L2) |
| `REQUEST_FOR_ACTION` | `SUBMITTED` | `PENDING_RESUBMISSION` | `LEVEL_1`                            | OFFICER (L1) |
| `REQUEST_FOR_ACTION` | `RESUBMITTED` | `PENDING_RESUBMISSION` | `LEVEL_1`                            | OFFICER (L1) |
| `REQUEST_FOR_ACTION` | `PENDING_FINAL_APPROVAL` | `PENDING_RESUBMISSION` | `LEVEL_2`                            | SENIOR_OFFICER (L2) |
| `RESUBMIT` | `PENDING_RESUBMISSION` | `RESUBMITTED` | `LEVEL_1`                            | APPLICANT (owner) |

Any event fired against a status not listed above is rejected with `INVALID_STATE_TRANSITION`.

---

### Actions

Each transition carries two lists of actions: breaking and non-breaking.

**Breaking actions** run before the status is changed. If any of them throws, the transition is aborted and the application status stays unchanged.

**Non-breaking actions** run asynchronously after the status has been committed. Failures are logged but do not affect the transition outcome.

---

#### Breaking actions

| Action | What it does |
|---|---|
| `OFFICER_LEVEL_VALIDATION` | Checks that the acting user holds an officer record at the required level. Blocks the transition if they do not. |
| `DUPLICATE_REVIEWER_VALIDATION` | Checks that the Level 2 actor is not the same person who reviewed the application at Level 1. Blocks the transition if they are. |
| `APPLICANT_OWNERSHIP_VALIDATION` | Checks that the resubmitting user is the same user who originally submitted the application. Blocks the transition if they are not. |
| `SET_APPLICATION_ATTACHMENTS` | Validates uploaded file sizes and saves document metadata to the database on initial submission. |
| `RESUBMIT_DOCUMENTS` | Same as above, but for resubmission. Saves new documents under an incremented submission version. |
| `RECORD_OFFICER_REVIEW` | Saves the acting officer's ID and comment into the application's preferences map. This is what `DUPLICATE_REVIEWER_VALIDATION` reads on the next review step. |

#### Non-breaking actions

| Action | What it does |
|---|---|
| `NOTIFICATION` | Logs a notification event. Intended as the integration point for email or SMS alerts. Currently a log-only placeholder. |

---

### Execution order

For every transition the engine runs in this order:

1. Look up the `(event, current status)` pair. If no transition exists, throw `INVALID_STATE_TRANSITION`.
2. Run all breaking actions in sequence. If any fails, stop and propagate the error.
3. Update the application status and processing level.
4. Write an audit log entry (event, from status, to status, actor, comment).
5. Run all non-breaking actions asynchronously.

---

### Audit log

Every transition writes one row to the `audit_log` table regardless of the outcome of non-breaking actions. The entry records the application number, the event, the previous and new status, the actor, and any comment. This gives a complete, append-only history of every state change an application has gone through.

### Trade-offs
1. **Static config vs a proper state machine library (Spring State Machine, etc.):**                                                                                                                                                                                              
   The lookup table is simple and has no magic, but it has no built-in visualization and no persistence of in-flight state. A library gives you all of
   that at the cost of a heavy dependency. 
2. **Non-breaking actions use the default thread pool with no retry:**
   NOTIFICATION runs as a fire-and-forget async task. If the thread pool fails and throws an error, the failure is logged and dropped. There is no retry, no dead letter queue, and the applicant gets no notification. This is acceptable now since notifications are a
   placeholder, but it would become a problem once email or SMS is introduced.

### why these Trade-offs are acceptable for now

1. **State machine Library complexity and dependency:** a library state machine would help solve many problems at the cost losing control over most of the things we have and need  control over. With custom state machine, we own our own model. Every developer on the team can read the transition table and understand exactly what happens, in what order, and why. There is no framework abstraction to learn before you can debug a production issue.
2. **Time constraint:** Currently all non-breaking actions runs as a fire-and-forget async task. If something happens that makes it fail, the failure is logged and dropped. There is no retry, no dead letter queue, and the applicant gets no notification. This is acceptable considering notifications are not expected to work for now. 

---

## Data Model

---

### Relationship decisions

**`officers` is a separate table, not a column on `user_roles`**
A user holding the `OFFICER` role is not the same thing as being a reviewing officer with a level. The `officers` table is where the level (`LEVEL_1` or `LEVEL_2`) lives. This separation means the state machine can check officer level independently of role name, and a user can hold the role without being assigned to the review workflow yet, which is useful during account setup.

**`application_preferences` is a key-value table, not columns on `applications`**
The preferences table stores workflow metadata that the state machine writes during transitions: the Level 1 officer ID, the Level 2 officer ID, their comments, and the officer who requested corrections. Adding these as columns on `applications` would couple the schema to the current workflow steps. With a key-value table, adding a new step that needs to store data requires no schema change.

**`attachments` and `application_attachments` are two tables instead of one**
`attachments` stores the file itself (metadata, path, uploader). `application_attachments` is the join that links a file to an application and adds context specific to that link: the document type and the submission version. This allows the same physical file to theoretically be attached to multiple applications without duplication, and it keeps the submission version tracking cleanly on the join record rather than on the file itself.

**`audit_log.application_number` is denormalized alongside the FK**
The audit log has both `application_id` (UUID FK) and `application_number` (the human-readable identifier). The number is stored directly so audit log queries can be filtered and displayed by application number without joining back to the applications table. Since the audit log is append-only and the application number never changes, there is no consistency risk.

---
## Audit Logging: Append-Only Enforcement

---
The `audit_log` table is the system's audit log. Every state change in the application lifecycle is recorded here, including the actor, the event, the before/after states, and any officer comment. The record must be immutable after creation: no row may be modified or deleted, including by an administrator.

## Enforcement Layers

Three independent layers enforce this guarantee. Each guards a different attack surface. All three are active simultaneously.

### Layer 1: PostgreSQL Trigger (database level)
This one protect against direct DB operations. So No delete or update operations are acceptable on the audit_log table. the definition of this trigger can be found in the migration directory.

**Why this is the primary guarantee:** it operates entirely at the database level.

**Trade-off:** The DB Admin/Owner can still be able to delete this trigger and allow those operations. but we would know who to hold accountable.

### Layer 2: `@Immutable` (Hibernate)
@Immutable tells Hibernate to never issue an UPDATE on this entity.

**Trade-off:** @Immutable does not prevent DELETE. Hibernate will still execute a delete if you call entityManager.remove() on an immutable entity. and native SQL can delete rows freely. so this one alone is not enough, that is why we combined it with the layer one on database level.  this one most purpose is to communicate this to the developers so they know which entities they will no bother try to implement UPDATE or DELETE operations over.

### Layer 3: Narrow Repository Interface (application level)
This one is also just like a reminder. Extending Repository instead of JpaRepository means Spring Data only generates the methods you explicitly declare, so delete or update are never available anywhere in the codebase. It is a code-level guard against accidental misuse.

**Trade-off:** Still tho, someone can still inject EntityManager directly and delete rows that way. Which is also why all three layers had to be combined. 

---

## API Documentation

*[Find Postman collection here]*

### Response structure

Every endpoint returns the same envelope.

**Success**
```json
{
  "traceId": "e3729a9b-5539-428d-858b-c320896e4a1a",
  "data": { ... },
  "timestamp": "2026-05-11T05:49:12.000Z"
}
```

**Error**
```json
{
  "traceId": "e3729a9b-5539-428d-858b-c320896e4a1a",
  "error": {
    "errorCode": "INVALID_STATE_TRANSITION",
    "errorMessage": "This action is not allowed in the current application state",
    "details": { ... }
  },
  "timestamp": "2026-05-11T05:49:12.000Z"
}
```

The `traceId` is the same ID logged on the server for that request, so a specific failure can be traced directly from the client response to the server logs. The `details` field is only present on validation errors and contains a map of field names to error messages. It is omitted on all other error types.
