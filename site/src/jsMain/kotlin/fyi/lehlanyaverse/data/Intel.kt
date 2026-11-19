package fyi.lehlanyaverse.data

/*
 * ┌──────────────────────────────────────────────────────────────┐
 * │  LEHLANYAVERSE INTEL: every word on the site lives here.     │
 * │  Add a project: append an Operation to `operations`.         │
 * │  Add a paper:   append an IntelPaper to `fieldIntel` (give   │
 * │                 it an id; linkedTo ties it to other papers). │
 * │  Update CV:     ./scripts/update-cv.sh <new.pdf>      │
 * │  No layout code needs to change. See README.md.              │
 * └──────────────────────────────────────────────────────────────┘
 */
object Intel {

    val operator = Operator(
        name = "Sebabatso Moleele",
        callsign = "LEHLANYA",
        role = "Software Engineer",
        specialty = "Java Backend",
        location = "Johannesburg, ZA",
        coordinates = "26.2041°S 28.0473°E",
        status = "ACTIVE DUTY",
        // false: the beacon goes dim and reads "ON ASSIGNMENT"
        openToOpportunities = true,
        availabilityNote = "Roles: Backend / Java / Kotlin — Johannesburg or remote",
        clearance = "LEVEL 4 // BACKEND",
        bio = listOf(
            "Java backend engineer with 4+ years of commercial experience in Java and REST API development, " +
                "shipping production systems across fintech and edtech, including integrations with Bankserv, HANIS " +
                "and Active Directory. Now transitioning into Kotlin.",
            "Strong in Spring Boot, system design, RabbitMQ/Kafka messaging and MongoDB/MySQL data modelling; " +
                "writes well-tested code with JUnit, Mockito and pytest, and automates business functions with Java, " +
                "Python and AI agents.",
            "Security-first, Agile, and a mentor to junior developers.",
        ),
        stats = listOf(
            Stat("4+", "Years commercial Java"),
            Stat("500", "Learners on live analytics", redacted = true),
            Stat("~4h", "Saved weekly by automation"),
            Stat("2", "Sectors: fintech + edtech"),
        ),
        // Regenerate the image files with: npm --prefix tools/og run photos -- <folder of originals>
        photos = listOf(
            Photo("/images/operator-1.jpg", "Photo of Sebabatso Moleele, studio portrait", "OPERATOR: S. MOLEELE", "ID PHOTO // 2026"),
            Photo("/images/operator-2.jpg", "Photo of Sebabatso Moleele, relaxing on a balcony", "OFF DUTY", "SIGHTED: JHB"),
            Photo("/images/operator-3.jpg", "Photo of Sebabatso Moleele, selfie in a beanie and glasses", "FIELD RECON", "CALLSIGN LEHLANYA"),
        ),
    )

    // TODO(sebabatso): proficiency values are estimates. Adjust to taste (0–100).
    // Scale: high ≈ 70–90, medium = 55. Only pytest, Kotlin and Python sit at medium (IntelTest checks this).
    val loadout = Loadout(
        primary = listOf(
            Skill("Java", 90),
            Skill("Spring Boot", 88),
            Skill("REST API design", 88),
            Skill("Spring Data", 82),
            Skill("MongoDB", 80),
            Skill("Spring Security", 78),
            Skill("MySQL", 75),
            Skill("System design", 72),
            Skill("RabbitMQ", 70),
            Skill("Kafka (event-driven / async messaging)", 70),
        ),
        integration = listOf(
            Skill("REST/JSON APIs over HTTPS", 88),
            Skill("Third-party system integration (Bankserv, HANIS, Active Directory)", 85),
            Skill("Batch & real-time data sync", 82),
        ),
        testing = listOf(
            Skill("JUnit", 85),
            Skill("Unit & integration testing", 85),
            Skill("Mockito", 82),
            Skill("API testing", 82),
            Skill("pytest", 55, note = "Unit & integration testing in Python (fixtures, pytest-mock, respx, testcontainers)"),
        ),
        secondary = listOf(
            Skill("Kotlin", 55),
            Skill("Python (FastAPI)", 55),
        ),
        sidearms = listOf(
            Skill("HTML/CSS", 70),
            Skill("JavaScript", 65),
            Skill("Angular", 55),
            Skill("React", 45),
            Skill("Django", 40),
        ),
        equipment = listOf(
            Skill("Git", 88),
            Skill("Maven", 85),
            Skill("Docker", 70),
            Skill("Microsoft 365 / Teams Admin Center", 70),
            Skill("Gradle", 65),
            Skill("AWS", 60),
            Skill("GitHub Actions (CI/CD)", 70),
            Skill("Linux", 70),
        ),
    )

