const STORAGE_KEY = "social-tasker-state-v1";
const CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar.events";
const CALENDAR_EVENTS_URL = "https://www.googleapis.com/calendar/v3/calendars/primary/events";

const state = loadState();
let activeFilter = "all";
let googleClientId = "";
let calendarAccessToken = "";
let tokenClient = null;

const elements = {
  todayLabel: document.querySelector("#todayLabel"),
  syncStatus: document.querySelector("#syncStatus"),
  focusTitle: document.querySelector("#focusTitle"),
  focusSubtitle: document.querySelector("#focusSubtitle"),
  progressValue: document.querySelector("#progressValue"),
  taskCount: document.querySelector("#taskCount"),
  taskMeta: document.querySelector("#taskMeta"),
  unreadCount: document.querySelector("#unreadCount"),
  messageMeta: document.querySelector("#messageMeta"),
  taskForm: document.querySelector("#taskForm"),
  taskList: document.querySelector("#taskList"),
  messageForm: document.querySelector("#messageForm"),
  messageList: document.querySelector("#messageList"),
  suggestionList: document.querySelector("#suggestionList"),
  clearDoneButton: document.querySelector("#clearDoneButton"),
  markReadButton: document.querySelector("#markReadButton"),
  seedMessagesButton: document.querySelector("#seedMessagesButton"),
  clearMessagesButton: document.querySelector("#clearMessagesButton"),
  refreshSuggestionsButton: document.querySelector("#refreshSuggestionsButton"),
  calendarStatus: document.querySelector("#calendarStatus"),
  connectCalendarButton: document.querySelector("#connectCalendarButton"),
  reminderMinutes: document.querySelector("#reminderMinutes"),
  calendarTaskList: document.querySelector("#calendarTaskList")
};

elements.todayLabel.textContent = new Intl.DateTimeFormat(undefined, {
  weekday: "long",
  month: "short",
  day: "numeric"
}).format(new Date()).toUpperCase();

elements.taskForm.addEventListener("submit", (event) => {
  event.preventDefault();
  const form = new FormData(elements.taskForm);
  state.tasks.unshift({
    id: crypto.randomUUID(),
    title: form.get("title").trim(),
    category: form.get("category"),
    priority: form.get("priority"),
    due: form.get("due"),
    minutes: Number(form.get("minutes") || 25),
    completed: false,
    createdAt: Date.now()
  });
  elements.taskForm.reset();
  document.querySelector("#taskMinutes").value = 25;
  persistAndRender();
});

elements.messageForm.addEventListener("submit", (event) => {
  event.preventDefault();
  const form = new FormData(elements.messageForm);
  state.messages.unshift({
    id: crypto.randomUUID(),
    sender: form.get("sender").trim(),
    platform: form.get("platform"),
    text: form.get("text").trim(),
    read: false,
    timestamp: Date.now()
  });
  elements.messageForm.reset();
  persistAndRender();
});

document.querySelectorAll("[data-filter]").forEach((button) => {
  button.addEventListener("click", () => {
    activeFilter = button.dataset.filter;
    document.querySelectorAll("[data-filter]").forEach((item) => item.classList.toggle("active", item === button));
    render();
  });
});

elements.clearDoneButton.addEventListener("click", () => {
  state.tasks = state.tasks.filter((task) => !task.completed);
  persistAndRender();
});

elements.markReadButton.addEventListener("click", () => {
  state.messages = state.messages.map((message) => ({ ...message, read: true }));
  persistAndRender();
});

elements.clearMessagesButton.addEventListener("click", () => {
  state.messages = [];
  persistAndRender();
});

elements.seedMessagesButton.addEventListener("click", () => {
  state.messages.unshift(
    {
      id: crypto.randomUUID(),
      sender: "Maya",
      platform: "Instagram",
      text: "Can you approve the campaign captions before 5?",
      read: false,
      timestamp: Date.now()
    },
    {
      id: crypto.randomUUID(),
      sender: "Rohan",
      platform: "LinkedIn",
      text: "Please review the client brief and send notes today.",
      read: false,
      timestamp: Date.now() - 780000
    }
  );
  persistAndRender();
});

