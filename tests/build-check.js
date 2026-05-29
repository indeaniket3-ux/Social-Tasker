const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");

const root = path.join(__dirname, "..");
const requiredFiles = [
  "index.html",
  "styles.css",
  "assets/app.js",
  "assets/social-tasker-preview.svg",
  "api/google-config.js",
  "api/suggestions.js",
  "shared/calendarEvent.js",
  "shared/suggestionEngine.js"
];

for (const file of requiredFiles) {
  assert.equal(fs.existsSync(path.join(root, file)), true, `${file} should exist`);
}

const html = fs.readFileSync(path.join(root, "index.html"), "utf8");
assert.match(html, /Social Tasker/);
assert.match(html, /assets\/app\.js/);
assert.match(html, /accounts\.google\.com\/gsi\/client/);

console.log("static build check ok");
