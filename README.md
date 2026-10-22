# LEHLANYAVERSE

Personal portfolio of **Sebabatso Moleele**, a Java backend engineer, framed as a tactical mission dossier.
Live at **https://lehlanyaverse.fyi**.

Written in **Kotlin** with [Kobweb](https://kobweb.varabyte.com) (Compose HTML) and shipped as a **static export**:
plain HTML/CSS/JS with every page pre-rendered, no server, and no third-party requests.

---

## Quick start

**Prerequisite:** JDK 21. Gradle comes with the wrapper, so nothing else is needed.

```bash
./gradlew :site:kobwebStart -t     # dev server on http://localhost:8080, live reload
./gradlew :site:kobwebStop         # stop it
./gradlew :site:jsNodeTest         # data-integrity tests for Intel.kt
```

In IntelliJ, use the shared run configurations: **Site: dev server (live reload)**, **Site: stop server** and **Site: static export**.

The first visit plays a short boot intro. To see it again, clear site data or run
`localStorage.removeItem("lv.booted")` in the browser console.

---

## Editing content: one file

**Everything on the site lives in [`site/src/jsMain/kotlin/fyi/lehlanyaverse/data/Intel.kt`](site/src/jsMain/kotlin/fyi/lehlanyaverse/data/Intel.kt).**
The data types are in `Model.kt` next to it. Layout code never needs to change for content edits.
Search the file for `TODO(sebabatso)` to find what still needs your input.

### Add a project (Active Operations)

Append an `Operation` to `Intel.operations`:

```kotlin
Operation(
    slug = "payments-gateway",            // URL: /operations/payments-gateway (lowercase, digits, dashes)
    codename = "OP IRON VAULT",
    title = "Payments Gateway",
    status = OpStatus.DEPLOYED,           // DEPLOYED, IN_PROGRESS or RECON
    summary = "One-sentence summary, also used as the page's meta description.",
    briefing = listOf("Paragraph one.", "Paragraph two."),
    objectives = listOf("What it set out to do", "…"),
    stack = listOf("Kotlin", "Spring Boot", "PostgreSQL"),
    repoUrl = "https://github.com/Tuma2/payments-gateway",   // or null → shows "SOURCE // CLASSIFIED"
    liveUrl = null,
    screenshot = Screenshot("/img/projects/payments.webp", "Dashboard showing settled transactions"),
),
```

That one entry produces:
- the card in section 05
- a pre-rendered detail page at `/operations/<slug>`
- a sitemap entry
- a social preview image, generated at deploy time

Put screenshots in `site/src/jsMain/resources/public/img/projects/`. Alt text is required, and the tests fail without it.

### Add a Field Intel note

Append to `Intel.fieldIntel`:

```kotlin
FieldNote(
    title = "What I'm looking into",
    note = "Two lines on why it's worth the time.",
    tags = listOf("kotlin", "jvm"),
    logged = "2026-11",                    // ISO year-month
    url = "https://example.com/source",    // optional, must be https
),
```

Skills, experience, certifications and contact links work the same way (`loadout`, `serviceRecord`,
`commendations`, `comms`). Proficiency values run from 0 to 100.

---

## Project layout

```
site/
  build.gradle.kts              Kobweb config: <head>, CSP, export routes, sitemap generation
  src/head/boot.js              inline head script: boot-intro gate + post-paint app loader
  src/jsMain/kotlin/fyi/lehlanyaverse/
    data/                       Intel.kt (content) + Model.kt (types)
    components/hud/             HUD primitives: Panel, Stamp, Meter, StatusTag, Redacted…
    components/layout/          PageLayout, header + mission clock, ticker, footer, Seo
    components/sections/        the seven dossier sections + operation debrief + 404
    pages/                      routes: /, /operations/{slug}, /404
  src/jsMain/resources/public/
    css/hud.css                 the whole design system (tokens at the top)
    fonts/                      self-hosted Google Fonts (OFL licences included)
    img/, og/, favicon.svg, CNAME, robots.txt, site.webmanifest
  src/jsTest/                   IntelTest: data-integrity checks
tools/og/                       Node script that renders Open Graph images from Intel.kt
.github/workflows/deploy.yml    test → OG images → export → Lighthouse → GitHub Pages
```

The colours, type scale and motion timings are CSS custom properties at the top of `hud.css`
(`--void`, `--gunmetal`, `--olive`, `--bone`, `--orange`, `--acid`, `--red`, `--tape`).
The site is dark only by design.

---

## Static export (what gets deployed)

```bash
./gradlew :site:kobwebExport -PkobwebReuseServer=false -PkobwebEnv=DEV \
  -PkobwebRunLayout=FULLSTACK -PkobwebBuildTarget=RELEASE -PkobwebExportLayout=STATIC
# output: site/.kobweb/site
```

`FULLSTACK` is the *dev server* layout used while snapshotting. It's needed so dynamic routes
(`/operations/{slug}`) resolve during export. The output itself is still fully static.

Regenerate social preview images locally. CI does this on every deploy, so it's optional here.

```bash
npm --prefix tools/og ci
npx --prefix tools/og playwright-core install chromium
npm --prefix tools/og run og
```

---

## Security

- **Content-Security-Policy** (meta tag): `script-src 'self'` plus the SHA-256 hash of the one inline head script.
  No `unsafe-eval`, and no inline scripts other than that one. The dev build uses source maps instead of `eval`
  so the same policy holds everywhere.
- No third-party scripts, fonts, analytics or CDNs. `form-action 'none'`, `object-src 'none'`, `base-uri 'self'`.
- External links use `rel="noopener noreferrer"`, with a `strict-origin-when-cross-origin` referrer policy.
- Source maps are not shipped.
- CI actions are pinned to commit SHAs with least-privilege permissions, and the Gradle wrapper checksum is verified.
  Dependabot covers Actions, Gradle and npm.
- GitHub Pages can't send custom headers, so `frame-ancestors` and HSTS are out of reach there.
  Enforce HTTPS in the Pages settings.

---

## Deploy

Pushing to `main` runs `.github/workflows/deploy.yml`: tests, OG images, static export, Lighthouse
(it fails if performance, accessibility, best practices or SEO drop below 90), then deploy to GitHub Pages.

**One-time setup**

1. Repo **Settings → Pages → Source: GitHub Actions**.
   *Private repos need GitHub Pro or above for Pages.* Without that, see *Cloudflare Pages* below.
2. **Settings → Pages → Custom domain:** `lehlanyaverse.fyi` (also shipped as `CNAME` in the build).
   Tick **Enforce HTTPS** once the certificate is issued.
3. **Your profile → Settings → Pages → Verified domains:** add `lehlanyaverse.fyi` and create the TXT record
   it shows. This stops anyone else claiming the domain.

**DNS records at your registrar**

| Type  | Host                            | Value                 |
|-------|---------------------------------|-----------------------|
| A     | `@`                             | `185.199.108.153`     |
| A     | `@`                             | `185.199.109.153`     |
| A     | `@`                             | `185.199.110.153`     |
| A     | `@`                             | `185.199.111.153`     |
| AAAA  | `@`                             | `2606:50c0:8000::153` |
| AAAA  | `@`                             | `2606:50c0:8001::153` |
| AAAA  | `@`                             | `2606:50c0:8002::153` |
| AAAA  | `@`                             | `2606:50c0:8003::153` |
| CNAME | `www`                           | `tuma2.github.io.`    |
| TXT   | `_github-pages-challenge-Tuma2` | *(value from step 3)* |

Remove any parking-page A records or URL-forwarding on `@` first. DNS can take up to 24 hours to propagate,
and the HTTPS certificate follows shortly after. To check: `nslookup lehlanyaverse.fyi` should list the four
`185.199.x.153` addresses.

**Cloudflare Pages (free, works with private repos)**

Replace the `deploy` job with a `cloudflare/wrangler-action` step running
`pages deploy site/.kobweb/site --project-name=lehlanyaverse`, using `CLOUDFLARE_API_TOKEN` and
`CLOUDFLARE_ACCOUNT_ID` repository secrets. In the Cloudflare dashboard, add `lehlanyaverse.fyi` as a
custom domain on the Pages project; with Cloudflare DNS this creates the records for you. A
`public/_headers` file can then set real security headers (HSTS, `frame-ancestors`).
