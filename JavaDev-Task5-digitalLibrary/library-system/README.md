## जिज्ञासा Digital Library Management System
click here to visit - https://oibsip-2-7m8c.onrender.com/

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
<img width="1919" height="988" alt="image" src="https://github.com/user-attachments/assets/599e0865-cdbc-471d-a049-255a9665455e" />
<img width="1916" height="987" alt="image" src="https://github.com/user-attachments/assets/94f27044-77ce-43d4-bc3b-7c4aec63a99d" />

## Admin pannel
<img width="1910" height="987" alt="image" src="https://github.com/user-attachments/assets/fa2dbc7c-7ea3-4b56-9ca6-cf800503377c" />
<img width="1913" height="983" alt="image" src="https://github.com/user-attachments/assets/c04437da-4784-4088-9868-924b078ac743" />





