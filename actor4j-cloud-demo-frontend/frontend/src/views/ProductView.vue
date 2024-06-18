<template>
	<div id="product_view">
		<v-row>
			<v-col></v-col>
			<v-col xs="12" md="6" lg="4">
				<v-card v-if="productLoaded">
					<v-card-text>
						<v-img height="400" :src="product.picture"/>
						<br>
						<h3>{{ product.name }}</h3>
						<p>{{ currency }} {{ price }}</p>
						
						<p>{{ product.description }}</p>
					</v-card-text>
					<v-divider></v-divider>					
					<v-card-actions>
						<v-container>
							<v-row>
								<v-col cols="12" sm="6">
									<v-btn block @click="addToCart(product.id);">Add to cart</v-btn>
								</v-col>
								<v-col cols="12" sm="6">
									<v-btn block @click="$router.push({name: 'Home'});">Keep browsing</v-btn>
								</v-col>
							</v-row>
						</v-container>
					</v-card-actions>
					<v-card-actions>
						
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
				<v-card hover outlined @click="$router.push({name: 'Product', params: { id: product.id }});load(product.id);">
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
	import { ProductCatalogService } from '@/services/ProductCatalogService';
	import { RecomendationService } from '@/services/RecomendationService';
	import { AdService } from '@/services/AdService';
	import { CartService } from '@/services/CartService';
	import LoginDialog from "@/dialogs/LoginDialog.vue";

	export default {
		name: 'ProductView',
		components: {
			LoginDialog: LoginDialog
		},
		props: {
		},
		data() {
			return {
				showLoginDialog: false,
			
				product: {},
				productLoaded: false,
				currency: 'USD',
				price: '',
				
				recommendedProducts: [],
				recommendedProductsLoaded: false,
				
				ad: ''
			}
		},
		created() {
			this.load(this.$route.params.id);
		},
		methods: {
			load(id) { 
				this.recommendedProductsLoaded = false;
				this.recommendedProducts = [];
			
				ProductCatalogService.get(id)
				.then(response => {
					this.product = response.data.data;
					this.price = this.product.price_usd.units.toString() + '.' + this.product.price_usd.nanos.toString().substr(0, 2);
					this.productLoaded = true;
				});
				
				var self = this;
				RecomendationService.getAll([id])
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
				
				AdService.randomAds()
				.then(response => {
					this.ad = response.data.data[0].text;
				});
			},
			addToCart(id) {
				let cartItem = {
					'product_id': id, 
					'quantity': 1
				};
			
				if (this.$store.state.user.auth==null) {
					let items = this.$store.state.user.cart_unauthenticated.items;
					let foundIndex = items.findIndex(item => cartItem.product_id === item.product_id);
					if (foundIndex>=0)
						this.$store.state.user.cart_unauthenticated.items[foundIndex].quantity += 1;
					else
						this.$store.state.user.cart_unauthenticated.items.push(cartItem);
						
					localStorage.setItem("user", JSON.stringify(this.$store.state.user));
						
					this.$router.push({name: 'Cart'});
				}
				else
					CartService.post(cartItem, this.$store.state.user.auth)
					.then( () => {
						this.$router.push({name: 'Cart'});
					})
					.catch(error => {
						console.log(error.status+":"+error.message);
						this.showLoginDialog = true;
						new Promise(resolve => {
							const stop = this.$watch('showLoginDialog', () => resolve(stop));
						})
						.then(() => {
							CartService.post(cartItem, this.$store.state.user.auth)
							.then( () => {
								this.$router.push({name: 'Cart'});
							});
					});
				});
			}
		},
		
	}
</script>

<style scoped>
#product_view {
	text-align: center;
}
</style>
