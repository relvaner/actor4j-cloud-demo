<template>
	<v-app id="app">
		<v-app-bar flat color="surface" class="app-bar" height="72">
			<div class="page app-bar__inner">
				<a class="brand" href="#/" @click.prevent="$route.name!=='Home' && $router.push({name: 'Home'})">
					<svg class="brand__mark" viewBox="0 0 32 32" aria-hidden="true">
						<rect x="2" y="2" width="28" height="28" rx="8" fill="currentColor"/>
						<path d="M10 22 L16 9 L22 22 M12.5 17 H19.5" stroke="#FAF7F2" stroke-width="2.4" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
					</svg>
					<span class="brand__text">
						<span class="brand__name">Actor4j Boutique</span>
						<span class="brand__sub">Cloud Demo</span>
					</span>
				</a>

				<v-spacer/>

				<nav class="nav">
					<v-btn variant="text" :class="{ 'nav--active': $route.name==='Home' || $route.name==='Product' }" @click="$route.name!=='Home' && $router.push({name: 'Home'})"><v-icon icon="fa-solid fa-store" size="small" class="mr-2"/>Shop</v-btn>
					<v-btn variant="text" :class="{ 'nav--active': $route.name==='Cart' || $route.name==='Checkout' }" @click="$route.name!=='Cart' && $router.push({name: 'Cart'})">
						<v-icon icon="fa-solid fa-bag-shopping" size="small" class="mr-2"/>
						Cart
						<span v-if="cartCount>0" class="nav__badge">{{ cartCount }}</span>
					</v-btn>
					<span v-if="loggedIn && userName" class="nav__user">Hi, {{ userName }}</span>
					<v-btn v-if="loggedIn" variant="outlined" class="ml-2" @click="logout"><v-icon icon="fa-solid fa-arrow-right-from-bracket" size="small" class="mr-2"/>Logout</v-btn>
					<v-btn v-else variant="flat" color="primary" class="ml-2" @click="showLogin"><v-icon icon="fa-regular fa-user" size="small" class="mr-2"/>Login</v-btn>
				</nav>
			</div>
		</v-app-bar>

		<v-main>
			<router-view v-slot="{ Component, route }">
				<div :class="route.name==='Home' ? 'pb-8' : 'page pt-8 pb-16'">
					<component :is="Component"/>
				</div>
			</router-view>
			<LoginDialog :visible="showLoginDialog" @close="showLoginDialog=false"/>
		</v-main>

		<v-footer class="footer">
			<div class="page footer__inner">
				<span>© {{ new Date().getFullYear() }} <strong>Actor4j Boutique</strong>, a demo of actor4j</span>
				<span class="footer__links">
					<a href="https://actor4j.io" target="_blank" rel="noopener">actor4j.io</a>
					<a href="https://github.com/relvaner/actor4j-cloud-demo" target="_blank" rel="noopener"><i class="fa-brands fa-github"></i> GitHub</a>
				</span>
			</div>
		</v-footer>
	</v-app>
</template>

<script>
	import { mapStores } from "pinia";
	import LoginDialog from "@/dialogs/LoginDialog.vue";
	import { useMainStore } from "@/stores/main";
	export default {
		components: {
			LoginDialog: LoginDialog
		},
		data() {
			return  {
				showLoginDialog: false
			}
		},
		computed: {
			...mapStores(useMainStore),
			loggedIn() {
				return this.mainStore.user.auth!=null;
			},
			userName() {
				return this.mainStore.user.prefered_username;
			},
			// only known locally before the login, afterwards the cart is on the server
			cartCount() {
				if (this.loggedIn)
					return 0;
				return this.mainStore.user.cart_unauthenticated.items.reduce((sum, item) => sum + item.quantity, 0);
			}
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
				this.mainStore.user.auth=null;
				this.mainStore.user.prefered_username=null
				this.mainStore.user.cart_unauthenticated.items = [];
				this.mainStore.order=null;
				localStorage.setItem("user", JSON.stringify(this.mainStore.user));

				this.$router.go(0);
			}
		}
	}
</script>

<style>
.app-bar {
	border-bottom: 1px solid rgba(43, 42, 40, 0.08) !important;
}

.app-bar__inner {
	display: flex;
	align-items: center;
	width: 100%;
}

.brand {
	display: flex;
	align-items: center;
	gap: 12px;
	text-decoration: none;
	color: rgb(var(--v-theme-primary));
}

.brand__mark {
	width: 34px;
	height: 34px;
}

.brand__text {
	display: flex;
	flex-direction: column;
	line-height: 1.1;
}

.brand__name {
	font-size: 1.15rem;
	font-weight: 700;
	letter-spacing: -0.01em;
}

.brand__sub {
	font-size: 0.75rem;
	letter-spacing: 0.08em;
	text-transform: uppercase;
	color: rgb(var(--v-theme-secondary));
}

.nav {
	display: flex;
	align-items: center;
	gap: 4px;
}

.nav--active {
	background: rgba(201, 169, 126, 0.18);
}

.nav__badge {
	margin-left: 6px;
	min-width: 20px;
	height: 20px;
	padding: 0 6px;
	border-radius: 10px;
	background: rgb(var(--v-theme-secondary));
	color: #fff;
	font-size: 0.75rem;
	line-height: 20px;
}

.nav__user {
	margin-left: 8px;
	font-size: 0.9rem;
	color: rgba(var(--v-theme-on-surface), 0.7);
}

@media (max-width: 600px) {
	.brand__text, .nav__user {
		display: none;
	}
}

.footer {
	background: #2B2A28 !important;
	color: rgba(250, 247, 242, 0.8) !important;
	padding: 28px 0 !important;
	flex: 0 0 auto;
}

.footer__inner {
	display: flex;
	justify-content: space-between;
	flex-wrap: wrap;
	gap: 12px;
	width: 100%;
	font-size: 0.9rem;
}

.footer__links a {
	color: rgba(250, 247, 242, 0.8);
	text-decoration: none;
	margin-left: 20px;
}

.footer__links a:hover {
	color: #fff;
}
</style>
