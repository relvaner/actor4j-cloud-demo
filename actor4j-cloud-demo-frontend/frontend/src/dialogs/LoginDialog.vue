<template>
	<v-row class="justify-center"> 
	<v-dialog v-model="show" persistent max-width="600px">
		<v-card> 
			<v-card-title> 
				<span class="text-headline-small">Login required</span> 
			</v-card-title> 
			<v-card-text>
				<v-form ref="loginForm">
				<v-container> 
					<v-row>
						<v-col cols="12"> 
							<v-text-field :rules="usernameRules" v-model="username" label="Username*" required></v-text-field>
						</v-col> 
						<v-col cols="12"> 
							<v-text-field :rules="passwordRules" v-model="password" label="Password*" type="password" required></v-text-field> 
						</v-col> 
					</v-row> 
				</v-container> 
				</v-form>
				<small>*indicates required field</small> 
			</v-card-text> 
			<v-card-actions> 
				<v-spacer></v-spacer>
				<v-btn color="blue-darken-1" variant="text" @click="login">Ok</v-btn>
				<v-btn color="blue-darken-1" variant="text" @click="show=false;$router.push({name: 'Home'})">Cancel</v-btn>
			</v-card-actions> 
		</v-card> 
	</v-dialog> 
	</v-row>
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
				
				usernameRules: [
					v => !!v || 'Username is required',
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
		methods: {
			async login() {
				const { valid } = await this.$refs.loginForm.validate();
				if (valid) {
					let credentials = {
						username: this.mainStore.user.sid,
						password: this.password
					};
										
					AuthorizationService.post(credentials)
					.then(response => {
						this.mainStore.user.auth = response.data.data;
						this.mainStore.user.prefered_username = this.username;
						
						if (this.mainStore.user.cart_unauthenticated.items.length>0) {
							let cartItems = this.mainStore.user.cart_unauthenticated.items;
							
							var promises = [];
							for (let i = 0; i < cartItems.length; i++)
								promises.push(CartService.post(cartItems[i], this.mainStore.user.auth));
							Promise.all(promises).then(() => {
								this.mainStore.user.cart_unauthenticated.items = [];
								localStorage.setItem("user", JSON.stringify(this.mainStore.user));
								this.show=false;
							});
						}
						else {
							localStorage.setItem("user", JSON.stringify(this.mainStore.user));
							this.show=false;
						}
					})
					.catch(error => {
						console.log(error.status+":"+error.message);
						this.show=false;
						this.$router.push({name: 'Home'});
					});
				}
			}
		}
	}
</script>




