/*
 * Inlined into the document head by site/build.gradle.kts (the CSP allows it by hash).
 * NOTE: Kobweb parses head content as XML, so this file must not contain ampersands or angle brackets.
 */
(function () {
  var exporting = /_kobwebIsExporting/.test(location.search);

  /*
   * 1. Boot intro gate. The overlay itself is pure CSS (html[data-boot] in hud.css); this decides whether
   *    to show it: first visit only, never under prefers-reduced-motion, never during the export snapshot.
   */
  function bootGate() {
    try {
      var root = document.documentElement;
      if (exporting) return;
      if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;
      if (window.localStorage.getItem("lv.booted")) return;
      window.localStorage.setItem("lv.booted", "1");

      root.setAttribute("data-boot", "");
      // The ID card's photo stack drops in as the overlay clears (hud.css). Dropped again before the app
      // re-mounts the DOM, so the animation never replays on the new nodes.
      root.setAttribute("data-drop", "");
      var end = function () {
        root.removeAttribute("data-boot");
        window.removeEventListener("keydown", end);
        window.removeEventListener("pointerdown", end);
        window.setTimeout(function () { root.removeAttribute("data-drop"); }, 600);
      };
      window.addEventListener("keydown", end);
      window.addEventListener("pointerdown", end);
      window.setTimeout(end, 1800);
    } catch (e) {
      // Storage blocked (private mode etc.): skip the intro rather than show it on every visit.
    }
  }

  /*
   * 2. App loader. Every page ships pre-rendered and fully readable, so the Kotlin app bundle (emitted by
   *    Kobweb as an inert text/x-kobweb-app script) only starts on the visitor's first interaction, or after
   *    a few seconds if there is none. Kobweb re-mounts the DOM when it starts; doing that before anyone
   *    interacts would cost a second largest-contentful-paint and main-thread time while the page is being read.
   *    During export it starts immediately, so the snapshot waits for the app to render.
   */
  function startApp() {
    var tag = document.querySelector("script[type='text/x-kobweb-app']");
    if (!tag) return;
    var app = document.createElement("script");
    app.src = tag.getAttribute("src");
    app.async = false;
    // Drop the tag once it has run, so an export snapshot never bakes in a second, render-blocking copy
    app.onload = function () { app.remove(); };
    document.body.appendChild(app);
  }

  /*
   * 3. Photo stack. Hide the "tap to flip" hint before first paint for visitors who have already flipped.
   *    A tap on the stack before the app is running lands on inert pre-rendered DOM, so it is recorded here
   *    and the stack replays it once mounted (PolaroidStack in Polaroid.kt).
   */
  function photoState() {
    try {
      if (exporting) return;
      if (window.localStorage.getItem("lv.flipped")) document.documentElement.setAttribute("data-flipped", "");
    } catch (e) {
      // Storage blocked: the hint stays visible
    }
  }

  // Listens from page load until the app is running (PageLayout adds html.js-ready), not just for the first
  // interaction: scrolling down to the card usually starts the app, and the tap lands while it is still loading.
  var flipEvents = ["pointerdown", "keydown"];
  function notePendingFlip(e) {
    if (document.documentElement.classList.contains("js-ready")) {
      flipEvents.forEach(function (t) { window.removeEventListener(t, notePendingFlip, true); });
      return;
    }
    var target = e.type === "keydown" ? document.activeElement : e.target;
    if (!target || !target.closest || !target.closest(".pstack__btn")) return;
    if (e.type === "keydown") {
      if (e.key === "Enter" || e.key === " ") window.lvPendingFlip = "key";
    } else {
      window.lvPendingFlip = "pointer";
    }
  }

  var started = false;
  var triggers = ["pointerdown", "keydown", "wheel", "touchstart", "scroll"];
  function startOnce() {
    if (started) return;
    started = true;
    triggers.forEach(function (t) { window.removeEventListener(t, startOnce, true); });
    startApp();
  }

  bootGate();
  photoState();
  document.addEventListener("DOMContentLoaded", function () {
    if (exporting) return startApp();
    // Registered before the start triggers, so a tap that also starts the app is recorded too
    flipEvents.forEach(function (t) { window.addEventListener(t, notePendingFlip, { capture: true, passive: true }); });
    triggers.forEach(function (t) { window.addEventListener(t, startOnce, { capture: true, passive: true }); });
    window.setTimeout(startOnce, 5000);
  });
})();
