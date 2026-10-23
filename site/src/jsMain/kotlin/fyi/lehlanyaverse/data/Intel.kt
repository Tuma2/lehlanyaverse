package fyi.lehlanyaverse.data

/*
 * ┌──────────────────────────────────────────────────────────────┐
 * │  LEHLANYAVERSE INTEL: every word on the site lives here.     │
 * │  Add a project: append an Operation to `operations`.         │
 * │  Add a note:    append a FieldNote to `fieldIntel`.          │
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
        clearance = "LEVEL 4 // BACKEND",
        bio = listOf(
            "Java backend engineer with 4+ years of commercial experience in Java and REST API development, " +
                "shipping production systems across fintech and edtech. Transitioning into Kotlin.",
            "Strong in Spring Boot, REST API design, system design, event-driven messaging with RabbitMQ and " +
                "MongoDB/MySQL data modelling. Automates business functions with Java, Python and AI agents.",
            "Security-first. Mentors juniors and interns.",
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
        ),
        secondary = listOf(
            Skill("Kotlin", 50),
            Skill("Python", 65),
        ),
        sidearms = listOf(
            Skill("JavaScript", 55),
            Skill("Angular", 55),
            Skill("React", 45),
            Skill("FastAPI", 45),
            Skill("Django", 40),
        ),
        equipment = listOf(
            Skill("Git", 88),
            Skill("Maven", 85),
            Skill("Docker", 70),
            Skill("Microsoft 365 / Teams Admin Center", 70),
            Skill("Gradle", 65),
            Skill("AWS", 60),
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
        Commendation("Exposure Management Expert", "XM Cyber", CommendationKind.CERTIFICATION, awarded = 2025),
        Commendation("Certified in Cybersecurity (CC)", "ISC2", CommendationKind.CERTIFICATION, awarded = 2025, expires = 2028),
        Commendation("AWS Certified Cloud Practitioner", "Amazon Web Services", CommendationKind.CERTIFICATION, awarded = 2023, expires = 2026),
        Commendation("Software Engineering Certificate", "HyperionDev", CommendationKind.EDUCATION, awarded = 2022),
        Commendation("National Senior Certificate", "Gimnasium", CommendationKind.EDUCATION, awarded = 2016),
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

    // TODO(sebabatso): these are starter notes. Replace them with your own.
    val fieldIntel = listOf(
        FieldNote(
            title = "Java → Kotlin, one service at a time",
            note = "Null safety, data classes and coroutines without leaving the JVM or Spring. This site is written in Kotlin too.",
            tags = listOf("kotlin", "jvm"),
            logged = "2026-10",
            url = "https://kotlinlang.org/docs/home.html",
        ),
        FieldNote(
            title = "Event-driven systems that fail well",
            note = "Dead-letter queues, idempotent consumers and back-pressure: the unglamorous parts that keep pipelines alive.",
            tags = listOf("rabbitmq", "architecture"),
            logged = "2026-10",
            url = "https://www.rabbitmq.com/tutorials",
        ),
        FieldNote(
            title = "AI agents for business automation",
            note = "Where agents save real hours (grading, admin, reporting) and where a plain script is still the better tool.",
            tags = listOf("ai", "automation"),
            logged = "2026-10",
        ),
    )

    val comms = listOf(
        CommsLink(CommsChannel.EMAIL, "EMAIL", "moleele25@gmail.com", "mailto:moleele25@gmail.com"),
        CommsLink(CommsChannel.LINKEDIN, "LINKEDIN", "in/sebabatso-moleele", "https://www.linkedin.com/in/sebabatso-moleele"),
        CommsLink(CommsChannel.GITHUB, "GITHUB", "Tuma2", "https://github.com/Tuma2"),
    )
}
