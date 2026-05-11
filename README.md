# (BLCP) Banking Licensing & Compliance Platform

A full-stack web application for managing bank licensing applications in Rwanda. Applicants submit licence requests with supporting documents; officers review, approve, reject, or request resubmission through a structured workflow.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Angular 18, Angular Material, TypeScript 5.5, SCSS |
| Backend | Spring Boot 3.3, Java 17, Spring Security, Spring Data JPA |
| Database | PostgreSQL 16 (Docker) |
| Auth | JWT (5-min access token) + HttpOnly cookie refresh token (8 hr) |
| Migrations | Flyway |
| Build tools | Maven (backend), Angular CLI / npm (frontend) |

---

## Prerequisites

Install all of the following before you begin.

| Tool | Version | Check |
|---|---|---|
| **Java** | 17 or later | `java -version` |
| **Maven** | 3.9+ | `mvn -version` |
| **Node.js** | 18 or later | `node -v` |
| **npm** | 9 or later | `npm -v` |
| **Docker Desktop** | Any recent version | `docker -v` |
| **Angular CLI** | 18 | `ng version` |
| **IntelliJ IDEA** | 2023.1+ (Community or Ultimate) | ... |

Install Angular CLI globally if you don't have it:

```bash
npm install -g @angular/cli@18
```

---

## Project Structure
**Backend**:

**Frontend**:


---

## Setup

### 1. Clone & Environment

Clone the two repositories separately.

**Backend** (this repo):

```bash
# Option 1: GitHub CLI
gh repo clone rukundoeric/BLCPBackend

# Option 2: HTTPS
git clone https://github.com/rukundoeric/BLCPBackend.git
```

```bash
cd BLCPBackend
```

**Frontend** (separate repo, clone in a different terminal or directory):

```bash
# Option 1: GitHub CLI
gh repo clone rukundoeric/BLCPFrontend

# Option 2: HTTPS
git clone https://github.com/rukundoeric/BLCPFrontend.git
```

```bash
cd BLCPFrontend
```

Create the backend environment file from the example:

```bash
# Inside BLCPBackend/
cp .env.example .env
```

Open `.env` and set a JWT secret (any random string, minimum 32 characters):

```
JWT_SECRET=replace-with-a-secure-random-string-at-least-32-characters
```

You can generate one quickly with:

```bash
openssl rand -base64 48
```

> The `.env` file is loaded automatically by the backend at startup via Spring Dotenv.

---

### 2. Start the Database

From the `BLCPBackend/` directory:

```bash
docker compose up -d
```

This starts a PostgreSQL 16 container named `blcp_postgres` on host port **5433**.
Data is persisted to `BLCPBackend/data/postgres/`.


> Flyway will create and seed all tables automatically on the first backend startup.
> You do **not** need to run any SQL scripts manually.

---

### 3. Run the Backend

Choose either option, both produce the same result.

---

#### Option A: Terminal (Maven)

From the `BLCPBackend/` directory:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

---

#### Option B: IntelliJ IDEA

1. Open IntelliJ IDEA.
2. Choose **File → Open** and select the `BLCPBackend` folder. IntelliJ will detect the `pom.xml` and import the Maven project automatically.
3. Wait for the Maven sync to finish (watch the progress bar at the bottom of the window, it will say "Resolving dependencies…" then disappear when done).
4. Set up the environment variable:
   - Go to **Run → Edit Configurations…**
   - Select the **BlcpBackendApplication** run configuration (IntelliJ creates this automatically for Spring Boot apps)
   - In the **Environment variables** field, add: `JWT_SECRET=<your-secret-from-.env>`
   - Alternatively, if you are on IntelliJ IDEA 2023.1 or later, tick **Enable EnvFile** and point it at the `.env` file in the project root, IntelliJ will load it automatically and you can skip the manual step above.
   - Click **OK**.
5. Click the green **Run** button (▶️) in the top toolbar, or press **Shift + F10**.

IntelliJ will compile the project and start the application in its built-in Run console.

---


The API is now available at **http://localhost:9009**.

**What happens on first startup:**
- Flyway runs all migrations in `src/main/resources/db/migration/`
- All tables are created
- Roles, users, officers, and sample applications are seeded automatically

---

### 4. Run the Frontend

The frontend is a separate repository. Open a **new terminal**, clone it if you haven't already, then:

