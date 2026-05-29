const assert = require("node:assert/strict");
const { buildSuggestions, categoryFromText, keywordPriority, taskFromMessage } = require("../shared/suggestionEngine");
const { buildCalendarEvent } = require("../shared/calendarEvent");

assert.equal(keywordPriority("please approve before 5 today"), "High");
assert.equal(keywordPriority("review the draft caption"), "Medium");
assert.equal(categoryFromText("invoice payment receipt"), "Finance");
assert.equal(categoryFromText("client campaign brief"), "Work");

const converted = taskFromMessage({
  id: "m1",
  sender: "Maya",
  text: "Can you approve the campaign captions before 5?"
});

assert.equal(converted.priority, "High");
assert.equal(converted.category, "Work");
assert.match(converted.title, /Reply to Maya/);

const result = buildSuggestions(
  [
    { title: "Low item", priority: "Low", completed: false, category: "Personal", minutes: 10 },
    { title: "Urgent item", priority: "High", completed: false, category: "Work", minutes: 25 }
  ],
  [{ id: "m2", sender: "Rohan", text: "send notes today", read: false }]
);

assert.match(result.suggestions[0].title, /Urgent item/);
assert.equal(result.convertedMessages.length, 1);
assert.equal(result.stats.unreadMessages, 1);

const event = buildCalendarEvent(
  { title: "Review captions", category: "Work", priority: "High", due: "2026-05-30", minutes: 45 },
  10,
  "Asia/Calcutta",
  new Date("2026-05-29T08:00:00Z")
);

assert.equal(event.summary, "Review captions");
assert.equal(event.reminders.useDefault, false);
assert.equal(event.reminders.overrides[0].minutes, 10);
assert.equal(event.start.timeZone, "Asia/Calcutta");

console.log("suggestion engine ok");