    val serviceRecord = listOf(
        Mission(
            codename = "OP NIGHT SCHOOL",
            role = "Software Engineer",
            unit = "WWISE",
            start = "JUL 2025", startIso = "2025-07",
            end = null, endIso = null,
            briefing = "Backend engineering for a Spring Boot + MongoDB learning-management platform.",
            objectives = listOf(
                "Built and maintain the Spring Boot + MongoDB LMS backend",
                "Shipped REST APIs with zero-downtime releases",
                "MongoTemplate aggregation pipelines powering real-time analytics for active learners",
                "Spring Security with custom authentication",
                "Built a Python grading automation bot that marks learner assessments",
                "Automated business functions with Java, Python and AI agents (~4 hrs/week saved)",
                "Mentor interns and juniors to QCTO criteria",
            ),
            stack = listOf("Java", "Spring Boot", "MongoDB", "Spring Security", "Python", "AI agents"),
        ),
        Mission(
            codename = "OP CLEAR LEDGER",
            role = "Junior Software Developer",
            unit = "Mpilo Technologies",
            start = "2024", startIso = "2024",
            end = "JUN 2025", endIso = "2025-06",
            briefing = "Fintech integrations: bank account validation and identity verification.",
            objectives = listOf(
                "Built Bankserv Account Validation Services (batch + real-time) in Java",
                "Integrated HANIS identity verification for automated KYC",
                "API testing and code reviews across the team",
                "Diagnosed and fixed a production session-caching defect",
            ),
            stack = listOf("Java", "REST", "Bankserv AVS", "HANIS"),
        ),
        Mission(
            codename = "OP FIRST LIGHT",
            role = "Graduate Developer",
            unit = "Mpilo Technologies",
            start = "2023", startIso = "2023",
            end = "2024", endIso = "2024",
            briefing = "Graduate rotation across identity, frontend and mobile-sensor features.",
            objectives = listOf(
                "Microsoft Active Directory integration",
                "Angular frontends on Java REST APIs",
                "Accelerometer step-counter feature, delivered end-to-end",
            ),
            stack = listOf("Java", "Angular", "Active Directory"),
        ),
    )

    // TODO(sebabatso): add credentialUrl links (Credly etc.) where you have them.
    val commendations = listOf(
        Commendation("Exposure Management Expert", "XM Cyber", CommendationKind.CERTIFICATION, CommendationGroup.SECURITY, awarded = 2025),
        Commendation("Certified in Cybersecurity (CC)", "ISC2", CommendationKind.CERTIFICATION, CommendationGroup.SECURITY, awarded = 2025, expires = 2028),
        Commendation("ISO/IEC 27001 ISMS Implementation", "WWISE", CommendationKind.COMPLETION, CommendationGroup.SECURITY, awarded = 2026),
        Commendation("ISO/IEC 27001 Lead Auditor", "WWISE", CommendationKind.TRAINING, CommendationGroup.SECURITY, awarded = 2026),
        Commendation("AWS Certified Cloud Practitioner", "Amazon Web Services", CommendationKind.CERTIFICATION, CommendationGroup.CLOUD, awarded = 2023, expires = 2026),
        Commendation("Software Engineering Certificate", "HyperionDev", CommendationKind.EDUCATION, CommendationGroup.CLOUD, awarded = 2022),
        Commendation("National Senior Certificate", "Gimnasium", CommendationKind.EDUCATION, CommendationGroup.EDUCATION, awarded = 2016),
    )

