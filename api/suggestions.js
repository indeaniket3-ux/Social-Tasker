const { buildSuggestions } = require("../shared/suggestionEngine");

module.exports = function handler(request, response) {
  if (request.method !== "POST") {
    response.setHeader("Allow", "POST");
    response.status(405).json({ error: "Method not allowed" });
    return;
  }

  const body = typeof request.body === "string" ? JSON.parse(request.body || "{}") : request.body || {};
  const result = buildSuggestions(body.tasks || [], body.messages || []);

  response.status(200).json(result);
};
