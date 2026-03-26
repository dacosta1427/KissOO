<script lang="ts">
  import '../app.css';
  import { onMount } from 'svelte';
  import { session } from '$lib/state/session.svelte';
  import { initBackend } from '$lib/api/Auth';
  import { Server } from '$lib/services/Server';
  import { initLocale } from '$lib/i18n';
  import Navbar from '$lib/components/Navbar.svelte';
  import GlobalModal from '$lib/components/GlobalModal.svelte';
  import NotificationToast from '$lib/components/NotificationToast.svelte';

  onMount(async () => {
    // Restore session and language preference
    initBackend();
    const restored = session.restore();
    if (restored && session.uuid) {
      Server.setUUID(session.uuid);
      console.log('[Layout] Restored session UUID:', session.uuid);
      
      // Try to restore username from stored credentials
      const credentials = await session.getStoredCredentials();
      if (credentials) {
        session.setUsername(credentials.username);
        console.log('[Layout] Restored username:', credentials.username);
      }
    }
    
    // Initialize locale from session, cookie, or browser (in priority order)
    const userPreferredLanguage = session.restoreLanguage();
    initLocale(userPreferredLanguage);
  });
</script>

<GlobalModal />
<NotificationToast />
<Navbar />

<slot />
