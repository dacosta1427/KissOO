<script lang="ts">
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { Server } from '$lib/services/Server';
  import { initBackend } from '$lib/api/Auth';
  import { t, currentLocale } from '$lib/i18n';
  
  // Reactive translation helper
  const tt = (key: string) => t(key, undefined, $currentLocale);
  
  let status = $state<'loading' | 'success' | 'error'>('loading');
  let message = $state('');
  let username = $state('');
  
  onMount(async () => {
    initBackend();
    
    // Get token from URL
    const token = $page.url.searchParams.get('token');
    
    if (!token) {
      status = 'error';
      message = 'No verification token provided';
      return;
    }
    
    try {
      const res = await Server.call('services.Users', 'verifyEmail', { token });
      
      if (res._Success || res.success) {
        status = 'success';
        message = res.message || 'Email verified successfully!';
        username = res.username || '';
      } else {
        status = 'error';
        message = res._ErrorMessage || res.error || 'Verification failed';
      }
    } catch (e: any) {
      status = 'error';
      message = e.message || 'Failed to verify email';
    }
  });
</script>

<div class="min-h-screen bg-gray-50 flex items-center justify-center">
  <div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md text-center">
    {#if status === 'loading'}
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
      <h1 class="text-xl font-semibold text-gray-900">{tt('verify.loading')}</h1>
      <p class="text-gray-600 mt-2">{tt('verify.please_wait')}</p>
      
    {:else if status === 'success'}
      <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
        <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
        </svg>
      </div>
      <h1 class="text-xl font-semibold text-green-800">{tt('verify.success_title')}</h1>
      <p class="text-gray-600 mt-2">{message}</p>
      {#if username}
        <p class="text-gray-500 mt-1">{tt('verify.login_as')} <strong>{username}</strong></p>
      {/if}
      <a href="/login" class="inline-block mt-6 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
        {tt('verify.go_to_login')}
      </a>
      
    {:else}
      <div class="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
        <svg class="w-8 h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
        </svg>
      </div>
      <h1 class="text-xl font-semibold text-red-800">{tt('verify.error_title')}</h1>
      <p class="text-gray-600 mt-2">{message}</p>
      <div class="mt-6 space-x-4">
        <a href="/signup" class="inline-block px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
          {tt('verify.try_again')}
        </a>
        <a href="/" class="inline-block px-6 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors">
          {tt('verify.go_home')}
        </a>
      </div>
    {/if}
  </div>
</div>
