<template>
	<div>
		<h1 class="page-title">Your cart</h1>

		<v-row v-if="!cartLoaded">
			<v-col cols="12" md="8"><v-skeleton-loader type="list-item-avatar-two-line@3" class="rounded-lg"/></v-col>
			<v-col cols="12" md="4"><v-skeleton-loader type="card" class="rounded-lg"/></v-col>
		</v-row>

		<div v-else-if="isEmpty" class="empty">
			<v-icon icon="fa-solid fa-bag-shopping" size="40" class="empty__icon"/>
			<h2 class="empty__title">Your cart is empty</h2>
			<p class="muted">Discover our products and find something you like.</p>
			<v-btn color="primary" variant="flat" size="large" @click="$router.push({name: 'Home'})">Continue shopping</v-btn>
		</div>

		<v-row v-else class="align-start">
			<v-col cols="12" md="8">
				<section class="block">
					<h2 class="block__title">Items</h2>
					<CartItem v-for="item in cart.items" :key="item.product_id" :product="findProduct(item.product_id)" :quantity="item.quantity"/>
				</section>

				<section class="block">
					<h2 class="block__title">Checkout</h2>
					<v-form ref="checkoutForm">
						<div class="group">
							<div class="group__label"><v-icon icon="fa-regular fa-envelope" size="x-small" class="mr-2"/>Contact</div>
							<v-row>
								<v-col cols="12" sm="6">
									<v-text-field v-model="email" :rules="emailRules" label="E-mail address" required></v-text-field>
								</v-col>
							</v-row>
						</div>
						<div class="group">
							<div class="group__label"><v-icon icon="fa-solid fa-location-dot" size="x-small" class="mr-2"/>Shipping address</div>
							<v-row>
								<v-col cols="12" sm="8">
									<v-text-field v-model="address.street_address" :rules="nameRules" label="Street address" required></v-text-field>
								</v-col>
								<v-col cols="12" sm="4">
									<v-text-field v-model="address.zip_code" label="Zip code" required></v-text-field>
								</v-col>
								<v-col cols="12" sm="4">
									<v-text-field v-model="address.city" :rules="nameRules" label="City" required></v-text-field>
								</v-col>
								<v-col cols="12" sm="4">
									<v-text-field v-model="address.state" :rules="nameRules" label="State" required></v-text-field>
								</v-col>
								<v-col cols="12" sm="4">
									<v-text-field v-model="address.country" :rules="nameRules" label="Country" required></v-text-field>
								</v-col>
							</v-row>
						</div>
						<div class="group">
							<div class="group__label"><v-icon icon="fa-regular fa-credit-card" size="x-small" class="mr-2"/>Payment</div>
							<v-row>
								<v-col cols="12" sm="6">
									<v-text-field v-model="creditCard.credit_card_number" label="Card number" required></v-text-field>
								</v-col>
								<v-col cols="4" sm="2">
									<v-text-field v-model="creditCard.credit_card_expiration_month" label="Month" required></v-text-field>
								</v-col>
								<v-col cols="4" sm="2">
									<v-text-field v-model="creditCard.credit_card_expiration_year" label="Year" required></v-text-field>
								</v-col>
								<v-col cols="4" sm="2">
									<v-text-field v-model="creditCard.credit_card_cvv" label="CVV" required></v-text-field>
								</v-col>
							</v-row>
							<p class="muted hint"><v-icon icon="fa-solid fa-circle-info" size="x-small" class="mr-1"/>Demo shop: the card is only validated, nothing is charged.</p>
						</div>
					</v-form>
				</section>
			</v-col>

			<v-col cols="12" md="4">
				<aside class="summary">
					<h2 class="block__title">Summary</h2>
					<div class="summary__row"><span>Subtotal</span><span>{{ subtotal }}</span></div>
					<div class="summary__row"><span>Shipping</span><span>{{ costsLoaded ? shippingCost : '…' }}</span></div>
					<v-divider class="my-3"/>
					<div class="summary__row summary__total"><span>Total</span><span>{{ costsLoaded ? totalCost : '…' }}</span></div>
					<v-btn block color="primary" variant="flat" size="large" class="mt-5" @click="placeOrder()">
						<v-icon icon="fa-solid fa-lock" size="x-small" class="mr-2"/>Place order
					</v-btn>
					<div class="summary__actions">
						<v-btn variant="text" size="small" @click="$router.push({name: 'Home'})">Keep browsing</v-btn>
						<v-btn variant="text" size="small" color="error" @click="emptyCart()">Empty cart</v-btn>
					</div>
				</aside>
			</v-col>
		</v-row>

		<div v-if="ad" class="ad">
			<span class="ad__label">Sponsored</span>
			<span>{{ ad }}</span>
		</div>

		<section v-if="recommendedProducts.length>0" class="mt-12">
			<h2 class="section-title mb-5">You might also like</h2>
			<v-row>
				<v-col cols="6" md="3" v-for="product in recommendedProducts" :key="product.id">
					<Product :product="product" compact/>
				</v-col>
			</v-row>
		</section>

		<LoginDialog :visible="showLoginDialog" @close="showLoginDialog=false"/>
	</div>