    val operations = listOf(
        Operation(
            slug = "grading-automation-bot",
            codename = "OP RED PEN",
            title = "Grading Automation Bot",
            status = OpStatus.DEPLOYED,
            summary = "A Python bot that marks learner assessments automatically, freeing facilitators from repetitive grading.",
            briefing = listOf(
                "Learner assessments at WWISE were being marked by hand, which cost facilitators hours every week " +
                    "and slowed down feedback to learners.",
                // TODO(sebabatso): describe how it works: inputs, marking logic, where results land.
                "The bot ingests submissions, applies the marking rubric and records results, so humans only " +
                    "review the edge cases.",
            ),
            objectives = listOf(
                "Automate marking of learner assessments",
                "Cut manual grading time for facilitators",
                "Return feedback to learners faster",
            ),
            stack = listOf("Python"), // TODO(sebabatso): add libraries / services used
            repoUrl = null, // TODO(sebabatso): GitHub link (or leave null if private)
            liveUrl = null,
            screenshot = null, // TODO(sebabatso): Screenshot("/img/projects/grading-bot.webp", "Describe the screenshot")
        ),
        Operation(
            slug = "kotlin-rabbitmq-event-pipeline",
            codename = "OP SIGNAL FIRE",
            title = "Kotlin + RabbitMQ Event Pipeline",
            status = OpStatus.IN_PROGRESS,
            summary = "An event-driven pipeline in Kotlin and Spring Boot, using RabbitMQ to decouple producers from consumers.",
            briefing = listOf(
                "Part of the move from Java to Kotlin: rebuilding a familiar event-driven pattern idiomatically in Kotlin.",
                // TODO(sebabatso): describe the domain, the events and what you're learning.
                "Focus areas: reliable delivery, retries and dead-letter queues, and idempotent consumers.",
            ),
            objectives = listOf(
                "Publish and consume domain events through RabbitMQ",
                "Dead-letter handling and retry strategy",
                "Idiomatic Kotlin with Spring Boot",
            ),
            stack = listOf("Kotlin", "Spring Boot", "RabbitMQ"),
            repoUrl = null, // TODO(sebabatso): GitHub link
            liveUrl = null,
            screenshot = null, // TODO(sebabatso): architecture diagram or screenshot
        ),
    )

