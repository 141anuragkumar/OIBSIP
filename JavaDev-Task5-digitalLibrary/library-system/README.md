## जिज्ञासा Digital Library Management System

A Spring Boot web app with separate **Admin** and **User** roles for managing a book
catalogue, issuing/returning books, tracking overdue fines, and advance-booking
reservations.

## Tech stack
- Java 17, Spring Boot 3.3
- SQLite
- Server-rendered HTML/CSS 

## Run
- mvn spring-boot:run

The app starts on http://localhost:8080. A SQLite file `library.db` is created in
the working directory on first run, and a default admin account is seeded automatically:

Regular users register themselves via the "Register" link.

## Feature (where to find each requirement)

**Admin module** (`/admin/**`, requires ADMIN role)
- Login: shared `/login` page, routed to `/admin/dashboard` on success
- Add/edit/delete books: `/admin/books`, `/admin/books/new`, `/admin/books/{id}/edit`
- View issued books & due dates: `/admin/issued`
- View/manage member accounts (suspend/reactivate): `/admin/members`
- Mark fines as paid: `/admin/fines`
- View advance bookings: `/admin/reservations`
- View contact/query messages: `/admin/messages`

**User module** (requires USER role, or public where noted)
- Registration/login: `/register`, `/login` (public)
- Browse catalogue by category: `/catalogue?category=...`
- Search by title/author: `/search?q=...`
- Issue a book: POST `/issue/{bookId}` (decrements available copies, sets a 14-day due date)
- Return a book: POST `/return/{issueId}` (increments available copies, auto-calculates
  a ₹5/day fine if overdue)
- Advance booking: POST `/reserve/{bookId}` — only offered when a book has zero
  available copies. When a copy is returned, the oldest pending reservation is
  automatically marked "Ready", and that member gets first claim on the freed copy.
- My Books: `/my-books` — current & past issues, live overdue/fine status, and
  reservations (with cancel / issue-now actions)
- Contact/query form: `/contact` (stored in DB, visible to admins under Messages)

Business rules (loan length, fine per day) are configurable in
`src/main/resources/application.properties`:

```properties
library.issue.loan-days=14
library.fine.per-day=5.0
```

## One view of website here admin mode/user mode