</template>

<script>
	import CartItem from "@/components/CartItem.vue";
	import Product from "@/components/Product.vue";
	import LoginDialog from "@/dialogs/LoginDialog.vue";
	import { CartService } from '@/services/CartService';
	import { ProductCatalogService } from '@/services/ProductCatalogService';
	import { ShippingService } from '@/services/ShippingService';
	import { RecomendationService } from '@/services/RecomendationService';
	import { AdService } from '@/services/AdService';
	import { formatMoney } from '@/utils/money';
	import { mapStores } from 'pinia';
	import { useMainStore } from '@/stores/main';

	// amount as number -> MoneyDTO for formatMoney
	function money(currency_code, amount) {
		return { currency_code: currency_code, units: Math.trunc(amount), nanos: Math.round((amount % 1) * 1e9) };
	}

	export default {
		name: 'CartView',
		components: {
			CartItem: CartItem,
			Product: Product,
			LoginDialog: LoginDialog
		},
		data() {
			return {
				showLoginDialog: false,

				productCatalog: [],

				cart: {
					items: []
				},
				cartLoaded: false,
				currency: 'USD',

				costsLoaded: false,
				shippingCost: '',
				totalCost: '',

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
				ad: ''
			}
		},
		computed: {
			...mapStores(useMainStore),
			isEmpty() {
				return !this.cart || !this.cart.items || this.cart.items.length===0;
			},
			// products missing in the catalog are skipped
			subtotalNumber() {
				return (this.cart.items || []).reduce((sum, item) => {
					const product = this.findProduct(item.product_id);
					return product ? sum + (Number(product.price_usd.units) + Number(product.price_usd.nanos) / 1e9) * item.quantity : sum;
				}, 0);
			},
			subtotal() {
				return formatMoney(money(this.currency, this.subtotalNumber));
			}
		},
		created() {
			this.loadProductCatalog();
		},
		methods: {
			emptyCart() {
				if (this.mainStore.user.auth==null) {
					this.mainStore.user.cart_unauthenticated.items = [];

					localStorage.setItem("user", JSON.stringify(this.mainStore.user));

					this.$router.push({name: 'Home'});
				}
				else
					CartService.del(this.mainStore.user.auth)
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
							CartService.del(this.mainStore.user.auth)
							.then(() => {
								this.$router.push({name: 'Home'});
							})
							.catch(error => console.log(error.status+":"+error.message));
						});
					});
			},
			async placeOrder() {
				const { valid } = await this.$refs.checkoutForm.validate();
				if (valid) {
					if (this.mainStore.user.auth==null) {
						this.showLoginDialog = true;
						new Promise(resolve => {
							const stop = this.$watch('showLoginDialog', () => resolve(stop));
						})
						.then(() => {
							// cancelled, or the login failed
							if (this.mainStore.user.auth==null)
								return;
							// the login moved the local items to the server cart, the local cart is empty now
							CartService.get(this.mainStore.user.auth)
							.then(response => {
								this.cart = response.data.data;
								this.placeOrder_subroutine();
							})
							.catch(error => console.log(error.status+":"+error.message));
						});
					}
					else
						this.placeOrder_subroutine();
				}
			},
			placeOrder_subroutine() {
				if (this.cart.items.length>0) {
					this.mainStore.order = {
						'user_currency': this.currency,
						'address': this.address,
						'email': this.email,
						'credit_card': this.creditCard
					};

					this.$router.push({name: 'Checkout'});
				}
			},

			findProduct(id) {
				let product = Array.isArray(this.productCatalog) ? this.productCatalog.find(product => product.id === id) : undefined;

				return product !== undefined ? product : null;
			},
			calculateCosts() {
				this.costsLoaded = false;

				let shipOrder = {
					address: this.address,
					items: this.cart.items
				};
				ShippingService.quote(shipOrder)
					.then(response => {
						const shipping = response.data.data;
						this.shippingCost = formatMoney(shipping);
						this.totalCost = formatMoney(money(this.currency, this.subtotalNumber + Number(shipping.units) + Number(shipping.nanos) / 1e9));
						this.costsLoaded = true;
					})
					.catch(error => console.log(error.status+":"+error.message));
			},

			loadProductCatalog() {
				ProductCatalogService.getAll()
				.then(response => {
					this.productCatalog = response.data.data || [];
					this.loadCart(this.mainStore.user.auth);
				})
				.catch(error => console.log(error.status+":"+error.message));
			},
			loadCart(auth) {
				if (this.mainStore.user.auth==null) {
					this.cart = this.mainStore.user.cart_unauthenticated;
					this.cartLoaded = true;
					this.loadCartDetails();
				}
				else
					this.loadCart_authorized(auth);

				AdService.randomAds()
				.then(response => {
					this.ad = response.data.data?.[0]?.text || '';
				})
				.catch(error => console.log(error.status+":"+error.message));
			},
			loadCart_authorized(auth) {
				CartService.get(auth)
				.then(response => {
					this.cart = response.data.data;
					this.cartLoaded = true;
					this.loadCartDetails();
				})
				.catch(error => {
					console.log(error.status+":"+error.message);
					// token rejected (401): the APIService has logged out, show the local cart
					if (this.mainStore.user.auth==null)
						this.loadCart(null);
				});
			},
			loadCartDetails() {
				if (this.cart && this.cart.items !== undefined && this.cart.items.length>0) {
					this.loadRecommendations(this.cart.items.map(item => item.product_id));
					this.calculateCosts();
				}
			},
			loadRecommendations(ids) {
				this.recommendedProducts = [];

				RecomendationService.getAll(ids)
				.then(response => Promise.all((response.data.data || []).map(productId => ProductCatalogService.get(productId))))
				.then(values => {
					// without duplicates (the recommendation may repeat ids)
					const seen = new Set();
					this.recommendedProducts = values.map(value => value.data.data).filter(p => p && !seen.has(p.id) && seen.add(p.id));
				})
				.catch(error => console.log(error.status+":"+error.message));
			}
		}
	}
