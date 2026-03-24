<script lang="ts">
	// Svelte 5: Use $props() for props in runes mode
	interface Field {
		name: string;
		label: string;
		type?: 'text' | 'email' | 'tel' | 'number' | 'select' | 'textarea' | 'checkbox' | 'password';
		required?: boolean;
		placeholder?: string;
		options?: Array<{ value: string; label: string }>;
		rows?: number;
		helpText?: string;
	}

	interface Props {
		fields?: Field[];
		data?: Record<string, any>;
		errors?: Record<string, string>;
		loading?: boolean;
		title?: string;
		submitLabel?: string;
		cancelLabel?: string;
		onSubmit?: (data: Record<string, any>) => void;
		onCancel?: () => void;
		onUpdate?: (detail: { field: string; value: any }) => void;
	}

	let {
		fields = [],
		data = $bindable({}),
		errors = $bindable({}),
		loading = false,
		title = '',
		submitLabel = 'Save',
		cancelLabel = 'Cancel',
		onSubmit,
		onCancel,
		onUpdate
	}: Props = $props();

	// Debug logging for fields
	$effect(() => {
		console.log('[Form] fields updated:', fields?.length);
		if (fields && fields.length > 0) {
			fields.forEach((field, i) => {
				if (field.options) {
					console.log(`[Form] field ${i} (${field.name}) options:`, field.options.map(o => ({ value: o.value, label: o.label })));
				}
			});
		}
	});

	function handleSubmit(event: Event) {
		event.preventDefault();
		onSubmit?.(data);
	}

	function handleCancel() {
		onCancel?.();
	}

	function updateField(fieldName: string, value: any) {
		data[fieldName] = value;
		// Clear field-specific error when user starts typing
		if (errors[fieldName]) {
			errors[fieldName] = '';
		}
		onUpdate?.({ field: fieldName, value });
	}

	function getFieldError(field: Field): string {
		return errors[field.name] || '';
	}

	function isFieldInvalid(field: Field): boolean {
		return !!getFieldError(field);
	}
</script>

<form class="space-y-6" onsubmit={handleSubmit}>
	{#if title}
		<div class="border-b border-gray-200 pb-4">
			<h3 class="text-lg font-medium leading-6 text-gray-900">{title}</h3>
		</div>
	{/if}

	<div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
		{#each fields as field}
			<div class="space-y-1">
				<label for={field.name} class="block text-sm font-medium text-gray-700">
					{field.label}
					{#if field.required}
						<span class="text-red-500">*</span>
					{/if}
				</label>

				{#if field.type === 'text' || field.type === 'email' || field.type === 'tel' || field.type === 'number' || field.type === 'password'}
					<input
						type={field.type}
						id={field.name}
						name={field.name}
						value={data[field.name] ?? ''}
						placeholder={field.placeholder}
						required={field.required}
						disabled={loading}
						class="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm {isFieldInvalid(field) ? 'border-red-300 text-red-900 placeholder-red-300 focus:border-red-500 focus:ring-red-500' : ''}"
						oninput={(e) => updateField(field.name, (e.target as HTMLInputElement).value)}
					/>
				{:else if field.type === 'select'}
					<select
						id={field.name}
						name={field.name}
						value={data[field.name] ?? ''}
						required={field.required}
						disabled={loading}
						class="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm {isFieldInvalid(field) ? 'border-red-300 text-red-900 focus:border-red-500 focus:ring-red-500' : ''}"
						onchange={(e) => updateField(field.name, (e.target as HTMLSelectElement).value)}
						style="background-color: red;"
					>
						<option value="">-- Select {field.label} --</option>
						{#each (field.options ? [...field.options] : []) as option}
							<option value={option.value}>{option.label}</option>
						{/each}
					</select>
				{:else if field.type === 'textarea'}
					<textarea
						id={field.name}
						name={field.name}
						value={data[field.name] ?? ''}
						placeholder={field.placeholder}
						required={field.required}
						disabled={loading}
						rows={field.rows || 4}
						class="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm {isFieldInvalid(field) ? 'border-red-300 text-red-900 placeholder-red-300 focus:border-red-500 focus:ring-red-500' : ''}"
						oninput={(e) => updateField(field.name, (e.target as HTMLTextAreaElement).value)}
					></textarea>
				{:else if field.type === 'checkbox'}
					<div class="flex items-center">
						<input
							type="checkbox"
							id={field.name}
							name={field.name}
							checked={data[field.name] ?? false}
							disabled={loading}
							class="h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
							onchange={(e) => updateField(field.name, (e.target as HTMLInputElement).checked)}
						/>
						<label for={field.name} class="ml-2 block text-sm text-gray-700">
							{field.label}
						</label>
					</div>
				{/if}

				{#if isFieldInvalid(field)}
					<p class="text-sm text-red-600">{getFieldError(field)}</p>
				{/if}

				{#if field.helpText && !isFieldInvalid(field)}
					<p class="text-sm text-gray-500">{field.helpText}</p>
				{/if}
			</div>
		{/each}
	</div>

	<div class="flex justify-end space-x-3 border-t border-gray-200 pt-4">
		<button
			type="button"
			onclick={handleCancel}
			disabled={loading}
			class="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
		>
			{cancelLabel}
		</button>
		<button
			type="submit"
			disabled={loading}
			class="inline-flex justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
		>
			{loading ? 'Saving...' : submitLabel}
		</button>
	</div>
</form>