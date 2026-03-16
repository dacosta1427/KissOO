/**
 * Main application entry point
 */
import { Server } from './services/Server';
import App from './components/App.svelte';

// Set default backend URL
if (window.location.protocol === 'file:') {
  Server.setURL('http://localhost:8080');
} else {
  const port = parseInt(window.location.port || '0');
  if (port >= 8000) {
    Server.setURL(`http://${window.location.hostname}:8080`);
  } else {
    Server.setURL(window.location.origin);
  }
}

// Create the main app component
const target = document.getElementById('svelte');
if (!target) {
  throw new Error('Could not find target element');
}
const app = new App({
  target: target
});

// Export for use in tests and HMR
export default app;