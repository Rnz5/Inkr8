<p align="center">
  <img src="https://i.imgur.com/KiLgK6a.png" alt="Inkr8 Logo" width="400"/>
</p>
<h1 align="center">Inkr8</h1>
 
<p align="center">
  <strong>The competitive English writing arena.</strong>
</p>
<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.inkr8">
    <img src="https://img.shields.io/badge/Google%20Play-Available%20Soon-3DDC84?logo=google-play&logoColor=white" alt="Google Play"/>
  </a>
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" alt="Android"/>
  <img src="https://img.shields.io/badge/License-Proprietary-red" alt="Proprietary"/>
  <img src="https://img.shields.io/badge/Status-In%20Development-orange" alt="Status"/>
</p>

## Content Database
 
The vocabulary database — words, themes, and on-topic writing prompts used in Inkr8's challenges — is publicly available for reference and community contribution:
 
**[Inkr8-Content Repository →](https://github.com/Rnz5/Inkr8-Content)**
 
---
 
## The Problem
 
Most English learning apps are passive. You memorize words you never use. You read grammar rules you never apply. You get praised for showing up, not for writing well.
 
There's nowhere online where a non-native English speaker can **write under real constraints, receive honest AI judgment, and compete for visible status** — without it devolving into a pay-to-win game or a "good job, keep it up!" echo chamber (DUOLINGO).
 
**Inkr8 is the answer.** It forces you to use vocabulary actively, judges your writing without flattery, and gives your skill a public rank that actually means something.
 
> *"A good artist can paint with any color; a great writer should write with any word."*
 
 
## What Inkr8 Does
 
Inkr8 is a **competitive writing platform for Android** where users:
 
- Write short paragraphs using **randomly assigned required words**
- Receive an honest **AI score from 0.00% to 100.00%** across grammar, coherence, creativity, vocabulary, structure, and more
- Earn **Merit** — an in-app currency that cannot be purchased, only earned through skill
- Climb through **ranked leagues**: Scribe → Stylist → Author → Novelist → Laureate → Luminary → Pantheon
- Enter **tournaments** with prize pools, entry fees, and public leaderboards
- Build a **visible competitive identity** through profile, rank, badges, and reputation
The emotional atmosphere is deliberate: **dark, premium, intellectual, sarcastic, and competitive**. Writing well gives status. Writing poorly costs you.
 
## Core Features
 
**Writing System**
- Two gamemodes: *Standard Writing* (free-form, 4 required words) and *On-Topic Writing* (assigned theme + topic, 2 required words)
- Three play modes: *Practice* (risk-free), *Ranked* (affects competitive standing), *Tournament* (high-stakes events)
- Required words that highlight in real time as you type
**R8 — The AI Evaluator**
- Powered by OpenAI's GPT-4o mini, running server-side via Firebase Cloud Functions
- Evaluates craft, not opinion — grammar, coherence, depth, vocabulary, metaphors, structure, and required word usage
- Delivers feedback in a sharp, sarcastic voice: direct, useful, never empty praise
- A score of 90+ is rare. A perfect 100 is nearly unreachable
**Merit Economy**
- Merit is earned by writing and competing — it cannot be purchased
- Spent on ranked entry, tournaments, saving submissions, username changes, and more
- Subject to a weekly tax, a soft cap, and a slow-release overflow mechanic — it stays meaningful
**Competitive Structure**
- Ranked leagues with rating-based progression and seasonal resets planned
- The Pantheon: the top 100 players by rating, publicly visible
- Tournament system: enrollment windows, submission deadlines, automated prize distribution, and post-tournament tipping
- Reputation score: a hidden behavioral metric that affects ranked entry costs and tournament access
**Philosopher Subscription**
- No ads, free expanded feedback, free example sentences, profile glow and badge
- Sells comfort and identity, never score advantage or competitive edge
---
 
## Security & Privacy
 
Inkr8 is built with security-first practices throughout the stack:
 
- **Server-side trust** -> all economy actions (Merit changes, ranked entry, tournament enrollment, reward distribution) are executed exclusively through Firebase Cloud Functions. The client app never directly writes to sensitive user fields.
- **Firebase Authentication** -> Google Sign-In with Firebase Auth handles all identity management. No passwords are stored.
- **Firestore Security Rules** -> read/write access is enforced at the database level, not just in app logic. Users can only access their own data.
- **No client-side secrets** -> API keys and sensitive configuration are stored as Firebase Secret Manager secrets, accessed only by Cloud Functions at runtime.
- **Input validation** -> all user-submitted content is validated both client-side and server-side before any processing or storage.
- **HTTPS enforced** -> all communication with Firebase services and the OpenAI API is encrypted in transit via HTTPS/TLS.
---
 
## Tech Stack
 
| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Backend | Firebase (Auth, Firestore, Cloud Functions) |
| Functions Language | TypeScript (Node.js 18) |
| AI Evaluation | OpenAI GPT-4o mini |
| Ads | Google AdMob |
| Architecture | MVVM-aligned, repository pattern, server-side economy |
 
**Key architectural decisions:**
 
- All economy mutations run inside Firestore transactions on the server, preventing race conditions and ensuring atomic Merit operations
- Tournament evaluation is triggered by Firestore document state changes, keeping the evaluation pipeline decoupled from the client
- A scheduled phase controller transitions tournaments through "ENROLLING → ACTIVE → EVALUATING → COMPLETED" automatically, with refund logic triggered on cancellation
- The Merit overflow system routes earnings above the user's cap into a hold field, released gradually via a scheduled job - creating a sense of earning beyond limits without breaking economy balance
---
 
## Project Status & Roadmap
 
**Current:** Alpha v0.5.3 — core loop is fully functional end-to-end.
 
**Completed:**
- [x] Full writing → AI evaluation → results loop (Practice and Ranked)
- [x] Tournament system (creation, enrollment, evaluation, rewards, refunds, tipping)
- [x] Merit economy (cap, hold, weekly tax, slow-release, anti-hoarding mechanics)
- [x] Ranked leagues, rating system, reputation, and Pantheon
- [x] Profile, leaderboard, submissions archive, and saved submissions
- [x] Philosopher subscription flow (UI complete)
- [x] R8 identity and lore finalization

**Planned:**
- [ ] iOS version
- [ ] Website
- [ ] Claude Sonnet integration (planned upgrade from GPT-4o mini post-launch)
- [ ] Widget
- [ ] Expand to more languages~
      
> ⚠️ The source code for this project is **private and proprietary**. This repository serves as a public project reference. Unauthorized use, reproduction, or distribution is not permitted.
 
---
 
## Developer
 
**Renzo Adrianzén** — Solo developer, designer, and product architect
 
For business inquiries, collaboration, or beta testing interest:
 
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-0A66C2?logo=linkedin&logoColor=white)](https://www.linkedin.com/in/radrianzen/)
[![Email](https://img.shields.io/badge/Email-Contact-D14836?logo=gmail&logoColor=white)](mailto:renzoadrianzen387@gmail.com)
 
