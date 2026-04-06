<script lang="ts">
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
  import { Server } from '$lib/services/Server';
  import { initBackend } from '$lib/api/Auth';
  import { t, currentLocale } from '$lib/i18n';
  
  const tt = (key: string) => t(key, undefined, $currentLocale);

  let token = $state('');
  let userName = $state('');
  let userEmail = $state('');
  let password = $state('');
  let confirmPassword = $state('');
  let passwordError = $state('');
  let submitting = $state(false);
  let status = $state<'form' | 'loading' | 'success' | 'error'>('form');
  let message = $state('');
  let username = $state('');

  onMount(async () => {
    // Initialize backend first
    initBackend();
    
    const urlParams = new URLSearchParams(window.location.search);
    token = urlParams.get('token') || '';
    
    if (!token) {
      status = 'error';
      message = 'Invalid verification link: missing token';
      return;
    }

    // Fetch user info using the token
    try {
      const res = await Server.call('services.Users', 'getUserByToken', { token: token });
      
      if (res._Success) {
        userName = res.userName || '';
        userEmail = res.email || '';
      } else {
        status = 'error';
        message = res._ErrorMessage || 'Invalid or expired verification link';
      }
    } catch (e: any) {
      status = 'error';
      message = e.message || 'Failed to load verification data';
    }
  });

  function validatePassword(): boolean {
    passwordError = '';
    
    if (!password || password.length < 6) {
      passwordError = tt('verify.password_min_length') || 'Password must be at least 6 characters';
      return false;
    }
    
    if (password !== confirmPassword) {
      passwordError = tt('verify.passwords_no_match') || 'Passwords do not match';
      return false;
    }
    
    return true;
  }
  
  async function handleSubmit(e: Event) {
    e.preventDefault();
    
    if (!validatePassword()) {
      return;
    }
    
    submitting = true;
    status = 'loading';
    
    try {
      const res = await Server.call('services.Users', 'verifyEmail', { 
        token: token,
        password: password 
      });
      
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
    } finally {
      submitting = false;
    }
  }
</script>

<div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
  <div class="flex items-center justify-center min-h-[60vh]">
    <div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
      {#if status === 'form'}
        <div class="text-center mb-6">
          {#if userName}
            <h1 class="text-2xl font-bold text-gray-900">{tt('verify.welcome') || 'Welcome'} {userName}!</h1>
          {:else}
            <h1 class="text-2xl font-bold text-gray-900">{tt('verify.verify_email_title') || 'Verify Your Email'}</h1>
          {/if}
          <p class="text-gray-600 mt-2">
            {tt('verify.verify_email_desc') || 'Set your password to activate your account'}
          </p>
          {#if userEmail}
            <p class="text-sm text-gray-500 mt-1">{userEmail}</p>
          {/if}
        </div>
        
        <form onsubmit={handleSubmit}>
          <div class="mb-4">
            <label for="password" class="block text-sm font-medium text-gray-700 mb-1">
              {tt('common.password') || 'Password'}
            </label>
            <input 
              type="password" 
              id="password" 
              bind:value={password}
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder={tt('common.password_placeholder') || 'Enter password'}
              required
              minlength="6"
            />
          </div>
          
          <div class="mb-4">
            <label for="confirmPassword" class="block text-sm font-medium text-gray-700 mb-1">
              {tt('common.confirm_password') || 'Confirm Password'}
            </label>
            <input 
              type="password" 
              id="confirmPassword" 
              bind:value={confirmPassword}
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder={tt('common.confirm_password_placeholder') || 'Confirm password'}
              required
            />
          </div>
          
          {#if passwordError}
            <div class="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
              <p class="text-sm text-red-600">{passwordError}</p>
            </div>
          {/if}
          
          <button 
            type="submit"
            disabled={submitting}
            class="w-full py-2 px-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {#if submitting}
              <span class="inline-flex items-center">
                <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                {tt('verify.verifying') || 'Verifying...'}
              </span>
            {:else}
              {tt('verify.verify_and_set_password') || 'Verify & Set Password'}
            {/if}
          </button>
        </form>
      
      {:else if status === 'loading'}
        <div class="text-center">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <h1 class="text-xl font-semibold text-gray-900">{tt('verify.loading')}</h1>
          <p class="text-gray-600 mt-2">{tt('verify.please_wait')}</p>
        </div>
      
      {:else if status === 'success'}
        <div class="text-center">
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
        </div>
      
      {:else}
        <div class="text-center">
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
        </div>
      {/if}
    </div>
  </div>
</div>