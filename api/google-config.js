module.exports = function handler(request, response) {
  if (request.method !== "GET") {
    response.setHeader("Allow", "GET");
    response.status(405).json({ error: "Method not allowed" });
    return;
  }

  const clientId = process.env.GOOGLE_CLIENT_ID || "";

  response.status(200).json({
    configured: Boolean(clientId),
    clientId
  });
};
