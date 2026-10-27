// Renders the site's Open Graph images (1200x630 PNG) in the HUD style:
//   og/og-default.png    - home / fallback
//   og/<slug>.png        - one per Operation in Intel.kt
//   apple-touch-icon.png - 180x180 home-screen icon
//
// Usage:  npm --prefix tools/og ci && npm --prefix tools/og run og
// Browser: uses CHROME_PATH if set, otherwise Playwright's own Chromium (`npx playwright install chromium`).
import { chromium } from "playwright-core";
import { readFileSync, mkdirSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const root = resolve(dirname(fileURLToPath(import.meta.url)), "../..");
const pub = join(root, "site/src/jsMain/resources/public");
const intel = readFileSync(join(root, "site/src/jsMain/kotlin/fyi/lehlanyaverse/data/Intel.kt"), "utf8");

// Light-touch parsing of Intel.kt: pull the fields each card needs from every Operation(...) block
const str = (block, key) => block.match(new RegExp(`${key}\\s*=\\s*"([^"]*)"`))?.[1];
const operations = intel.split(/\bOperation\(/).slice(1).map((block) => ({
  slug: str(block, "slug"),
  codename: str(block, "codename"),
  title: str(block, "title"),
  status: block.match(/status\s*=\s*OpStatus\.(\w+)/)?.[1]?.replace("_", " ") ?? "",
})).filter((op) => op.slug);
const name = str(intel, "name") ?? "Sebabatso Moleele";
const role = str(intel, "role") ?? "Software Engineer";
const specialty = str(intel, "specialty") ?? "Java Backend";

const esc = (s) => s.replace(/[&<>"]/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" })[c]);
// Inlined as data URIs: pages built with setContent() cannot load file:// resources
const dataUri = (file, type) => `data:${type};base64,${readFileSync(file).toString("base64")}`;
const font = (f) => dataUri(join(pub, "fonts", f), "font/woff2");
const img = (f) => dataUri(join(pub, "img", f), "image/svg+xml");

const card = ({ kicker, title, sub, tag, tagColor }) => `<!doctype html><html><head><meta charset="utf-8"><style>
@font-face{font-family:D;src:url(${font("big-shoulders-display.woff2")});font-weight:100 900}
@font-face{font-family:S;src:url(${font("big-shoulders-stencil.woff2")});font-weight:800}
@font-face{font-family:M;src:url(${font("jetbrains-mono.woff2")});font-weight:100 800}
*{margin:0;box-sizing:border-box}
body{width:1200px;height:630px;overflow:hidden;background:#07090b;color:#d6dee4;font-family:M;position:relative}
.topo{position:absolute;inset:0;background:url(${img("topo.svg")}) center/cover;opacity:.16}
.grid{position:absolute;inset:0;background-image:linear-gradient(rgba(163,184,200,.12) 1px,transparent 1px),linear-gradient(90deg,rgba(163,184,200,.12) 1px,transparent 1px);background-size:56px 56px}
.scan{position:absolute;inset:0;background:repeating-linear-gradient(to bottom,transparent 0 2px,rgba(0,0,0,.3) 2px 3px)}
.frame{position:absolute;inset:40px;border:1px solid rgba(214,222,228,.18)}
.frame:before{content:"";position:absolute;inset:-8px;background:
 linear-gradient(#a3b8c8,#a3b8c8) top left/48px 5px,linear-gradient(#a3b8c8,#a3b8c8) top left/5px 48px,
 linear-gradient(#a3b8c8,#a3b8c8) top right/48px 5px,linear-gradient(#a3b8c8,#a3b8c8) top right/5px 48px,
 linear-gradient(#a3b8c8,#a3b8c8) bottom left/48px 5px,linear-gradient(#a3b8c8,#a3b8c8) bottom left/5px 48px,
 linear-gradient(#a3b8c8,#a3b8c8) bottom right/48px 5px,linear-gradient(#a3b8c8,#a3b8c8) bottom right/5px 48px;background-repeat:no-repeat}
.bar{position:absolute;left:80px;right:80px;top:78px;display:flex;justify-content:space-between;font-size:20px;font-weight:700;letter-spacing:.18em;color:#8f9daa;border-bottom:1px dashed rgba(214,222,228,.3);padding-bottom:14px}
.bar b{color:#7cffb2}
.kicker{position:absolute;left:80px;top:160px;font-size:24px;font-weight:700;letter-spacing:.14em;color:#7cffb2}
h1{position:absolute;left:76px;right:330px;top:205px;font-family:D;font-weight:900;font-size:${title.length > 26 ? 96 : title.length > 16 ? 112 : 140}px;line-height:.86;text-transform:uppercase}
.sub{position:absolute;left:80px;bottom:84px;display:flex;gap:16px;align-items:center}
.chip{font-family:D;font-weight:900;font-size:34px;padding:8px 18px;background:#a3b8c8;color:#07090b;text-transform:uppercase}
.tag{font-size:22px;font-weight:800;letter-spacing:.14em;padding:10px 16px;border:3px solid ${tagColor};color:${tagColor}}
.reticle{position:absolute;right:90px;bottom:150px;width:200px;height:200px;background:url(${img("reticle.svg")}) center/contain no-repeat}
.stamp{position:absolute;right:84px;top:150px;font-family:S;font-weight:800;font-size:46px;letter-spacing:.1em;color:#7cffb2;border:6px solid #7cffb2;padding:4px 18px;transform:rotate(-7deg)}
.hazard{position:absolute;left:0;right:0;bottom:0;height:16px;background:repeating-linear-gradient(-45deg,#e2a43a 0 18px,#07090b 18px 36px)}
</style></head><body>
<div class="grid"></div><div class="topo"></div>
<div class="frame"></div>
<div class="bar"><span>LEHLANYAVERSE <b>//</b> DOSSIER</span><span>LEHLANYAVERSE.FYI</span></div>
<div class="kicker">&gt; ${esc(kicker)}</div>
<h1>${esc(title)}</h1>
<div class="reticle"></div>
<div class="sub"><span class="chip">${esc(sub)}</span>${tag ? `<span class="tag">${esc(tag)}</span>` : ""}</div>
<div class="stamp">DECLASSIFIED</div>
<div class="scan"></div><div class="hazard"></div>
</body></html>`;

const browser = await chromium.launch(process.env.CHROME_PATH ? { executablePath: process.env.CHROME_PATH } : {});
const page = await browser.newPage({ viewport: { width: 1200, height: 630 } });
mkdirSync(join(pub, "og"), { recursive: true });

async function render(html, file, size) {
  if (size) await page.setViewportSize(size);
  await page.setContent(html, { waitUntil: "load" });
  await page.evaluate(() => document.fonts.ready);
  await page.screenshot({ path: file });
}

await render(card({ kicker: "OPERATOR PROFILE", title: name, sub: `${role} // ${specialty}`, tag: "ACTIVE DUTY", tagColor: "#7cffb2" }),
  join(pub, "og/og-default.png"));
for (const op of operations) {
  const color = op.status === "DEPLOYED" ? "#7cffb2" : op.status === "IN PROGRESS" ? "#a3b8c8" : "#e2a43a";
  await render(card({ kicker: `OPERATION DEBRIEF // ${op.codename}`, title: op.title, sub: "Active operation", tag: op.status, tagColor: color }),
    join(pub, `og/${op.slug}.png`));
}
// Home-screen icon from the favicon artwork
await render(`<!doctype html><body style="margin:0;background:#07090b"><img src="${dataUri(join(pub, "favicon.svg"), "image/svg+xml")}" width="180" height="180">`,
  join(pub, "apple-touch-icon.png"), { width: 180, height: 180 });

await browser.close();
console.log(`Rendered og-default + ${operations.length} operation card(s) + apple-touch-icon`);
