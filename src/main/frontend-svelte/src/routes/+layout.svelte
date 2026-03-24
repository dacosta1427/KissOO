<script lang="ts">
  import '../app.css';
  import { onMount } from 'svelte';
  import { session } from '$lib/state/session';
  import { initBackend } from '$lib/api/Auth';
  import { Server } from '$lib/services/Server';
  import Navbar from '$lib/components/Navbar.svelte';
  import GlobalModal from '$lib/components/GlobalModal.svelte';
  import NotificationToast from '$lib/components/NotificationToast.svelte';

  onMount(() => {
    initBackend();
    const restored = session.restore();
    if (restored && session.uuid) {
      Server.setUUID(session.uuid);
      console.log('[Layout] Restored session UUID:', session.uuid);
    }
  });
</script>

<GlobalModal />
<NotificationToast />
<Navbar />

<slot />