```bash
# Option 1: GitHub CLI
gh repo clone rukundoeric/BLCPFrontend

# Option 2: HTTPS
git clone https://github.com/rukundoeric/BLCPFrontend.git
```

```bash
cd BLCPFrontend
npm install
ng serve
```

Or after the first install, just:

```bash
npm start
```

The dev server compiles and starts on **http://localhost:4022**.


## Default Accounts

The following accounts are created automatically by the database seed migrations.

| Role | Email | Password |
|---|---|----|
| Applicant | `user@gmail.com` | `Test@123` |
| Officer (Level 1) | `officer@nbr.rw` | `Test@123` |
| Senior Officer (Level 2) | `senior.officer@nbr.rw` | `Test@123` |
| Admin | `admin@nbr.rw` | `Test@123` |

### Role capabilities

| Role | Can do |
|---|---|
| **Applicant** | Submit new applications, upload documents, view own applications, resubmit when requested |
| **Officer (L1)** | View all submitted applications, approve (escalate to L2), reject, or request resubmission |
| **Senior Officer (L2)** | View Level 2 and resubmitted applications, grant final approval or reject |
| **Admin** | Create users, assign officer roles |

---

## Application URLs

| Service | URL |
|---|---|
| Frontend | http://localhost:4022 |
| Backend API | http://localhost:9009 |
| Auth endpoints | http://localhost:9009/api/v1/public/auth/** |
| Applications API | http://localhost:9009/api/v1/applications |
| PostgreSQL | localhost:5433 (user: `blcp_user`, password: `blcp_password`, db: `blcp_db`) |

---

## Key Workflows

### Submitting an application (Applicant)

1. Log in as `user@gmail.com`
2. Click **Create application** in the sidebar
3. Fill in bank name, licence type, and notes
4. Attach supporting documents (PDF, max 5 MB each, max 25 MB total per request)
5. Submit: the application enters `SUBMITTED` state

### Reviewing an application (Officer)

1. Log in as `officer@nbr.rw`
2. Navigate to **Pending applications**
3. Open an application and click **Submit Review**
4. Choose an action:
   - **Approve**: escalates to Level 2 (`PENDING_FINAL_APPROVAL`)
   - **Request resubmission**: sends back to applicant (`PENDING_RESUBMISSION`), comment required
   - **Reject**: ends the process (`REJECTED`), comment required

### Final approval (Senior Officer)

1. Log in as `senior.officer@nbr.rw`
2. Navigate to **Pending applications** (shows Level 2 applications)
3. Open an application and click **Submit Review**
4. Choose **Approve** (final: `APPROVED`) or **Reject** (`REJECTED`)

### Resubmission (Applicant)

1. Log in as `user@gmail.com`
2. Open the application marked **Pending Resubmission**
3. Read the reviewer's comment in the blue banner
4. Click **Edit & Resubmit** and upload new or updated documents

---

## Running Tests

### Backend unit tests

```bash
cd BLCPBackend
./mvnw test
```

Or use IntelJ tool to run tests


---

## Troubleshooting

**`Unable to connect to the database`**
- Confirm Docker Desktop is running
- Run `docker compose ps` from `BLCPBackend/` and check the container status is `healthy`
- The backend connects to `localhost:5433`, make sure no other service is using that port

**`JWT_SECRET environment variable not set`**
- Make sure `.env` exists inside `BLCPBackend/` and contains a `JWT_SECRET` value
- The file must be named exactly `.env` (not `.env.local` or `.env.development`)

**`Flyway migration failed`**
- Usually caused by a leftover `data/postgres/` directory from a previous schema version
- Stop the container, delete `BLCPBackend/data/postgres/`, restart with `docker compose up -d`, then restart the backend

**`Port already in use`**
- Backend (9009): kill the process with `lsof -ti:9009 | xargs kill`
- Frontend (4022): kill the process with `lsof -ti:4022 | xargs kill`
- Database (5433): kill the process with `lsof -ti:5433 | xargs kill`

**Angular `ng: command not found`**
- Run `npm install -g @angular/cli@18` then retry
- Or use `npx ng serve` as an alternative

**Login fails with seed credentials**
- The BCrypt hashes in the seed file were generated at project creation time
- Check `BLCPBackend/src/main/resources/db/migration/V20260509164101__seed_users.sql` and verify the passwords with the project maintainer
