<script lang="ts">
	// Owners page - backend entity needed
	// Currently shows placeholder
</script>

<div class="owners-page">
	<div class="page-header">
		<h1>Owners</h1>
	</div>

	<div class="placeholder">
		<p>Owner management requires backend entity implementation.</p>
		<p class="text-sm mt-2">One owner can own multiple houses.</p>
		<p class="text-sm">Please create Owner entity in backend to enable this feature.</p>
	</div>
</div>

<style>
	.owners-page {
		padding: 2rem;
		max-width: 1200px;
		margin: 0 auto;
	}

	.page-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 2rem;
		border-bottom: 2px solid var(--border-color, #e5e7eb);
		padding-bottom: 1rem;
	}

	.page-header h1 {
		margin: 0;
		color: var(--text-color, #111827);
	}

	.placeholder {
		background: #fef3c7;
		border: 1px solid #fcd34d;
		border-radius: 8px;
		padding: 2rem;
		text-align: center;
		color: #92400e;
	}

	.placeholder p {
		margin: 0.5rem 0;
	}
</style>
