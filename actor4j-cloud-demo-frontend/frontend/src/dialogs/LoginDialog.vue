<template>
	<v-row justify="center"> 
	<v-dialog v-model="show" persistent max-width="600px">
		<v-card> 
			<v-card-title> 
				<span class="text-h5">Login required</span> 
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
				<v-btn color="blue darken-1" text @click="login">Ok</v-btn>
				<v-btn color="blue darken-1" text @click="show=false;$router.push({name: 'Home'})">Cancel</v-btn>
			</v-card-actions> 
		</v-card> 
	</v-dialog> 
	</v-row>
</template>

<script>
	import { AuthorizationService } from '@/services/AuthorizationService';
	import { CartService } from '@/services/CartService';
	
	export default {
		name: "LoginDialog",
		props: {
			visible: Boolean,
		},
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
			login() {
				if (this.$refs.loginForm.validate()) {
					let credentials = {
						username: this.$store.state.user.sid,
						password: this.password
					};
										
					AuthorizationService.post(credentials)
					.then(response => {
						this.$store.state.user.auth = response.data.data;
						this.$store.state.user.prefered_username = this.username;
						
						if (this.$store.state.user.cart_unauthenticated.items.length>0) {
							let cartItems = this.$store.state.user.cart_unauthenticated.items;
							
							var promises = [];
							for (let i = 0; i < cartItems.length; i++)
								promises.push(CartService.post(cartItems[i], this.$store.state.user.auth));
							Promise.all(promises).then(() => {
								this.$store.state.user.cart_unauthenticated.items = [];
								localStorage.setItem("user", JSON.stringify(this.$store.state.user));
								this.show=false;
							});
						}
						else {
							localStorage.setItem("user", JSON.stringify(this.$store.state.user));
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




