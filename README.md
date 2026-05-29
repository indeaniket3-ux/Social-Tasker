# Social Tasker

Social Tasker turns social messages into prioritized daily work. The uploaded project started as an Android app, and this repository now also includes a Vercel-ready web app at the project root.

## What is included

- Static frontend dashboard in `index.html`, `styles.css`, and `assets/app.js`
- Serverless suggestion API in `api/suggestions.js`
- Google Calendar config endpoint in `api/google-config.js`
- Shared prioritization logic in `shared/suggestionEngine.js`
- Tests and deployment checks in `tests/`
- Original Android source in `app/`

## Run the website locally

```bash
npm test
npm run build
npx vercel dev
```

The frontend also works by opening `index.html`, but the suggestion API is available when running through Vercel.

## Deploy

```bash
vercel --prod
```

## Google Calendar reminders

The website can connect a user's Google Calendar and create reminder events from open tasks.

Google setup:

1. Create or open a Google Cloud project.
2. Enable the Google Calendar API.
3. Configure the OAuth consent screen.
4. Create an OAuth 2.0 Client ID for a web application.
5. Add the production origin to Authorized JavaScript origins:

```text
https://social-tasker-two.vercel.app
```

6. Add the client ID to Vercel:

```bash
vercel env add GOOGLE_CLIENT_ID production
vercel --prod
```

The app requests only `https://www.googleapis.com/auth/calendar.events`, which allows creating and editing calendar events.

## Android app

**Prerequisites:** [Android Studio](https://developer.android.com/studio)

1. Open Android Studio.
2. Select **Open** and choose this directory.
3. Allow Android Studio to import the Gradle project.
4. Create a `.env` file with `GEMINI_API_KEY` if you continue the Android AI Studio workflow.
5. Run the app on an emulator or physical device.
