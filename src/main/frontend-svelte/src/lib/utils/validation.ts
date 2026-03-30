/**
 * Validation utilities for form validation
 */

// Type definitions
export type ValidationFunction = (value: any, data?: any) => string | null;
export type FieldValidator = (value: any, data?: any) => string | null;

export interface FieldConfig {
	name: string;
	required?: boolean;
	validation?: ValidationFunction;
}

export interface ValidationResult {
	errors: Record<string, string>;
	isValid: boolean;
}

/**
 * Built-in validators
 */
export const validators = {
	/**
	 * Check if a field is required
	 */
	required: (value: any): string | null => {
		return value && value.trim() !== '' ? null : 'This field is required';
	},

	/**
	 * Validate email format
	 */
	email: (value: string): string | null => {
		const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		return emailRegex.test(value) ? null : 'Please enter a valid email address';
	},

	/**
	 * Validate phone number format
	 */
	phone: (value: string): string | null => {
		if (!value) return null;
		const phoneRegex = /^[\+]?[\d\s\-\(\)]{7,}$/;
		return phoneRegex.test(value) ? null : 'Please enter a valid phone number';
	},

	/**
	 * Validate date format
	 */
	date: (value: string | Date): string | null => {
		const date = new Date(value);
		return !isNaN(date.getTime()) ? null : 'Please enter a valid date';
	},

	/**
	 * Validate date range (end date after start date)
	 */
	dateRange: (startDate: string | Date, endDate: string | Date): string | null => {
		const start = new Date(startDate);
		const end = new Date(endDate);
		return start < end ? null : 'End date must be after start date';
	},

	/**
	 * Validate number with optional min/max constraints
	 */
	number: (
		value: string | number,
		min: number | null = null,
		max: number | null = null
	): string | null => {
		const num = typeof value === 'string' ? parseInt(value, 10) : value;
		if (isNaN(num)) return 'Please enter a valid number';
		if (min !== null && num < min) return `Value must be at least ${min}`;
		if (max !== null && num > max) return `Value must be at most ${max}`;
		return null;
	},

	/**
	 * Validate string length
	 */
	length: (value: string, min: number = 0, max: number = Infinity): string | null => {
		if (value.length < min) return `Must be at least ${min} characters long`;
		if (value.length > max) return `Must be at most ${max} characters long`;
		return null;
	},

	/**
	 * Validate against a custom regex pattern
	 */
	pattern: (value: string, regex: RegExp, message: string = 'Invalid format'): string | null => {
		return regex.test(value) ? null : message;
	}
};

/**
 * Validate a form based on field configurations
 * @param fields - Array of field configurations
 * @param data - Form data object
 * @returns Validation result with errors and validity status
 */
export function validateForm(fields: FieldConfig[], data: Record<string, any>): ValidationResult {
	const errors: Record<string, string> = {};
	let isValid = true;

	fields.forEach((field) => {
		// Check required validation
		if (field.required && validators.required(data[field.name])) {
			errors[field.name] = validators.required(data[field.name])!;
			isValid = false;
		}

		// Check custom validation
		if (data[field.name] !== undefined && field.validation) {
			const fieldError = field.validation(data[field.name], data);
			if (fieldError) {
				errors[field.name] = fieldError;
				isValid = false;
			}
		}
	});

	return { errors, isValid };
}

/**
 * Create a validation chain for a field
 */
export class ValidationChain {
	private validators: ValidationFunction[] = [];

	/**
	 * Add a required validator
	 */
	required(message: string = 'This field is required'): this {
		this.validators.push((value) => validators.required(value) || message);
		return this;
	}

	/**
	 * Add an email validator
	 */
	email(message: string = 'Please enter a valid email address'): this {
		this.validators.push((value) => validators.email(value) || message);
		return this;
	}

	/**
	 * Add a phone validator
	 */
	phone(message: string = 'Please enter a valid phone number'): this {
		this.validators.push((value) => validators.phone(value) || message);
		return this;
	}

	/**
	 * Add a date validator
	 */
	date(message: string = 'Please enter a valid date'): this {
		this.validators.push((value) => validators.date(value) || message);
		return this;
	}

	/**
	 * Add a number validator with optional min/max
	 */
	number(min: number | null = null, max: number | null = null, message?: string): this {
		const msg = message ?? null;
		this.validators.push((value) => validators.number(value, min, max) || msg);
		return this;
	}

	/**
	 * Add a length validator
	 */
	length(min: number = 0, max: number = Infinity, message?: string): this {
		const msg = message ?? null;
		this.validators.push((value) => validators.length(value, min, max) || msg);
		return this;
	}

	/**
	 * Add a pattern validator
	 */
	pattern(regex: RegExp, message: string = 'Invalid format'): this {
		this.validators.push((value) => validators.pattern(value, regex, message));
		return this;
	}

	/**
	 * Add a custom validator
	 */
	custom(validator: ValidationFunction): this {
		this.validators.push(validator);
		return this;
	}

	/**
	 * Execute all validators on a value
	 */
	validate(value: any, data?: any): string | null {
		for (const validator of this.validators) {
			const error = validator(value, data);
			if (error) {
				return error;
			}
		}
		return null;
	}
}

/**
 * Create a validation chain for a field
 */
export function field(): ValidationChain {
	return new ValidationChain();
}

/**
 * Validate multiple fields at once
 */
export function validateFields(
	fieldConfigs: Record<string, ValidationChain>,
	data: Record<string, any>
): ValidationResult {
	const errors: Record<string, string> = {};
	let isValid = true;

	Object.keys(fieldConfigs).forEach((fieldName) => {
		const chain = fieldConfigs[fieldName];
		if (!chain) return;
		const error = chain.validate(data[fieldName], data);
		if (error) {
			errors[fieldName] = error;
			isValid = false;
		}
	});

	return { errors, isValid };
}

// Common validation presets
export const commonValidations = {
	/**
	 * Email field validation
	 */
	email: () => field().required().email(),

	/**
	 * Phone field validation
	 */
	phone: () => field().phone(),

	/**
	 * Name field validation (required, minimum length)
	 */
	name: (minLength: number = 2) => field().required().length(minLength),

	/**
	 * Date field validation
	 */
	date: () => field().required().date(),

	/**
	 * Numeric field validation with optional range
	 */
	number: (min: number | null = null, max: number | null = null) =>
		field().required().number(min, max),

	/**
	 * Password field validation (minimum length and complexity)
	 */
	password: (minLength: number = 8) =>
		field()
			.required()
			.length(minLength)
			.pattern(
				/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/,
				'Password must contain uppercase, lowercase, and number'
			),

	/**
	 * Confirm password validation
	 */
	confirmPassword: (passwordField: string) => (value: string, data: any) =>
		value === data[passwordField] ? null : 'Passwords do not match'
};