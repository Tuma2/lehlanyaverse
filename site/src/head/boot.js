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
      var end = function () {
        root.removeAttribute("data-boot");
        window.removeEventListener("keydown", end);
        window.removeEventListener("pointerdown", end);
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

  var started = false;
  var triggers = ["pointerdown", "keydown", "wheel", "touchstart", "scroll"];
  function startOnce() {
    if (started) return;
    started = true;
    triggers.forEach(function (t) { window.removeEventListener(t, startOnce, true); });
    startApp();
  }

  bootGate();
  document.addEventListener("DOMContentLoaded", function () {
    if (exporting) return startApp();
    triggers.forEach(function (t) { window.addEventListener(t, startOnce, { capture: true, passive: true }); });
    window.setTimeout(startOnce, 5000);
  });
})();
