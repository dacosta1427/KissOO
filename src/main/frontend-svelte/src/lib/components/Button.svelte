<script lang="ts">
	interface Props {
		type?: 'button' | 'submit' | 'reset';
		class?: string;
		disabled?: boolean;
		loading?: boolean;
		onclick?: () => void;
		children?: any;
	}

	let { type = 'button', class: className = '', disabled = false, loading = false, onclick, children }: Props = $props();
</script>

<button
	{type}
	class="btn {className}"
	class:btn-disabled={disabled || loading}
	class:btn-loading={loading}
	disabled={disabled || loading}
	{onclick}
>
	{#if loading}
		<span class="spinner"></span>
	{/if}
	{#if children}
		{@render children()}
	{/if}
</button>

<style>
	.btn { padding: 0.5rem 1rem; border: none; border-radius: 6px; font-size: 0.875rem; font-weight: 600; cursor: pointer; transition: all 0.2s; display: inline-flex; align-items: center; justify-content: center; gap: 0.5rem; }
	.btn-primary { background: #3b82f6; color: white; }
	.btn-primary:hover { background: #2563eb; }
	.btn-secondary { background: #6b7280; color: white; }
	.btn-secondary:hover { background: #4b5563; }
	.btn-danger { background: #ef4444; color: white; }
	.btn-danger:hover { background: #dc2626; }
	.btn-sm { padding: 0.25rem 0.75rem; font-size: 0.75rem; }
	.btn-disabled { background: #93c5fd; color: #1e40af; cursor: not-allowed; }
	.btn-disabled:hover { background: #93c5fd; }
	.btn-loading { cursor: wait; }
	.spinner { width: 1rem; height: 1rem; border: 2px solid white; border-top-color: transparent; border-radius: 50%; animation: spin 1s linear infinite; }
	.spinner:not([data-theme="light"]) { border-color: #9ca3af; border-top-color: transparent; }
	@keyframes spin { to { transform: rotate(360deg); } }
</style>