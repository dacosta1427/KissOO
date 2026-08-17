<script lang="ts">
  import { currentLocale, setLocale, getLocaleOptions, t } from '$lib/i18n';
  import { session } from '$lib/state/session.svelte';
  import { browser } from '$app/environment';
  import { onMount, onDestroy } from 'svelte';
  
  let isOpen = $state(false);
  
  // Helper for reactive translations
  const tt = (key: string) => t(key, undefined, $currentLocale);
  
  function selectLocale(locale: string) {
    // Save to session and cookie - store reactivity handles updates
    session.setLanguage(locale);
    setLocale(locale as any);
    isOpen = false;
  }
  
  function handleClickOutside(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.language-switcher')) {
      isOpen = false;
    }
  }
  
  onMount(() => {
    if (browser) {
      document.addEventListener('click', handleClickOutside);
    }
  });
  
  onDestroy(() => {
    if (browser) {
      document.removeEventListener('click', handleClickOutside);
    }
  });
</script>

<div class="language-switcher relative">
  <button 
    onclick={() => isOpen = !isOpen}
    class="flex items-center gap-1 px-2 py-1 text-sm text-gray-600 hover:text-gray-900 rounded-md hover:bg-gray-100"
    aria-label={tt('nav.language')}
  >
    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
    <span class="uppercase">{$currentLocale}</span>
    <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
    </svg>
  </button>
  
  {#if isOpen}
    <div class="absolute right-0 mt-1 w-32 bg-white border border-gray-200 rounded-md shadow-lg z-50">
      {#each getLocaleOptions() as option}
        <button
          onclick={() => selectLocale(option.value)}
          class="w-full text-left px-3 py-2 text-sm hover:bg-gray-100 {option.value === $currentLocale ? 'bg-blue-50 text-blue-600' : 'text-gray-700'}"
        >
          {option.label}
        </button>
      {/each}
    </div>
  {/if}
</div>