    // TODO(sebabatso): starter papers. Replace the summaries (and add repo links) with your own.
    // `position` is where the paper's pin goes, as % of the board (x across, y down); leave it out and the paper
    // takes a free spot near the papers it links to. `linkedTo` runs red string between papers (either side counts).
    val fieldIntel = listOf(
        IntelPaper(
            id = "kotlin",
            title = "Java → Kotlin, one service at a time",
            type = IntelType.INDEX_CARD,
            teaser = "Null safety and coroutines without leaving the JVM.",
            // TODO(sebabatso): summary
            summary = "Null safety, data classes and coroutines without leaving the JVM or Spring. " +
                "Migrating one service at a time keeps every release shippable. This site is written in Kotlin too.",
            tags = listOf("Kotlin", "JVM"),
            date = "2026-06",
            link = "https://kotlinlang.org/docs/home.html",
            position = BoardSpot(13.0, 5.0),
            rotation = -3.0,
        ),
        IntelPaper(
            id = "events",
            title = "Event-driven systems that fail well",
            type = IntelType.CLIPPING,
            teaser = "Dead letters, idempotency, back-pressure.",
            // TODO(sebabatso): summary
            summary = "Dead-letter queues, idempotent consumers and back-pressure: the unglamorous parts that keep " +
                "pipelines alive when a downstream service has a bad day.",
            tags = listOf("Messaging", "RabbitMQ", "Architecture"),
            date = "2026-07",
            link = "https://www.rabbitmq.com/tutorials",
            linkedTo = listOf("kotlin"),
            position = BoardSpot(39.0, 11.0),
            rotation = 2.0,
        ),
        IntelPaper(
            id = "ai-agents",
            title = "AI agents for business automation",
            type = IntelType.PHOTO,
            teaser = "Where agents save real hours, and where a script wins.",
            // TODO(sebabatso): summary
            summary = "Where agents save real hours (grading, admin, reporting) and where a plain script is still the " +
                "better tool.",
            tags = listOf("AI", "Automation"),
            date = "2026-08",
            position = BoardSpot(64.0, 4.0),
            rotation = -1.5,
        ),
        IntelPaper(
            id = "coroutines",
            title = "Coroutines on the consumer side",
            type = IntelType.NOTE,
            teaser = "Structured concurrency for message consumers.",
            // TODO(sebabatso): summary
            summary = "Structured concurrency for message consumers: cancellation, back-pressure and not blocking the " +
                "listener thread.",
            tags = listOf("Kotlin", "Concurrency", "Messaging"),
            date = "2026-09",
            link = "https://kotlinlang.org/docs/coroutines-overview.html",
            linkedTo = listOf("events", "kotlin"),
            position = BoardSpot(25.0, 56.0),
            rotation = 3.0,
        ),
        IntelPaper(
            id = "zero-trust",
            title = "Zero trust for small teams",
            type = IntelType.INDEX_CARD,
            teaser = "ISO 27001 when the security team is one person.",
            // TODO(sebabatso): summary
            summary = "What ISO 27001 controls look like when the security team is one person and the auditor still " +
                "wants evidence.",
            tags = listOf("Security", "ISO 27001"),
            date = "2026-10",
            position = BoardSpot(87.0, 9.0),
            rotation = 2.5,
        ),
        IntelPaper(
            id = "zero-trust-ic2",
            title = "Zero trust for ISC2",
            type = IntelType.CLIPPING,
            teaser = "Zero trust, the ISC2 way.",
            // TODO(sebabatso): summary (this one was a copy of "Zero trust for small teams")
            summary = "TODO: what this note covers for ISC2, and how it differs from the small-team take.",
            tags = listOf("Security", "ISC2"),
            date = "2026-10",
            linkedTo = listOf("zero-trust"),
        ),
        IntelPaper(
            id = "eval-bots",
            title = "Testing bots that mark work",
            type = IntelType.NOTE,
            teaser = "How to trust an automated grader.",
            // TODO(sebabatso): summary, and a repo link (linkLabel = IntelLinkLabel.VIEW_REPO) if there is one
            summary = "How to trust an automated grader: golden answer sets, spot checks and flagging low-confidence " +
                "marks for a human.",
            tags = listOf("AI", "Testing"),
            date = "2026-10",
            linkedTo = listOf("ai-agents"),
        ),
    )

    val comms = listOf(
        CommsLink(CommsChannel.EMAIL, "EMAIL", "moleele25@gmail.com", "mailto:moleele25@gmail.com"),
        CommsLink(CommsChannel.LINKEDIN, "LINKEDIN", "in/sebabatso-moleele", "https://www.linkedin.com/in/sebabatso-moleele"),
        CommsLink(CommsChannel.GITHUB, "GITHUB", "Tuma2", "https://github.com/Tuma2"),
    )

    // Update with: ./scripts/update-cv.sh <new.pdf>  (copies the file, sets `updated` to today, commits, pushes).
    // The size label and the /cv.pdf copy are made at build time from the file. See README "Updating the CV".
    val cv = CvFile(
        path = "/cv/Sebabatso_Moleele_CV.pdf",
        fileName = "Sebabatso_Moleele_CV.pdf",
        updated = "2026-11-19",
    )
}
