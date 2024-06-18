<template>
	<div id="app" data-app>
		<v-app-bar 
			dark
			prominent
			src="static/img/HeroBannerImage2.png">
			
			<v-container fluid>
				<v-row>
					<v-col cols="12" md="6">
						<v-toolbar-title >Actor4j Cloud  - Demo (Online Boutique)</v-toolbar-title>
					</v-col>
					<v-col cols="12" md="6" align="right">
						<v-select style="width:75px;display:inline-flex" v-model="defaultSelected" :items="items" ></v-select>
						<v-btn @click="$route.name!=='Home' && $router.push({name: 'Home'})" text>Home</v-btn>
						<v-btn @click="$route.name!=='Cart' && $router.push({name: 'Cart'})" text>Cart</v-btn>
						<v-btn v-if="loggedIn" @click="logout" text>Logout</v-btn>
						<v-btn v-if="!loggedIn" @click="showLogin" text>Login</v-btn>
					</v-col>
				</v-row>
				<v-row>
				</v-row>
				<v-row >
					<v-col v-if="welcomeUser!=null" cols="12" align="center">
						<v-toolbar-title>{{ welcomeUser }}</v-toolbar-title>	
					</v-col>
				</v-row>
			</v-container>
		</v-app-bar>

		<br>
		<v-container fluid>
			<router-view/>
			<LoginDialog :visible="showLoginDialog" @close="showLoginDialog=false"/>
		</v-container>
		<br>
					
		<v-footer padless id="footer">
			<v-col class="text-center" cols="12">
				{{ new Date().getFullYear() }} —
				<strong>actor4j.io</strong>
			</v-col>
		</v-footer>
	</div>
</template>

<script>
	import LoginDialog from "@/dialogs/LoginDialog.vue";
	export default {
		components: {
			LoginDialog: LoginDialog
		},
		data() {
			return  {
				loggedIn: false,
				welcomeUser: null,
				showLoginDialog: false,
			
				items: ['EUR', 'USD', 'JPY', 'GBP', 'TRY', 'CAD'],
				defaultSelected: 'USD'
			}
		},
		created() {
			this.loggedIn = this.$store.state.user.auth!=null;
			
			if (this.$store.state.user.prefered_username!=null)
				this.welcomeUser = ('Welcome ' + this.$store.state.user.prefered_username).toUpperCase();
		},
		methods: {
			showLogin() {
				this.showLoginDialog = true;
				new Promise(resolve => {
					const stop = this.$watch('showLoginDialog', () => resolve(stop));
				})
				.then(() => {
					this.$router.go(0);
				}); 
			},
			logout() {
				this.$store.state.user.auth=null;
				this.$store.state.user.prefered_username=null
				this.$store.state.user.cart_unauthenticated.items = [];
				this.$store.state.order=null;
				localStorage.setItem("user", JSON.stringify(this.$store.state.user));
				
				this.$router.go(0);
			}
		}
	}
</script>

<style>
#app {
	font-family: Avenir, Helvetica, Arial, sans-serif;
	-webkit-font-smoothing: antialiased;
	-moz-osx-font-smoothing: grayscale;
}

#footer {
	text-align: center;
}
</style>
