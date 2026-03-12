export const validators = {
	required: (value) => (value && value.trim() !== '' ? null : 'This field is required'),

	email: (value) => {
		const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		return emailRegex.test(value) ? null : 'Please enter a valid email address';
	},

	phone: (value) => {
		if (!value) return null;
		const phoneRegex = /^[\+]?[\d\s\-\(\)]{7,}$/;
		return phoneRegex.test(value) ? null : 'Please enter a valid phone number';
	},

	date: (value) => {
		const date = new Date(value);
		return !isNaN(date.getTime()) ? null : 'Please enter a valid date';
	},

	dateRange: (startDate, endDate) => {
		const start = new Date(startDate);
		const end = new Date(endDate);
		return start < end ? null : 'End date must be after start date';
	},

	number: (value, min = null, max = null) => {
		const num = parseInt(value, 10);
		if (isNaN(num)) return 'Please enter a valid number';
		if (min !== null && num < min) return `Value must be at least ${min}`;
		if (max !== null && num > max) return `Value must be at most ${max}`;
		return null;
	}
};

export function validateForm(fields, data) {
	const errors = {};
	let isValid = true;

	fields.forEach((field) => {
		if (field.required && validators.required(data[field.name])) {
			errors[field.name] = validators.required(data[field.name]);
			isValid = false;
		}

		if (data[field.name] && field.validation) {
			const fieldError = field.validation(data[field.name], data);
			if (fieldError) {
				errors[field.name] = fieldError;
				isValid = false;
			}
		}
	});

	return { errors, isValid };
}
