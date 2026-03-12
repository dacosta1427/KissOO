/**
 * Utility functions for the CleaningScheduler frontend
 */

/**
 * Format a date string to a more readable format
 * @param date - The date to format
 * @param format - The format type ('short', 'medium', 'long')
 * @returns Formatted date string
 */
export function formatDate(
	date: string | Date,
	format: 'short' | 'medium' | 'long' = 'medium'
): string {
	const d = new Date(date);

	if (isNaN(d.getTime())) {
		return 'Invalid Date';
	}

	const options = {
		short: { year: '2-digit', month: '2-digit', day: '2-digit' },
		medium: { year: 'numeric', month: 'short', day: 'numeric' },
		long: { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }
	};

	return d.toLocaleDateString('en-US', options[format]);
}

/**
 * Format a time string
 * @param time - The time to format
 * @returns Formatted time string
 */
export function formatTime(time: string | Date): string {
	const d = new Date(time);

	if (isNaN(d.getTime())) {
		return 'Invalid Time';
	}

	return d.toLocaleTimeString('en-US', {
		hour: '2-digit',
		minute: '2-digit'
	});
}

/**
 * Format a date-time string
 * @param dateTime - The date-time to format
 * @param format - The format type
 * @returns Formatted date-time string
 */
export function formatDateTime(
	dateTime: string | Date,
	format: 'short' | 'medium' | 'long' = 'medium'
): string {
	const d = new Date(dateTime);

	if (isNaN(d.getTime())) {
		return 'Invalid Date';
	}

	const options = {
		short: {
			year: '2-digit',
			month: '2-digit',
			day: '2-digit',
			hour: '2-digit',
			minute: '2-digit'
		},
		medium: {
			year: 'numeric',
			month: 'short',
			day: 'numeric',
			hour: '2-digit',
			minute: '2-digit'
		},
		long: {
			year: 'numeric',
			month: 'long',
			day: 'numeric',
			weekday: 'long',
			hour: '2-digit',
			minute: '2-digit'
		}
	};

	return d.toLocaleDateString('en-US', options[format]);
}

/**
 * Generate a unique ID
 * @returns Unique ID
 */
export function generateId(): string {
	return Math.random().toString(36).substr(2, 9);
}

/**
 * Debounce a function
 * @param func - The function to debounce
 * @param wait - The wait time in milliseconds
 * @param immediate - Whether to call the function immediately
 * @returns Debounced function
 */
export function debounce<T extends (...args: any[]) => any>(
	func: T,
	wait: number,
	immediate: boolean = false
): (...args: Parameters<T>) => void {
	let timeout: NodeJS.Timeout | null = null;
	return function executedFunction(...args: Parameters<T>) {
		const later = () => {
			timeout = null;
			if (!immediate) func(...args);
		};
		const callNow = immediate && !timeout;
		if (timeout) clearTimeout(timeout);
		timeout = setTimeout(later, wait);
		if (callNow) func(...args);
	};
}

/**
 * Throttle a function
 * @param func - The function to throttle
 * @param limit - The time limit in milliseconds
 * @returns Throttled function
 */
export function throttle<T extends (...args: any[]) => any>(
	func: T,
	limit: number
): (...args: Parameters<T>) => void {
	let inThrottle: boolean;
	return function (...args: Parameters<T>) {
		if (!inThrottle) {
			func.apply(this, args);
			inThrottle = true;
			setTimeout(() => (inThrottle = false), limit);
		}
	};
}

/**
 * Deep clone an object
 * @param obj - The object to clone
 * @returns Cloned object
 */
export function deepClone<T>(obj: T): T {
	if (obj === null || typeof obj !== 'object') {
		return obj;
	}

	if (obj instanceof Date) {
		return new Date(obj.getTime()) as T;
	}

	if (Array.isArray(obj)) {
		return obj.map((item) => deepClone(item)) as T;
	}

	const clonedObj = {} as T;
	for (const key in obj) {
		if (obj.hasOwnProperty(key)) {
			clonedObj[key] = deepClone(obj[key]);
		}
	}

	return clonedObj;
}

/**
 * Check if two objects are deeply equal
 * @param obj1 - First object
 * @param obj2 - Second object
 * @returns Whether objects are equal
 */
