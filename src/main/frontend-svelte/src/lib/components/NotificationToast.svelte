<script lang="ts">
	import { notificationsState, notificationActions } from '$lib/stores.svelte.js';
	
	// Svelte 5: Use $derived for reactive store access
	let notificationList = $derived(notificationsState.value);
	
	function getNotificationClass(type: string): string {
		switch (type) {
			case 'success':
				return 'bg-green-50 border-green-400';
			case 'error':
				return 'bg-red-50 border-red-400';
			case 'warning':
				return 'bg-yellow-50 border-yellow-400';
			case 'info':
				return 'bg-blue-50 border-blue-400';
			default:
				return 'bg-gray-50 border-gray-400';
		}
	}
	
	function getIconColor(type: string): string {
		switch (type) {
			case 'success':
				return 'text-green-400';
			case 'error':
				return 'text-red-400';
			case 'warning':
				return 'text-yellow-400';
			case 'info':
				return 'text-blue-400';
			default:
				return 'text-gray-400';
		}
	}
	
	function getCloseButtonColor(type: string): string {
		switch (type) {
			case 'success':
				return 'text-green-500 hover:text-green-600';
			case 'error':
				return 'text-red-500 hover:text-red-600';
			case 'warning':
				return 'text-yellow-500 hover:text-yellow-600';
			case 'info':
				return 'text-blue-500 hover:text-blue-600';
			default:
				return 'text-gray-500 hover:text-gray-600';
		}
	}
	
	function dismissNotification(id: number) {
		notificationActions.remove(id);
	}
</script>

{#if notificationList.length > 0}
	<div class="pointer-events-none fixed inset-0 z-50 flex items-start justify-end px-4 py-6 sm:p-6">
		<div class="flex w-full flex-col items-center space-y-4 sm:items-end">
			{#each notificationList as notification (notification.id)}
				<div class="pointer-events-auto w-full max-w-sm overflow-hidden rounded-lg border-l-4 shadow-lg {getNotificationClass(notification.type)}">
					<div class="p-4">
						<div class="flex items-start">
							<div class="flex-shrink-0">
								{#if notification.type === 'success'}
									<svg class="h-5 w-5 {getIconColor(notification.type)}" viewBox="0 0 20 20" fill="currentColor">
										<path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd" />
									</svg>
								{:else if notification.type === 'error'}
									<svg class="h-5 w-5 {getIconColor(notification.type)}" viewBox="0 0 20 20" fill="currentColor">
										<path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd" />
									</svg>
								{:else if notification.type === 'warning'}
									<svg class="h-5 w-5 {getIconColor(notification.type)}" viewBox="0 0 20 20" fill="currentColor">
										<path fill-rule="evenodd" d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd" />
									</svg>
								{:else}
									<svg class="h-5 w-5 {getIconColor(notification.type)}" viewBox="0 0 20 20" fill="currentColor">
										<path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a.75.75 0 000 1.5h.253a.25.25 0 01.244.304l-.459 2.066A1.75 1.75 0 0010.747 15H11a.75.75 0 000-1.5h-.253a.25.25 0 01-.244-.304l.459-2.066A1.75 1.75 0 009.253 9H9z" clip-rule="evenodd" />
									</svg>
								{/if}
							</div>
							<div class="ml-3 w-0 flex-1 pt-0.5">
								<p class="text-sm font-medium text-gray-900">
									{notification.message}
								</p>
							</div>
							<div class="ml-4 flex flex-shrink-0">
								<button
									type="button"
									onclick={() => dismissNotification(notification.id)}
									class="inline-flex rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2 {getCloseButtonColor(notification.type)}"
								>
									<span class="sr-only">Close</span>
									<svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
										<path d="M6.28 5.22a.75.75 0 00-1.06 1.06L8.94 10l-3.72 3.72a.75.75 0 101.06 1.06L10 11.06l3.72 3.72a.75.75 0 101.06-1.06L11.06 10l3.72-3.72a.75.75 0 00-1.06-1.06L10 8.94 6.28 5.22z" />
									</svg>
								</button>
							</div>
						</div>
					</div>
				</div>
			{/each}
		</div>
	</div>
{/if}