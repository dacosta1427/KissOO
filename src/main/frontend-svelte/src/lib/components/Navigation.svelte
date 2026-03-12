<script>
	import { userState, userActions } from '$lib/stores.svelte.js';

	function logout() {
		userActions.logout();
	}
</script>

<nav class="navigation">
	<div class="nav-container">
		<div class="nav-brand">
			<h1>CleaningScheduler</h1>
		</div>

		<div class="nav-links">
			{#if userState.value}
				<a href="/" class="nav-link">Dashboard</a>
				<a href="/cleaners" class="nav-link">Cleaners</a>
				<a href="/bookings" class="nav-link">Bookings</a>
				<a href="/schedules" class="nav-link">Schedules</a>
				<a href="/houses" class="nav-link">Houses</a>
			{/if}
		</div>

		<div class="nav-user">
			{#if userState.value}
				<span class="user-name">Welcome, {userState.value?.name || 'User'}</span>
				<button class="btn btn-secondary" onclick={logout}>Logout</button>
			{:else}
				<a href="/login" class="btn btn-primary">Login</a>
			{/if}
		</div>
	</div>
</nav>

<style>
	.navigation {
		background: var(--nav-bg, #2c3e50);
		color: white;
		padding: 1rem 0;
		box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
		position: sticky;
		top: 0;
		z-index: 100;
	}

	.nav-container {
		max-width: 1200px;
		margin: 0 auto;
		padding: 0 2rem;
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 2rem;
	}

	.nav-brand h1 {
		margin: 0;
		font-size: 1.5rem;
		font-weight: 700;
	}

	.nav-links {
		display: flex;
		gap: 1rem;
		flex: 1;
	}

	.nav-link {
		color: white;
		text-decoration: none;
		padding: 0.5rem 1rem;
		border-radius: 6px;
		transition: all 0.2s;
		font-weight: 500;
	}

	.nav-link:hover {
		background: rgba(255, 255, 255, 0.1);
		color: white;
	}

	.nav-user {
		display: flex;
		align-items: center;
		gap: 1rem;
	}

	.user-name {
		font-size: 0.9rem;
		opacity: 0.8;
	}

	.btn {
		padding: 0.5rem 1rem;
		border: none;
		border-radius: 6px;
		font-size: 0.9rem;
		font-weight: 600;
		cursor: pointer;
		transition: all 0.2s;
	}

	.btn-primary {
		background: var(--primary-color, #3498db);
		color: white;
	}

	.btn-primary:hover {
		background: var(--primary-hover, #2980b9);
	}

	.btn-secondary {
		background: transparent;
		color: white;
		border: 1px solid rgba(255, 255, 255, 0.3);
	}

	.btn-secondary:hover {
		background: rgba(255, 255, 255, 0.1);
	}

	/* Mobile responsive */
	@media (max-width: 768px) {
		.nav-container {
			flex-direction: column;
			gap: 1rem;
			padding: 0 1rem;
		}

		.nav-links {
			flex-direction: column;
			width: 100%;
			align-items: center;
		}

		.nav-link {
			width: 100%;
			text-align: center;
			margin-bottom: 0.5rem;
		}

		.nav-user {
			width: 100%;
			justify-content: center;
		}
	}
</style>
