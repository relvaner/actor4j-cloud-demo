<template>
	<div id="cart_view">
		<v-row v-for="item in cart.items" v-bind:key="item.product_id">
			<v-col></v-col>
			<v-col xs="12" md="6" lg="4">
				<CartItem :product="findProduct(item.product_id)" :quantity="item.quantity"></CartItem>
			</v-col>
			<v-col></v-col>
		</v-row>
		
		<v-row>
			<v-col></v-col>
			<v-col cols="12" md="6" lg="4">
				<v-card outlined v-if="costsLoaded">
					<v-card-text>
						<h3>Shipping Cost: {{ currency }} {{ shippingCost }}</h3>
						<br>
						<h2>Total Cost: {{ currency }} {{ totalCost }}</h2>
					</v-card-text>
					<v-divider></v-divider>	
					<v-card-actions>
						<v-container>
							<v-row>
								<v-col cols="12" sm="6">
									<v-btn block @click="emptyCart();">Empty Cart</v-btn>
								</v-col>
								<v-col cols="12" sm="6">
									<v-btn block @click="$router.push({name: 'Home'});">Keep browsing</v-btn>
								</v-col>
							</v-row>
						</v-container>
					</v-card-actions>
				</v-card>
			</v-col>
			<v-col></v-col>
		</v-row>
		
		<v-row>
			<v-col></v-col>
			<v-col cols="12" md="6" lg="4">
				<v-card outlined>
					<v-card-text>
						<h2>Checkout</h2>
					</v-card-text>
					<v-form ref="checkoutForm">
						<v-container>
							<v-row>
								<v-col cols="12" sm="4">
									<v-text-field v-model="email" :rules="emailRules" :counter="20" label="E-mail Address" required></v-text-field>
								</v-col>
								<v-col cols="12" sm="4">
									<v-text-field v-model="address.street_address" :rules="nameRules" :counter="20" label="Street Address" required></v-text-field>
								</v-col>
								<v-col cols="12" sm="4">
									<v-text-field v-model="address.zip_code" :counter="10" label="Zip Code" required></v-text-field>
								</v-col>
							</v-row>
							<v-row>
								<v-col cols="12" sm="4">
									<v-text-field v-model="address.city" :rules="nameRules" :counter="20" label="City" required></v-text-field>
								</v-col>
								<v-col cols="12" sm="4">
									<v-text-field v-model="address.state" :rules="nameRules" :counter="20" label="State" required></v-text-field>
								</v-col>
								<v-col cols="12" sm="4">
									<v-text-field v-model="address.country" :rules="nameRules" :counter="20" label="Country" required></v-text-field>
								</v-col>
							</v-row>
							<v-row>
								<v-col cols="12" sm="3">
									<v-text-field v-model="creditCard.credit_card_number" :counter="16" label="Credit Card Number" required></v-text-field>
								</v-col>
								<v-col cols="12" sm="3">
									<v-text-field v-model="creditCard.credit_card_expiration_month" :counter="2" label="Month" required></v-text-field>
								</v-col>
								<v-col cols="12" sm="3">
									<v-text-field v-model="creditCard.credit_card_expiration_year" :counter="4" label="Year" required></v-text-field>
								</v-col>
								<v-col cols="12" sm="3">
									<v-text-field v-model="creditCard.credit_card_cvv" :counter="3" label="CVV" required></v-text-field>
								</v-col>
							</v-row>
						</v-container>
					</v-form>
					<v-divider></v-divider>					
					<v-card-actions>
						<v-btn block @click="placeOrder()">Place Order</v-btn>
					</v-card-actions>
				</v-card>
			</v-col>
			<v-col></v-col>
		</v-row>
		
		<v-row>
			<v-col><br><h2>You might like</h2></v-col>
		</v-row>
		
		<v-row v-if="recommendedProductsLoaded">
			<v-col xs="12" sm="6" md="3" v-for="product in recommendedProducts" v-bind:key="product.id">
				<v-card hover outlined @click="$router.push({name: 'Product', params: { id: product.id }});">
					<v-card-text>
						<v-img contain height="100" :src="product.picture"/>
						<br>
						<h3>{{ product.name }}</h3>	
					</v-card-text>
				</v-card>
			</v-col>
		</v-row>
		
		<v-row>
			<v-col><br><p>Advertisement: {{ ad }}</p></v-col>
		</v-row>
		
		<LoginDialog :visible="showLoginDialog" @close="showLoginDialog=false"/>
	</div>
</template>

