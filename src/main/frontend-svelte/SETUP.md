# Svelte 5 Frontend Setup Guide

## Overview

This guide covers how to set up a Svelte 5 frontend within the KissOO framework. The frontend is located in `src/main/frontend-svelte/` and communicates with the KissOO backend via REST API.

## Prerequisites

- **Node.js 20+** (required by SvelteKit/Vite)
- **Java 17+** (for the KissOO backend)
- **npm** (or pnpm/yarn)

To check your Node version:
```bash
node --version
```

To switch Node versions (if using nvm):
```bash
nvm use 20
```

## Quick Start

### 1. Install Dependencies

```bash
cd src/main/frontend-svelte
npm install
```

### 2. Start Development Server

```bash
npm run dev
```

The frontend runs on `http://localhost:5173` by default.

### 3. Start the Backend

In a separate terminal, from the kissOO root:
```bash
./bld run
```

The backend runs on `http://localhost:8080`.

## Project Structure

```
frontend-svelte/
├── src/
│   ├── lib/
│   │   ├── components/     # Reusable Svelte components
│   │   │   ├── Form.svelte
│   │   │   ├── Table.svelte
│   │   │   ├── Navigation.svelte
│   │   │   ├── ScheduleBoard.svelte
│   │   │   └── NotificationToast.svelte
│   │   ├── stores.svelte.js    # Global state (runes)
│   │   ├── utils.ts            # Utility functions
│   │   ├── validation.ts       # Form validation
│   │   ├── kiss-client.ts      # API client wrapper
│   │   └── index.ts            # Library exports
│   ├── routes/                 # SvelteKit routes
│   │   ├── +layout.svelte      # Root layout
│   │   ├── +page.svelte        # Dashboard
│   │   ├── login/              # Login page
│   │   ├── signup/             # Signup page
│   │   ├── cleaners/           # Cleaners CRUD
│   │   ├── houses/             # Houses CRUD
│   │   ├── bookings/           # Bookings CRUD
│   │   ├── schedules/          # Schedules management
│   │   └── api/
│   │       └── kiss-remote.js  # Backend API calls
│   └── services/
│       └── api.ts              # Service layer
├── static/                      # Static assets
├── package.json
├── svelte.config.js
├── vite.config.ts
├── tsconfig.json
└── eslint.config.js
```

## Available Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start development server |
| `npm run build` | Build for production |
| `npm run preview` | Preview production build |
| `npm run check` | Type check with svelte-check |
| `npm run lint` | Run ESLint and Prettier |
| `npm run format` | Format code with Prettier |

## Integration with KissOO Backend

### API Communication

The frontend communicates with the backend via HTTP. Here's how it works:

#### Making API Calls

```javascript
import { kiss } from '$lib/kiss-client.js';

// GET request
const response = await kiss.get('/some-service');
const data = await response.json();

// POST request
const result = await kiss.post('/login', {
    email: 'user@example.com',
    password: 'secret'
});
```

#### kiss-client.ts Reference

Located at `src/lib/kiss-client.ts`:

```typescript
// Base URL is automatically set to the backend
// Configured in +layout.server.js

// GET
kiss.get(url: string, options?: RequestOptions): Promise<Response>

// POST  
kiss.post(url: string, data: any, options?: RequestOptions): Promise<Response>

// PUT
kiss.put(url: string, data: any, options?: RequestOptions): Promise<Response>

// DELETE
kiss.delete(url: string, options?: RequestOptions): Promise<Response>
```

### Session Management

Sessions are handled via cookies. The backend manages authentication.

```javascript
// After login, the backend sets a session cookie
// Subsequent requests automatically include the cookie
```

## Environment Configuration

### Development

In development, the frontend proxies API requests to the backend. This is configured in `vite.config.ts`:

```typescript
// vite.config.ts
export default defineConfig({
    server: {
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true
            }
        }
    }
});
```

### Production

In production, configure the backend URL in your deployment environment.

## Running the Full Stack

### Option 1: Separate Terminals

Terminal 1 (Frontend):
```bash
cd src/main/frontend-svelte
npm run dev
```

Terminal 2 (Backend):
```bash
cd kissOO
./bld run
```

Access the application at `http://localhost:5173`.

### Option 2: Integrated

The backend can serve the frontend static files in production using the WAR deployment.

## Building for Production

```bash
cd src/main/frontend-svelte
npm run build
```

The output is in `frontend-svelte/build/`.

To preview:
```bash
npm run preview
```

## Troubleshooting

### "Node.js version too low"

Ensure you're using Node.js 20+:
```bash
nvm use 20
```

### ESLint errors with "paths[0] argument"

If you get `The "paths[0]" argument must be of type string`:
```javascript
// eslint.config.js - ensure proper ESM setup
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
```

### TypeScript errors

Run type checking:
```bash
npm run check
```

### CORS errors in development

Ensure the backend is running on port 8080. The Vite proxy handles CORS in development.

### "$state is not defined"

Ensure `.js` files using Svelte 5 runes have `.svelte.js` extension:
```bash
mv stores.js stores.svelte.js
```

## Creating a New Feature

### 1. Create a Route

```bash
# Create directory and page
mkdir src/routes/myfeature
touch src/routes/myfeature/+page.svelte
```

### 2. Create Components

```bash
mkdir src/lib/components
touch src/lib/components/MyComponent.svelte
```

### 3. Create API Service

```bash
touch src/services/myfeature.js
```

### 4. Add to Navigation

Update `src/lib/components/Navigation.svelte` to include the new route.

## TypeScript Support

This project supports both JavaScript and TypeScript:

- `.ts` files are type-checked
- `.js` files work without type checking
- Svelte components can use either

## Testing

### Type Checking
```bash
npm run check
```

### Linting
```bash
npm run lint
```

### Formatting
```bash
npm run format
```

## Next Steps

- See [SVELTE5_REFERENCE.md](./SVELTE5_REFERENCE.md) for coding guidelines and patterns
- Review existing components in `src/lib/components/` for examples
- Check `src/routes/` for page implementation examples