elements.refreshSuggestionsButton.addEventListener("click", renderSuggestions);
elements.connectCalendarButton.addEventListener("click", connectGoogleCalendar);

function loadState() {
  try {
    const stored = JSON.parse(localStorage.getItem(STORAGE_KEY) || "null");
    if (stored && Array.isArray(stored.tasks) && Array.isArray(stored.messages)) return stored;
  } catch {
    localStorage.removeItem(STORAGE_KEY);
  }

  return {
    tasks: [
      {
        id: crypto.randomUUID(),
        title: "Review creator outreach notes",
        category: "Work",
        priority: "High",
        due: new Date().toISOString().slice(0, 10),
        minutes: 30,
        completed: false,
        createdAt: Date.now()
      },
      {
        id: crypto.randomUUID(),
        title: "Organize saved social requests",
        category: "Social",
        priority: "Medium",
        due: "",
        minutes: 20,
        completed: false,
        createdAt: Date.now() - 300000
      }
    ],
    messages: [
      {
        id: crypto.randomUUID(),
        sender: "Anika",
        platform: "Snapchat",
        text: "Streak live, reply today when you get a minute.",
        read: false,
        timestamp: Date.now() - 900000
      }
    ]
  };
}

function persistAndRender() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  render();
}

function render() {
  renderSummary();
  renderTasks();
  renderMessages();
  renderCalendarTasks();
  renderSuggestions();
}

function renderSummary() {
  const completed = state.tasks.filter((task) => task.completed).length;
  const total = state.tasks.length;
  const percent = total ? Math.round((completed / total) * 100) : 0;
  const high = state.tasks.filter((task) => !task.completed && task.priority === "High").length;
  const unread = state.messages.filter((message) => !message.read).length;

  elements.progressValue.textContent = `${percent}%`;
  document.documentElement.style.setProperty("--progress", `${percent * 3.6}deg`);
  elements.taskCount.textContent = String(total);
  elements.taskMeta.textContent = `${high} high priority`;
  elements.unreadCount.textContent = String(unread);
  elements.messageMeta.textContent = `${state.messages.length} social alerts`;
  elements.focusTitle.textContent = total ? `${completed} of ${total} tasks complete` : "Build your plan";
  elements.focusSubtitle.textContent = unread
    ? `${unread} unread alert${unread === 1 ? "" : "s"} can become trackable work.`
    : "No unread alerts. Keep the task list honest and finish the next focus block.";
}

function renderTasks() {
  const tasks = state.tasks.filter((task) => {
    if (activeFilter === "open") return !task.completed;
    if (activeFilter === "high") return task.priority === "High" && !task.completed;
    if (activeFilter === "done") return task.completed;
    return true;
  });

  if (!tasks.length) {
    elements.taskList.innerHTML = `<div class="empty-state">No tasks in this view.</div>`;
    return;
  }

  elements.taskList.innerHTML = tasks.map((task) => `
    <article class="task-card ${task.completed ? "done" : ""}">
      <button class="check-button ${task.completed ? "done" : ""}" data-action="toggle" data-id="${task.id}" aria-label="Toggle ${escapeHtml(task.title)}">${task.completed ? "OK" : ""}</button>
      <div>
        <div class="task-title">${escapeHtml(task.title)}</div>
        <div class="meta-row">
          <span class="tag">${escapeHtml(task.category)}</span>
          <span class="tag ${task.priority.toLowerCase()}">${escapeHtml(task.priority)}</span>
          <span class="tag">${task.minutes || 25} min</span>
          ${task.due ? `<span class="tag">Due ${escapeHtml(task.due)}</span>` : ""}
        </div>
      </div>
      <button class="delete-button" data-action="delete" data-id="${task.id}" aria-label="Delete ${escapeHtml(task.title)}">x</button>
    </article>
  `).join("");

  elements.taskList.querySelectorAll("[data-action]").forEach((button) => {
    button.addEventListener("click", () => {
      if (button.dataset.action === "toggle") {
        state.tasks = state.tasks.map((task) => task.id === button.dataset.id ? { ...task, completed: !task.completed } : task);
      }
      if (button.dataset.action === "delete") {
        state.tasks = state.tasks.filter((task) => task.id !== button.dataset.id);
      }
      persistAndRender();
    });
  });
}