<script>
	import CartItem from "@/components/CartItem.vue";
	import LoginDialog from "@/dialogs/LoginDialog.vue";
	import { CartService } from '@/services/CartService';
	import { ProductCatalogService } from '@/services/ProductCatalogService';
	import { ShippingService } from '@/services/ShippingService';
	import { RecomendationService } from '@/services/RecomendationService';
	import { AdService } from '@/services/AdService';

	export default {
		name: 'CartView',
		components: {
			CartItem: CartItem,
			LoginDialog: LoginDialog
		},
		props: {
		},
		data() {
			return {
				showLoginDialog: false,
			
				productCatalog: {},
			
				cart: {
					items: []
				},
				cartLoaded: false,
				currency: 'USD',
				
				costsLoaded: false,
				shippingCost: '0.00',
				totalCost: '0.00',
				totalCostNumber: 0,
				
				address: {
					street_address: 'Platz der Republik 1',
					city: 'Berlin',
					state: 'Berlin',
					country: 'Germany',
					zip_code: '11011',
				},
				email: 'me@example.de',
				creditCard: {
					credit_card_number: '4242424242424242', 
					credit_card_cvv: 111,
					credit_card_expiration_year: 2099,
					credit_card_expiration_month: 12
				},
				
				nameRules: [
					v => !!v || 'Name is required',
					v => v.length <= 20 || 'Name must be less than 20 characters',
				],
				emailRules: [
					v => !!v || 'E-mail is required',
					v => /.+@.+/.test(v) || 'E-mail must be valid',
				],
				
				recommendedProducts: [],
				recommendedProductsLoaded: false,
				
				ad: ''
			}
		},
		created() {
			this.initialize();
		},
		methods: {
			emptyCart() {
				if (this.$store.state.user.auth==null) {
					this.$store.state.user.cart_unauthenticated.items = [];
					
					localStorage.setItem("user", JSON.stringify(this.$store.state.user));
					
					this.$router.push({name: 'Home'});
				}
				else
					CartService.del(this.$store.state.user.auth)
					.then(() => {
						this.$router.push({name: 'Home'});
					})
					.catch(error => {
						console.log(error.status+":"+error.message);
						this.showLoginDialog = true;
						new Promise(resolve => {
							const stop = this.$watch('showLoginDialog', () => resolve(stop));
						})
						.then(() => {
							CartService.del(this.$store.state.user.auth)
							.then(() => {
								this.$router.push({name: 'Home'});
							})
						});
					});		
			},
			placeOrder() {
				if (this.$refs.checkoutForm.validate()) {
					if (this.$store.state.user.auth==null) {
						this.showLoginDialog = true;
						new Promise(resolve => {
							const stop = this.$watch('showLoginDialog', () => resolve(stop));
						})
						.then(() => {
							this.placeOrder_subroutine();
						});
					}
					else
						this.placeOrder_subroutine();
				}
			},
			placeOrder_subroutine() {
				if (this.cart.items.length>0) {
					this.$store.state.order = {
						'user_currency': this.currency,
						'address': this.address,
						'email': this.email,
						'credit_card': this.creditCard
					};
				
					this.$router.push({name: 'Checkout'});
				}
			},
		
			findProduct(id) {
				let product = this.productCatalog.find(product => product.id === id);
			
				return product !== undefined ? product : null;
			},
			moneyAsString(money) {
				return money.units.toString() + '.' + money.nanos.toString().substr(0, 2);
			},
			calculateCosts() {
				this.costsLoaded = false;
			
				let shipOrder = {
					address: this.address,
					items: this.cart.items
				};
				ShippingService.quote(shipOrder)
					.then(response => {
						for (let i=0; i<this.cart.items.length; i++) {
							let item = this.cart.items[i];
							let product  = this.productCatalog.find(product => product.id === item.product_id);
							this.totalCostNumber += product.price_usd.units*item.quantity + product.price_usd.nanos*1.0/1000000000*item.quantity;
						}
						this.shippingCost = this.moneyAsString(response.data.data);
						this.totalCostNumber += parseFloat(this.shippingCost);
						this.totalCost = new Intl.NumberFormat('en-US').format(this.totalCostNumber).toString();
						this.costsLoaded = true;
					});
			},
			
			initialize() {
				this.totalCost = '0.00';
				this.totalCostNumber = 0;
				this.loadProductCatalog();
			},
			loadProductCatalog() {
				ProductCatalogService.getAll()
				.then(response => {
					this.productCatalog = response.data.data;
					this.loadCart(this.$store.state.user.auth);
				});
			},
			loadCart(auth) {
				if (this.$store.state.user.auth==null) {
					this.cart = this.$store.state.user.cart_unauthenticated;
					this.cartLoaded = true;
					
					if (this.cart.items !== undefined) {
						var productIds = [];
						for (let i = 0; i < this.cart.items.length; i++)
							productIds.push(this.cart.items[i].product_id);
						this.loadRecommendations(productIds);

						this.calculateCosts();
					}
				}
				else
					this.loadCart_authorized(auth);
				
				AdService.randomAds()
				.then(response => {
					this.ad = response.data.data[0].text;
				});
			},
			loadCart_authorized(auth) {
				CartService.get(auth)
				.then(response => {
					this.cart = response.data.data;
					this.cartLoaded = true;
					
					if (this.cart.items !== undefined) {
						var productIds = [];
						for (let i = 0; i < this.cart.items.length; i++)
							productIds.push(this.cart.items[i].product_id);
						this.loadRecommendations(productIds);

						this.calculateCosts();
					}
				})
				.catch(error => {
					console.log(error.status+":"+error.message);
					this.showLoginDialog = true;
					new Promise(resolve => {
						const stop = this.$watch('showLoginDialog', () => resolve(stop));
					})
					.then(() => {
						this.$router.go(0); // this.$router.push({name: 'Cart'});
					});
				});
			},
			loadRecommendations(ids) { 
				this.recommendedProductsLoaded = false;
				this.recommendedProducts = [];
				
				var self = this;
				RecomendationService.getAll(ids)
				.then(response => {
					var productIds = response.data.data;
					
					var promises = [];
					for (let i = 0; i < productIds.length; i++)
						promises.push(ProductCatalogService.get(productIds[i]));
					Promise.all(promises).then((values) => {
						for (let j = 0; j < values.length; j++)
							self.recommendedProducts.push(values[j].data.data);
						self.recommendedProductsLoaded = true;
					});
				});
			}
		}
	}
</script>

<style scoped>
#cart_view {
	text-align: center;
}
</style>
