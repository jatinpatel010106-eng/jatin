# Project Proposal Options for College Viva

## Option 1: Student Skill Sharing Platform

A campus-focused web platform where students teach and learn from each other.

### Real-life use case
Students can post what they can teach (for example coding, design, public speaking, math), and peers can discover and join learning sessions.

### Core features
- Student authentication (register/login)
- Create skill posts with title, category, and session details
- Browse and join sessions
- Simple profile page (skills offered, sessions joined)
- Basic messaging between learner and mentor

### Why this is good for exam/demo
- Clear social impact in college context
- Easy to explain workflows end-to-end
- Demonstrates CRUD + authentication + relationships

### Cloud usage mapping
- **Data storage:** MongoDB Atlas
- **User management:** app auth + sessions (or JWT)
- **Scalable hosting:** Render / Railway / AWS EC2

---

## Option 2 (Recommended): Laundry Pickup Booking Website

A simple booking website for local laundry pickup and delivery service.

### Why this is the best simple + unique idea
- Very easy frontend and form flows
- Small, clean database model
- Real business use case everyone understands
- Fast to deploy on cloud and easy to present professionally

### Suggested MVP features
- User registration/login
- Place pickup request (address, date/time slot, clothing notes)
- Booking status tracking (Pending → Picked Up → Washing → Out for Delivery → Delivered)
- User booking history
- Admin view for all bookings

### Suggested simple tech stack
- **Frontend:** HTML, CSS, JavaScript
- **Backend:** Python Flask
- **Database:** MongoDB Atlas
- **Hosting/Cloud:** AWS EC2 / Render / Railway

### Suggested collections
- `users`
- `bookings`
- `status_history`

### Viva-friendly demo flow
1. Register as a user.
2. Book a pickup.
3. Show booking in dashboard.
4. Login as admin and change status.
5. Refresh user dashboard to show updated status.

This flow demonstrates authentication, CRUD, role-based actions, and cloud-hosted data in under 5 minutes.
