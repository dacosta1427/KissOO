export { matchers } from './matchers.js';

export const nodes = [
	() => import('./nodes/0'),
	() => import('./nodes/1'),
	() => import('./nodes/2'),
	() => import('./nodes/3'),
	() => import('./nodes/4'),
	() => import('./nodes/5'),
	() => import('./nodes/6'),
	() => import('./nodes/7'),
	() => import('./nodes/8'),
	() => import('./nodes/9'),
	() => import('./nodes/10'),
	() => import('./nodes/11'),
	() => import('./nodes/12'),
	() => import('./nodes/13'),
	() => import('./nodes/14'),
	() => import('./nodes/15'),
	() => import('./nodes/16'),
	() => import('./nodes/17'),
	() => import('./nodes/18'),
	() => import('./nodes/19'),
	() => import('./nodes/20'),
	() => import('./nodes/21'),
	() => import('./nodes/22'),
	() => import('./nodes/23'),
	() => import('./nodes/24'),
	() => import('./nodes/25'),
	() => import('./nodes/26')
];

export const server_loads = [];

export const dictionary = {
		"/": [2],
		"/benchmark": [3],
		"/bookings": [4],
		"/bookings/[bookingId]": [5],
		"/cleaners": [6],
		"/cleaners/[cleanerId]": [7],
		"/controls": [8],
		"/cost-profiles": [9],
		"/crud": [10],
		"/export": [11],
		"/file-upload": [12],
		"/houses": [13],
		"/houses/[houseId]": [14],
		"/login": [15],
		"/ollama": [16],
		"/owners": [17],
		"/owners/[id]": [18],
		"/report": [19],
		"/rest-services": [20],
		"/schedules": [21],
		"/schedules/[scheduleId]": [22],
		"/signup": [23],
		"/sql-access": [24],
		"/users": [25],
		"/verify-email": [26]
	};

export const hooks = {
	handleError: (({ error }) => { console.error(error) }),
	
	reroute: (() => {}),
	transport: {}
};

export const decoders = Object.fromEntries(Object.entries(hooks.transport).map(([k, v]) => [k, v.decode]));
export const encoders = Object.fromEntries(Object.entries(hooks.transport).map(([k, v]) => [k, v.encode]));

export const hash = false;

export const decode = (type, value) => decoders[type](value);

export { default as root } from '../root.js';