</script>

<style scoped>
.page-title {
	font-size: clamp(1.8rem, 3vw, 2.4rem);
	font-weight: 700;
	letter-spacing: -0.02em;
	color: rgb(var(--v-theme-primary));
	margin-bottom: 24px;
}

.block {
	background: #fff;
	border-radius: 16px;
	padding: 8px 24px 24px;
	margin-bottom: 24px;
	box-shadow: 0 1px 3px rgba(43, 42, 40, 0.06);
}

.block__title {
	font-size: 1.15rem;
	font-weight: 600;
	color: rgb(var(--v-theme-primary));
	padding-top: 16px;
	margin-bottom: 4px;
}

.group {
	margin-top: 20px;
}

.group__label {
	font-size: 0.8rem;
	letter-spacing: 0.08em;
	text-transform: uppercase;
	color: rgb(var(--v-theme-secondary));
	font-weight: 600;
	margin-bottom: 4px;
}

.hint {
	font-size: 0.85rem;
	margin-top: 8px;
}

.summary {
	position: sticky;
	top: 96px;
	background: #fff;
	border-radius: 16px;
	padding: 8px 24px 20px;
	box-shadow: 0 8px 28px rgba(43, 42, 40, 0.08);
}

.summary__row {
	display: flex;
	justify-content: space-between;
	padding: 6px 0;
	color: rgba(43, 42, 40, 0.75);
}

.summary__total {
	font-size: 1.2rem;
	font-weight: 700;
	color: rgb(var(--v-theme-primary));
}

.summary__actions {
	display: flex;
	justify-content: space-between;
	margin-top: 8px;
}

.empty {
	text-align: center;
	background: #fff;
	border-radius: 16px;
	padding: 56px 24px;
	box-shadow: 0 1px 3px rgba(43, 42, 40, 0.06);
}

.empty__icon {
	color: rgb(var(--v-theme-accent));
	margin-bottom: 16px;
}

.empty__title {
	font-size: 1.4rem;
	font-weight: 600;
	color: rgb(var(--v-theme-primary));
	margin-bottom: 8px;
}

.empty .v-btn {
	margin-top: 12px;
}

.ad {
	margin-top: 40px;
	padding: 16px 20px;
	border-radius: 12px;
	background: rgba(201, 169, 126, 0.16);
	color: rgb(var(--v-theme-primary));
	display: flex;
	gap: 16px;
	align-items: center;
}

.ad__label {
	font-size: 0.7rem;
	letter-spacing: 0.1em;
	text-transform: uppercase;
	color: rgb(var(--v-theme-secondary));
	font-weight: 600;
}
</style>
