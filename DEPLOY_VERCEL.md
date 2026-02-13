# Deploying Huobao Drama to Vercel

Due to the architecture of Huobao Drama (Go backend with system dependencies like FFmpeg + Vue3 Frontend), a **Split Deployment** strategy is required. Vercel is optimized for frontend and serverless functions, but not for long-running processes or heavy system dependencies required by the backend.

## Architecture Overview

- **Frontend (Vue 3)**: Deployed to **Vercel**.
- **Backend (Go + FFmpeg)**: Deployed to a Docker-compatible platform like **Railway**, **Render**, or **Fly.io**.

---

## Part 1: Deploy Backend (Required First)

The frontend needs a live backend to communicate with. We will deploy the backend first to get its URL.

### Option A: Deploy to Railway (Recommended)

1.  Create a [Railway](https://railway.app/) account.
2.  Create a **New Project** -> **Deploy from GitHub repo**.
3.  Select your `huobao-drama` repository.
4.  Railway should automatically detect the `Dockerfile` in the root.
5.  Once deployed, go to **Settings** -> **Networking** and generate a Domain (e.g., `huobao-drama-production.up.railway.app`).
6.  **Copy this URL**. You will need it for the frontend.
    *   *Note: Railway handles the SQLite database and FFmpeg installation automatically via the Dockerfile.*
    *   *Persistence: For production data safety on Railway, ensure you mount a Volume to `/app/data`.*

### Option B: Deploy to Render

1.  Create a [Render](https://render.com/) account.
2.  Create a new **Web Service**.
3.  Connect your repository.
4.  Choose **Docker** as the runtime.
5.  Render will build using the `Dockerfile`.
6.  Once deployed, copy the service URL (e.g., `https://huobao-drama.onrender.com`).
    *   *Note: Render's free tier spins down after inactivity. For persistent storage (SQLite), you need a paid plan with a Disk attached to `/app/data`.*

---

## Part 2: Deploy Frontend to Vercel

Now that the backend is running, we can deploy the frontend.

1.  **Prepare the Project (Already done by Agent)**
    *   A `vercel.json` has been added to the `web/` directory.
    *   The `web/src/utils/request.ts` has been updated to support the `VITE_API_BASE_URL` environment variable.

2.  **Import to Vercel**
    *   Go to [Vercel Dashboard](https://vercel.com/dashboard).
    *   Click **Add New...** -> **Project**.
    *   Import your `huobao-drama` repository.

3.  **Configure Project Settings**
    *   **Root Directory**: Click `Edit` and select `web`. **(Crucial Step)**
    *   **Framework Preset**: Vercel should auto-detect **Vite**.
    *   **Build Command**: `npm run build` (Default is fine).
    *   **Output Directory**: `dist` (Default is fine).

4.  **Environment Variables**
    *   Add a new variable:
        *   **Name**: `VITE_API_BASE_URL`
        *   **Value**: The URL from Part 1 (e.g., `https://huobao-drama-production.up.railway.app/api/v1`)
        *   *Important: Make sure to include `/api/v1` at the end if your backend URL is just the domain.*

5.  **Deploy**
    *   Click **Deploy**.

## Part 3: Final Configuration

1.  **CORS**:
    *   If you encounter CORS errors (frontend cannot talk to backend), you may need to update the Backend configuration.
    *   In `configs/config.yaml` (on the server), update `server.cors_origins` to include your Vercel domain (e.g., `https://huobao-drama.vercel.app`).
    *   *Note: Since the backend is built into a Docker image, you might need to use Environment Variables to override the config, or rebuild the image with the updated config.*
    *   The `Dockerfile` allows mounting `config.yaml`. On Railway, you can use the `CONFIG_FILE_CONTENT` variable approach if the app supports it, or simply fork the repo, update `configs/config.example.yaml`, and redeploy the backend.

## Summary

- **Frontend**: `https://your-project.vercel.app`
- **Backend**: `https://your-project.up.railway.app`
- **Data**: Stored in the Backend container (ensure volumes are configured for persistence).
