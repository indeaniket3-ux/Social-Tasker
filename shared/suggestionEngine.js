function normalizePriority(priority) {
  const value = String(priority || "Medium").toLowerCase();
  if (value === "high") return 3;
  if (value === "low") return 1;
  return 2;
}

function keywordPriority(text) {
  const value = String(text || "").toLowerCase();
  if (/(urgent|asap|today|before|deadline|streak|blocked|final|approve)/.test(value)) return "High";
  if (/(review|reply|send|schedule|call|invoice|brief|caption|draft)/.test(value)) return "Medium";
  return "Low";
}

function categoryFromText(text) {
  const value = String(text || "").toLowerCase();
  if (/(invoice|payment|budget|receipt)/.test(value)) return "Finance";
  if (/(brief|client|review|campaign|caption|meeting|deck|report)/.test(value)) return "Work";
  if (/(coffee|dinner|birthday|plan|friend)/.test(value)) return "Personal";
  return "Social";
}

function taskFromMessage(message) {
  const sender = message.sender || "Someone";
  const text = message.text || "new message";
  return {
    title: `Reply to ${sender}: ${text}`.slice(0, 96),
    category: categoryFromText(text),
    priority: keywordPriority(text),
    sourceMessageId: message.id || null,
    minutes: keywordPriority(text) === "High" ? 15 : 25
  };
}

function scoreTask(task) {
  const completePenalty = task.completed ? -100 : 0;
  const dueBoost = task.due ? 2 : 0;
  const socialBoost = task.category === "Social" ? 1 : 0;
  return normalizePriority(task.priority) * 10 + dueBoost + socialBoost + completePenalty;
}

function buildSuggestions(tasks = [], messages = []) {
  const openTasks = tasks.filter((task) => !task.completed);
  const unreadMessages = messages.filter((message) => !message.read);
  const sortedTasks = [...openTasks].sort((a, b) => scoreTask(b) - scoreTask(a));
  const convertedMessages = unreadMessages.slice(0, 3).map(taskFromMessage);
  const suggestions = [];

  if (sortedTasks[0]) {
    suggestions.push({
      type: "focus",
      title: `Start with: ${sortedTasks[0].title}`,
      detail: `${sortedTasks[0].priority || "Medium"} priority, ${sortedTasks[0].minutes || 25} minute focus block.`
    });
  }

  if (unreadMessages.length) {
    suggestions.push({
      type: "inbox",
      title: `Convert ${unreadMessages.length} unread alert${unreadMessages.length === 1 ? "" : "s"}`,
      detail: "Turn message requests into tasks so they stop living only in chat."
    });
  }

  const highCount = openTasks.filter((task) => task.priority === "High").length;
  if (highCount > 2) {
    suggestions.push({
      type: "load",
      title: "Trim the high-priority queue",
      detail: "More than two high-priority tasks usually means the day needs sequencing."
    });
  }

  if (!suggestions.length) {
    suggestions.push({
      type: "ready",
      title: "Your plan is clear",
      detail: "Capture new social requests as they arrive, then refresh this panel."
    });
  }

  return {
    suggestions,
    convertedMessages,
    stats: {
      openTasks: openTasks.length,
      unreadMessages: unreadMessages.length,
      highPriorityTasks: highCount
    }
  };
}

module.exports = {
  buildSuggestions,
  categoryFromText,
  keywordPriority,
  taskFromMessage
};