function renderMessages() {
  if (!state.messages.length) {
    elements.messageList.innerHTML = `<div class="empty-state">No captured social alerts.</div>`;
    return;
  }

  elements.messageList.innerHTML = state.messages.map((message) => `
    <article class="message-card ${message.read ? "" : "unread"}">
      <div class="message-topline">
        <span class="message-sender">${escapeHtml(message.sender)}</span>
        <span class="tag">${escapeHtml(message.platform)}</span>
      </div>
      <p>${escapeHtml(message.text)}</p>
      <button class="convert-button" data-id="${message.id}">Convert to task</button>
    </article>
  `).join("");

  elements.messageList.querySelectorAll(".convert-button").forEach((button) => {
    button.addEventListener("click", () => {
      const message = state.messages.find((item) => item.id === button.dataset.id);
      if (!message) return;
      const task = taskFromMessage(message);
      state.tasks.unshift({ ...task, id: crypto.randomUUID(), completed: false, due: "", createdAt: Date.now() });
      state.messages = state.messages.map((item) => item.id === message.id ? { ...item, read: true } : item);
      persistAndRender();
    });
  });
}

function renderCalendarTasks() {
  const openTasks = state.tasks.filter((task) => !task.completed).slice(0, 5);
  if (!openTasks.length) {
    elements.calendarTaskList.innerHTML = `<div class="empty-state">Open tasks will appear here for calendar reminders.</div>`;
    return;
  }

  elements.calendarTaskList.innerHTML = openTasks.map((task) => `
    <article class="calendar-task-card">
      <div>
        <strong>${escapeHtml(task.title)}</strong>
        <span>${escapeHtml(task.due || "Next available hour")} - ${task.minutes || 25} min</span>
      </div>
      <button class="calendar-button" data-calendar-task="${task.id}" type="button">Add reminder</button>
    </article>
  `).join("");

  elements.calendarTaskList.querySelectorAll("[data-calendar-task]").forEach((button) => {
    button.addEventListener("click", () => addTaskToCalendar(button.dataset.calendarTask));
  });
}

async function renderSuggestions() {
  const fallback = buildSuggestions(state.tasks, state.messages);
  elements.suggestionList.innerHTML = `<div class="empty-state">Refreshing suggestions...</div>`;

  try {
    const response = await fetch("/api/suggestions", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ tasks: state.tasks, messages: state.messages })
    });
    if (!response.ok) throw new Error("Suggestion API failed");
    renderSuggestionCards(await response.json(), true);
  } catch {
    renderSuggestionCards(fallback, false);
  }
}

function renderSuggestionCards(result, apiBacked) {
  elements.syncStatus.innerHTML = `<span class="status-dot"></span>${apiBacked ? "API assisted" : "Local workspace"}`;
  elements.suggestionList.innerHTML = result.suggestions.map((item) => `
    <article class="suggestion-card">
      <strong>${escapeHtml(item.title)}</strong>
      <p>${escapeHtml(item.detail)}</p>
    </article>
  `).join("");
}

async function loadCalendarConfig() {
  try {
    const response = await fetch("/api/google-config");
    if (!response.ok) throw new Error("Google config unavailable");
    const config = await response.json();
    googleClientId = config.clientId || "";
    elements.connectCalendarButton.disabled = !config.configured;
    elements.calendarStatus.textContent = config.configured
      ? "Google Calendar is ready to connect."
      : "Add GOOGLE_CLIENT_ID in Vercel to enable calendar connection.";
  } catch {
    elements.connectCalendarButton.disabled = true;
    elements.calendarStatus.textContent = "Calendar setup is available after deploying with GOOGLE_CLIENT_ID.";
  }
}

async function connectGoogleCalendar() {
  if (!googleClientId) {
    elements.calendarStatus.textContent = "Calendar is not configured yet. Set GOOGLE_CLIENT_ID first.";
    return;
  }

  try {
    await waitForGoogleIdentity();
    tokenClient = tokenClient || google.accounts.oauth2.initTokenClient({
      client_id: googleClientId,
      scope: CALENDAR_SCOPE,
      callback: (tokenResponse) => {
        if (tokenResponse.error) {
          elements.calendarStatus.textContent = "Google Calendar permission was not granted.";
          return;
        }
        calendarAccessToken = tokenResponse.access_token;
        elements.calendarStatus.textContent = "Connected. Choose a task to add a calendar reminder.";
      }
    });
    tokenClient.requestAccessToken({ prompt: calendarAccessToken ? "" : "consent" });
  } catch {
    elements.calendarStatus.textContent = "Could not load Google Calendar sign-in. Try again in a moment.";
  }
}

