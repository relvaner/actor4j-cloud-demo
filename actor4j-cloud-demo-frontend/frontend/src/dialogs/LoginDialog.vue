<template>
	<div class="login-dialog">
	<v-dialog v-model="show" persistent max-width="440px">
		<v-card class="login" rounded="xl">
			<div class="login__head">
				<div class="login__icon"><v-icon icon="fa-regular fa-user" size="20"/></div>
				<h2 class="login__title">Sign in</h2>
				<p class="login__sub">to keep your cart and place your order</p>
			</div>

			<v-form ref="loginForm" @submit.prevent="login">
				<v-text-field v-model="username" :rules="usernameRules" label="Username" variant="outlined" density="comfortable"
					prepend-inner-icon="fa-regular fa-user" autocomplete="username" class="mb-2"></v-text-field>
				<v-text-field v-model="password" :rules="passwordRules" label="Password" type="password" variant="outlined" density="comfortable"
					prepend-inner-icon="fa-solid fa-lock" autocomplete="current-password"></v-text-field>

				<v-alert v-if="error" type="error" variant="tonal" density="compact" class="mb-4">{{ error }}</v-alert>

				<p class="login__hint"><v-icon icon="fa-solid fa-circle-info" size="x-small" class="mr-1"/>Demo: any username, any password with at least 8 characters. Each username has its own cart.</p>

				<v-btn type="submit" block color="primary" variant="flat" size="large" :loading="loading">Sign in</v-btn>
				<v-btn block variant="text" class="mt-2" :disabled="loading" @click="cancel">Cancel</v-btn>
			</v-form>
		</v-card>
	</v-dialog>
	</div>
</template>

<script>
	import { AuthorizationService } from '@/services/AuthorizationService';
	import { CartService } from '@/services/CartService';
	import { mapStores } from 'pinia';
	import { useMainStore } from '@/stores/main';

	export default {
		name: "LoginDialog",
		props: {
			visible: Boolean,
		},
		emits: ['close'],
		data() {
			return {
				username: '',
				password: '',
				loading: false,
				error: '',

				usernameRules: [
					v => !!v && !!v.trim() || 'Username is required',
					v => v.length <= 20 || 'Username must be less than 20 characters',
				],
				passwordRules: [
					v => !!v || 'Password is required',
					v => v.length >= 8 && v.length <= 20 || 'Password must consist of at least 8 characters and a maximum of 20 characters',
				],
			}
		},
		computed: {
			...mapStores(useMainStore),
			show: {
				get() {
					return this.visible;
				},
				set(value) {
					if (!value)
						this.$emit('close');
				}
			}
		},
		watch: {
			visible(value) {
				if (value) {
					this.error = '';
					this.loading = false;
				}
			}
		},
		methods: {
			cancel() {
				this.show=false;
				this.$router.push({name: 'Home'});
			},
			async login() {
				const { valid } = await this.$refs.loginForm.validate();
				if (!valid)
					return;

				this.loading = true;
				this.error = '';

				// the username is the identity (and so the cart) of the user, not the browser
				let credentials = {
					username: this.username.trim().toLowerCase(),
					password: this.password
				};

				try {
					const response = await AuthorizationService.post(credentials);
					const auth = response.data.data;

					// items of the cart before the login move to the server cart, each one only once
					let cartItems = [...this.mainStore.user.cart_unauthenticated.items];
					for (const item of cartItems) {
						await CartService.post(item, auth);
						this.mainStore.user.cart_unauthenticated.items = this.mainStore.user.cart_unauthenticated.items.filter(i => i !== item);
					}

					this.mainStore.user.auth = auth;
					this.mainStore.user.prefered_username = this.username.trim();
					localStorage.setItem("user", JSON.stringify(this.mainStore.user));
					this.password = '';
					this.show=false;
				}
				catch (error) {
					console.log(error.status+":"+error.message);
					localStorage.setItem("user", JSON.stringify(this.mainStore.user));
					this.error = error.status===400 && typeof error.response?.data?.data === 'string' ? error.response.data.data : 'Sign in failed, please try again.';
				}
				finally {
					this.loading = false;
				}
			}
		}
	}
</script>

<style scoped>
.login {
	padding: 32px 28px 24px;
}

.login__head {
	text-align: center;
	margin-bottom: 24px;
}

.login__icon {
	width: 48px;
	height: 48px;
	margin: 0 auto 12px;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	background: rgba(201, 169, 126, 0.2);
	color: rgb(var(--v-theme-secondary));
}

.login__title {
	font-size: 1.5rem;
	font-weight: 700;
	letter-spacing: -0.01em;
	color: rgb(var(--v-theme-primary));
	margin-bottom: 4px;
}

.login__sub {
	color: rgba(43, 42, 40, 0.65);
	margin-bottom: 0 !important;
}

.login__hint {
	font-size: 0.8rem;
	color: rgba(43, 42, 40, 0.6);
	margin: 4px 0 16px !important;
}
</style>
