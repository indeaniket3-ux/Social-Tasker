function getTaskStart(task, now = new Date()) {
  const source = task.due ? new Date(`${task.due}T09:00:00`) : new Date(now.getTime() + 60 * 60 * 1000);
  if (Number.isNaN(source.getTime())) return new Date(now.getTime() + 60 * 60 * 1000);
  return source;
}

function buildCalendarEvent(task, reminderMinutes = 30, timeZone = "UTC", now = new Date()) {
  const start = getTaskStart(task, now);
  const duration = Number(task.minutes || 25);
  const end = new Date(start.getTime() + Math.max(duration, 5) * 60 * 1000);

  return {
    summary: task.title,
    description: `Created from Social Tasker. Category: ${task.category || "Task"}. Priority: ${task.priority || "Medium"}.`,
    start: {
      dateTime: start.toISOString(),
      timeZone
    },
    end: {
      dateTime: end.toISOString(),
      timeZone
    },
    reminders: {
      useDefault: false,
      overrides: [
        {
          method: "popup",
          minutes: Number(reminderMinutes || 30)
        }
      ]
    }
  };
}

module.exports = {
  buildCalendarEvent,
  getTaskStart
};
