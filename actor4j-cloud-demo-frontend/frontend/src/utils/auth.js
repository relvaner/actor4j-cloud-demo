// true if the JWT has expired (or cannot be read), the signature is checked by the server
export function tokenExpired(token) {
	try {
		const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
		const claims = JSON.parse(atob(payload.padEnd(payload.length + (4 - payload.length % 4) % 4, '=')));
		return typeof claims.exp === 'number' && claims.exp * 1000 <= Date.now();
	}
	catch (e) {
		return true;
	}
}

// forgets the token, e.g. after a 401 (the server was restarted with another key)
export function clearLogin(user) {
	user.auth = null;
	user.prefered_username = null;
	localStorage.setItem("user", JSON.stringify(user));
}
