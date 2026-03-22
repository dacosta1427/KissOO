<script lang="ts">
  import '../app.css';
  import { onMount } from 'svelte';
  import { session } from '$lib/state/session';
  import { initBackend } from '$lib/api/Auth';
  import Navbar from '$lib/components/Navbar.svelte';
  import GlobalModal from '$lib/components/GlobalModal.svelte';

  onMount(() => {
    initBackend();
    session.restore();
  });
</script>

<GlobalModal />
<Navbar />

<slot />
