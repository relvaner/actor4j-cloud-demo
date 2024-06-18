import Vue from 'vue'
import Vuex from 'vuex'
import App from './App.vue'
import router from './router'
import vuetify from '@/plugins/vuetify'
import { v4 as uuidv4 } from 'uuid'

Vue.use(Vuex)

Vue.config.productionTip = false

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
	
	return user;
}

const store = new Vuex.Store({
	state: {
		user: loadUser(),
		order: null
	},
	mutations: {
	}
})

new Vue({
	vuetify,
	router,
	render: h => h(App),
	store
}).$mount('#app')