export function deepEqual(obj1: any, obj2: any): boolean {
	if (obj1 === obj2) return true;

	if (obj1 == null || obj2 == null) return false;

	if (typeof obj1 !== 'object' || typeof obj2 !== 'object') return false;

	const keys1 = Object.keys(obj1);
	const keys2 = Object.keys(obj2);

	if (keys1.length !== keys2.length) return false;

	for (const key of keys1) {
		if (!keys2.includes(key)) return false;
		if (!deepEqual(obj1[key], obj2[key])) return false;
	}

	return true;
}

/**
 * Capitalize the first letter of a string
 * @param str - The string to capitalize
 * @returns Capitalized string
 */
export function capitalize(str: string): string {
	if (!str) return str;
	return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
}

/**
 * Format currency
 * @param amount - The amount to format
 * @param currency - The currency code
 * @returns Formatted currency string
 */
export function formatCurrency(amount: number, currency: string = 'USD'): string {
	return new Intl.NumberFormat('en-US', {
		style: 'currency',
		currency: currency
	}).format(amount);
}

/**
 * Format phone number
 * @param phone - The phone number to format
 * @returns Formatted phone number
 */
export function formatPhone(phone: string): string {
	const cleaned = ('' + phone).replace(/\D/g, '');
	const match = cleaned.match(/^(\d{3})(\d{3})(\d{4})$/);

	if (match) {
		return '(' + match[1] + ') ' + match[2] + '-' + match[3];
	}

	return phone;
}

/**
 * Generate a random color
 * @returns Random hex color
 */
export function randomColor(): string {
	return '#' + Math.floor(Math.random() * 16777215).toString(16);
}

/**
 * Calculate the difference between two dates in days
 * @param date1 - First date
 * @param date2 - Second date
 * @returns Difference in days
 */
export function dateDiffInDays(date1: Date | string, date2: Date | string): number {
	const d1 = new Date(date1);
	const d2 = new Date(date2);
	const diffTime = Math.abs(d2.getTime() - d1.getTime());
	return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
}

/**
 * Check if a date is today
 * @param date - The date to check
 * @returns Whether the date is today
 */
export function isToday(date: Date | string): boolean {
	const d = new Date(date);
	const today = new Date();

	return (
		d.getDate() === today.getDate() &&
		d.getMonth() === today.getMonth() &&
		d.getFullYear() === today.getFullYear()
	);
}

/**
 * Get the start of the day for a given date
 * @param date - The date
 * @returns Start of the day
 */
export function startOfDay(date: Date | string): Date {
	const d = new Date(date);
	d.setHours(0, 0, 0, 0);
	return d;
}

/**
 * Get the end of the day for a given date
 * @param date - The date
 * @returns End of the day
 */
export function endOfDay(date: Date | string): Date {
	const d = new Date(date);
	d.setHours(23, 59, 59, 999);
	return d;
}

/**
 * Format bytes to human readable format
 * @param bytes - The number of bytes
 * @param decimals - Number of decimal places
 * @returns Formatted size string
 */
export function formatBytes(bytes: number, decimals: number = 2): string {
	if (bytes === 0) return '0 Bytes';

	const k = 1024;
	const dm = decimals < 0 ? 0 : decimals;
	const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

	const i = Math.floor(Math.log(bytes) / Math.log(k));

	return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}

/**
 * Check if a value is empty
 * @param value - The value to check
 * @returns Whether the value is empty
 */
export function isEmpty(value: any): boolean {
	if (value === null || value === undefined) return true;
	if (typeof value === 'string') return value.trim() === '';
	if (Array.isArray(value)) return value.length === 0;
	if (typeof value === 'object') return Object.keys(value).length === 0;
	return false;
}

/**
 * Get file extension from filename
 * @param filename - The filename
 * @returns File extension
 */
export function getFileExtension(filename: string): string {
	return filename.slice(((filename.lastIndexOf('.') - 1) >>> 0) + 2);
}

/**
 * Check if a string is a valid email
 * @param email - The email to validate
 * @returns Whether the email is valid
 */
export function isValidEmail(email: string): boolean {
	const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
	return emailRegex.test(email);
}

/**
 * Check if a string is a valid URL
 * @param url - The URL to validate
 * @returns Whether the URL is valid
 */
export function isValidUrl(url: string): boolean {
	try {
		new URL(url);
		return true;
	} catch (_) {
		return false;
	}
}

// Type definitions for common utility types
export type DateFormat = 'short' | 'medium' | 'long';
export type DateInput = Date | string;
export type CurrencyCode = string;
