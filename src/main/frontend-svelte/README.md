# KissOO Svelte 5 Frontend

A modern Svelte 5 frontend for the KissOO framework.

## Features

- **Svelte 5 with Runes**: Modern reactive framework with runes
- **TypeScript**: Full type safety throughout the application
- **Svelte Kit**: File-based routing and server-side rendering
- **AG-Grid**: Data grid component for tables
- **CKEditor**: Rich text editor (loaded from CDN)
- **Tailwind CSS**: Utility-first styling
- **Perst Integration**: Embedded object database support

## Getting Started

### Prerequisites

- Node.js 18+
- npm or yarn

### Installation

1. Clone the repository
2. Navigate to the frontend-svelte directory
3. Install dependencies:

```bash
npm install
```

4. Start the development server:

```bash
npm run dev
```

5. Open http://localhost:5173 in your browser

### Project Structure

```
src/
├── components/     # Reusable UI components
├── services/       # Backend communication services
├── lib/           # Third-party libraries
├── routes/        # Svelte Kit file-based routing
├── stores/        # State management
└── types/         # TypeScript type definitions
```

## Development

### Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run check` - Type checking
- `npm run lint` - Lint code
- `npm run format` - Format code

### Backend Integration

The frontend communicates with the KissOO backend using the `Server.call()` method. The backend URL is automatically detected based on the environment.

### Configuration

The application automatically detects the backend URL:
- Development: `http://localhost:8080`
- Production: Current origin
- Electron: `http://localhost:8080`

## Technologies Used

- **Svelte 5**: Modern reactive framework
- **TypeScript**: Type safety
- **Vite**: Build tool and dev server
- **Svelte Kit**: Full-stack framework
- **Tailwind CSS**: Styling
- **AG-Grid**: Data grid
- **CKEditor**: Rich text editing

## License

This project is part of the KissOO framework.