async function addTaskToCalendar(taskId) {
  const task = state.tasks.find((item) => item.id === taskId);
  if (!task) return;
  if (!calendarAccessToken) {
    elements.calendarStatus.textContent = "Connect Google Calendar before adding reminders.";
    await connectGoogleCalendar();
    return;
  }

  const event = buildCalendarEvent(task, Number(elements.reminderMinutes.value || 30));
  elements.calendarStatus.textContent = "Adding reminder to Google Calendar...";

  try {
    const response = await fetch(CALENDAR_EVENTS_URL, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${calendarAccessToken}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify(event)
    });

    if (response.status === 401) {
      calendarAccessToken = "";
      elements.calendarStatus.textContent = "Calendar session expired. Connect again.";
      return;
    }

    if (!response.ok) throw new Error("Calendar insert failed");
    const created = await response.json();
    elements.calendarStatus.innerHTML = `Reminder added. <a href="${escapeHtml(created.htmlLink)}" target="_blank" rel="noreferrer">Open event</a>`;
  } catch {
    elements.calendarStatus.textContent = "Could not add reminder. Check Google Calendar API access and try again.";
  }
}

function buildCalendarEvent(task, reminderMinutes) {
  const start = getTaskStart(task);
  const end = new Date(start.getTime() + Math.max(Number(task.minutes || 25), 5) * 60 * 1000);
  const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone || "UTC";

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

function getTaskStart(task) {
  const start = task.due ? new Date(`${task.due}T09:00:00`) : new Date(Date.now() + 60 * 60 * 1000);
  if (Number.isNaN(start.getTime())) return new Date(Date.now() + 60 * 60 * 1000);
  return start;
}

function waitForGoogleIdentity() {
  return new Promise((resolve, reject) => {
    let attempts = 0;
    const timer = window.setInterval(() => {
      attempts += 1;
      if (window.google && google.accounts && google.accounts.oauth2) {
        window.clearInterval(timer);
        resolve();
      }
      if (attempts > 40) {
        window.clearInterval(timer);
        reject(new Error("Google Identity unavailable"));
      }
    }, 125);
  });
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
  return {
    title: `Reply to ${message.sender}: ${message.text}`.slice(0, 96),
    category: categoryFromText(message.text),
    priority: keywordPriority(message.text),
    sourceMessageId: message.id,
    minutes: keywordPriority(message.text) === "High" ? 15 : 25
  };
}

function buildSuggestions(tasks, messages) {
  const openTasks = tasks.filter((task) => !task.completed);
  const unreadMessages = messages.filter((message) => !message.read);
  const sortedTasks = [...openTasks].sort((a, b) => scoreTask(b) - scoreTask(a));
  const suggestions = [];

  if (sortedTasks[0]) {
    suggestions.push({
      title: `Start with: ${sortedTasks[0].title}`,
      detail: `${sortedTasks[0].priority || "Medium"} priority, ${sortedTasks[0].minutes || 25} minute focus block.`
    });
  }
  if (unreadMessages.length) {
    suggestions.push({
      title: `Convert ${unreadMessages.length} unread alert${unreadMessages.length === 1 ? "" : "s"}`,
      detail: "Turn message requests into tasks so they stop living only in chat."
    });
  }
  if (!suggestions.length) {
    suggestions.push({ title: "Your plan is clear", detail: "Capture new social requests as they arrive, then refresh this panel." });
  }
  return { suggestions, convertedMessages: unreadMessages.slice(0, 3).map(taskFromMessage) };
}

function scoreTask(task) {
  const priority = task.priority === "High" ? 3 : task.priority === "Low" ? 1 : 2;
  return priority * 10 + (task.due ? 2 : 0) + (task.category === "Social" ? 1 : 0) + (task.completed ? -100 : 0);
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

render();
loadCalendarConfig();
