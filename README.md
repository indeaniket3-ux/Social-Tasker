# Social Tasker

Social Tasker turns social messages into prioritized daily work. The uploaded project started as an Android app, and this repository now also includes a Vercel-ready web app at the project root.

## What is included

- Static frontend dashboard in `index.html`, `styles.css`, and `assets/app.js`
- Serverless suggestion API in `api/suggestions.js`
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

## Android app

**Prerequisites:** [Android Studio](https://developer.android.com/studio)

1. Open Android Studio.
2. Select **Open** and choose this directory.
3. Allow Android Studio to import the Gradle project.
4. Create a `.env` file with `GEMINI_API_KEY` if you continue the Android AI Studio workflow.
5. Run the app on an emulator or physical device.
