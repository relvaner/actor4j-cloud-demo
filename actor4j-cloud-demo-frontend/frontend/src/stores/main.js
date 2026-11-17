import { defineStore } from 'pinia'
import { v4 as uuidv4 } from 'uuid'
import { tokenExpired, clearLogin } from '@/utils/auth'

function loadUser() {
	var user = JSON.parse(localStorage.getItem("user"));
	
	if (user === null) {
		user = {
			sid: uuidv4(),
			auth: null,
			prefered_username: null,
			cart_unauthenticated: {
				items: []
			},
		}
		localStorage.setItem("user", JSON.stringify(user));
	}
	else if (user.auth && tokenExpired(user.auth))
		clearLogin(user);

	return user;
}

export const useMainStore = defineStore('main', {
	state: () => ({
		user: loadUser(),
		order: null
	})
})
