<template>
	<div>
		<v-btn variant="text" class="mb-4 px-0" @click="$router.push({name: 'Home'})">
			<v-icon icon="fa-solid fa-arrow-left" size="x-small" class="mr-2"/>Back to shop
		</v-btn>

		<v-row v-if="!productLoaded && !notFound">
			<v-col cols="12" md="7"><v-skeleton-loader type="image" height="420" class="rounded-lg"/></v-col>
			<v-col cols="12" md="5"><v-skeleton-loader type="heading, paragraph, button"/></v-col>
		</v-row>

		<v-alert v-else-if="notFound" type="warning" variant="tonal">
			This product is not available.
		</v-alert>

		<v-row v-else class="align-start">
			<v-col cols="12" md="7">
				<div class="detail__image">
					<v-img cover :aspect-ratio="3/2" :src="product.picture" :alt="product.name"/>
				</div>
			</v-col>
			<v-col cols="12" md="5">
				<div class="detail__info">
					<div class="detail__chips">
						<v-chip v-for="c in product.categories" :key="c" size="small" variant="tonal" color="secondary" class="mr-2 text-capitalize">{{ c }}</v-chip>
					</div>
					<h1 class="detail__name">{{ product.name }}</h1>
					<div class="detail__price">{{ price }}</div>
					<p class="detail__description">{{ product.description }}</p>

					<div class="detail__buy">
						<div class="quantity">
							<v-btn icon variant="text" size="small" :disabled="quantity<=1" @click="quantity--" aria-label="less">
								<v-icon icon="fa-solid fa-minus" size="x-small"/>
							</v-btn>
							<span class="quantity__value">{{ quantity }}</span>
							<v-btn icon variant="text" size="small" :disabled="quantity>=10" @click="quantity++" aria-label="more">
								<v-icon icon="fa-solid fa-plus" size="x-small"/>
							</v-btn>
						</div>
						<v-btn color="primary" variant="flat" size="large" class="flex-grow-1" @click="addToCart(product.id)">
							<v-icon icon="fa-solid fa-bag-shopping" size="small" class="mr-2"/>Add to cart
						</v-btn>
					</div>

					<ul class="detail__facts">
						<li><v-icon icon="fa-solid fa-truck-fast" size="x-small" class="mr-3"/>Shipping cost calculated at checkout</li>
						<li><v-icon icon="fa-solid fa-rotate-left" size="x-small" class="mr-3"/>30 days return (demo)</li>
					</ul>
				</div>
			</v-col>
		</v-row>

		<div v-if="ad" class="ad">
			<span class="ad__label">Sponsored</span>
			<span>{{ ad }}</span>
		</div>

		<section v-if="recommendedProducts.length>0" class="mt-12">
			<h2 class="section-title mb-5">You might also like</h2>
			<v-row>
				<v-col cols="6" md="3" v-for="recommended in recommendedProducts" :key="recommended.id">
					<Product :product="recommended" compact/>
				</v-col>
			</v-row>
		</section>

		<LoginDialog :visible="showLoginDialog" @close="showLoginDialog=false"/>
	</div>
</template>

<script>
	import { ProductCatalogService } from '@/services/ProductCatalogService';
	import { RecomendationService } from '@/services/RecomendationService';
	import { AdService } from '@/services/AdService';
	import { CartService } from '@/services/CartService';
	import Product from '@/components/Product.vue';
	import LoginDialog from "@/dialogs/LoginDialog.vue";
	import { formatMoney } from '@/utils/money';
	import { mapStores } from 'pinia';
	import { useMainStore } from '@/stores/main';

	export default {
		name: 'ProductView',
		components: {
			Product: Product,
			LoginDialog: LoginDialog
		},
		data() {
			return {
				showLoginDialog: false,

				product: {},
				productLoaded: false,
				notFound: false,
				quantity: 1,

				recommendedProducts: [],
				ad: ''
			}
		},
		computed: {
			...mapStores(useMainStore),
			price() {
				return formatMoney(this.product.price_usd);
			}
		},
		watch: {
			// the component is reused between products (e.g. recommendations, back and forward)
			'$route.params.id'(id) {
				if (id)
					this.load(id);
			}
		},
		created() {
			this.load(this.$route.params.id);
		},
		methods: {
			load(id) {
				this.productLoaded = false;
				this.notFound = false;
				this.quantity = 1;
				this.recommendedProducts = [];
				this.ad = '';
				window.scrollTo({ top: 0 });

				ProductCatalogService.get(id)
				.then(response => {
					this.product = response.data.data;
					this.productLoaded = true;
				})
				.catch(() => {
					this.notFound = true;
				});

				RecomendationService.getAll([id])
				.then(response => Promise.all((response.data.data || []).map(productId => ProductCatalogService.get(productId))))
				.then(values => {
					// without duplicates (the recommendation may repeat ids)
					const seen = new Set();
					this.recommendedProducts = values.map(value => value.data.data).filter(p => p && !seen.has(p.id) && seen.add(p.id));
				})
				.catch(error => console.log(error.status+":"+error.message));

				AdService.randomAds()
				.then(response => {
					this.ad = response.data.data?.[0]?.text || '';
				})
				.catch(error => console.log(error.status+":"+error.message));
			},
			addToCart(id) {
				let cartItem = {
					'product_id': id,
					'quantity': this.quantity
				};

				if (this.mainStore.user.auth==null) {
					let items = this.mainStore.user.cart_unauthenticated.items;
					let foundIndex = items.findIndex(item => cartItem.product_id === item.product_id);
					if (foundIndex>=0)
						this.mainStore.user.cart_unauthenticated.items[foundIndex].quantity += cartItem.quantity;
					else
						this.mainStore.user.cart_unauthenticated.items.push(cartItem);

					localStorage.setItem("user", JSON.stringify(this.mainStore.user));

					this.$router.push({name: 'Cart'});
				}
				else
					CartService.post(cartItem, this.mainStore.user.auth)
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
							CartService.post(cartItem, this.mainStore.user.auth)
							.then( () => {
								this.$router.push({name: 'Cart'});
							})
							.catch(error => console.log(error.status+":"+error.message));
					});
				});
			}
		}
	}
</script>

<style scoped>
.detail__image {
	overflow: hidden;
	border-radius: 16px;
	background: #EFEAE2;
	box-shadow: 0 8px 28px rgba(43, 42, 40, 0.10);
}

.detail__info {
	padding: 8px 0 0 16px;
}

@media (max-width: 960px) {
	.detail__info {
		padding: 8px 0 0 0;
	}
}

.detail__chips {
	margin-bottom: 12px;
}

.detail__name {
	font-size: clamp(1.8rem, 3vw, 2.4rem);
	line-height: 1.15;
	font-weight: 700;
	letter-spacing: -0.02em;
	color: rgb(var(--v-theme-primary));
	margin-bottom: 8px;
}

.detail__price {
	font-size: 1.5rem;
	font-weight: 600;
	color: rgb(var(--v-theme-secondary));
	margin-bottom: 20px;
}

.detail__description {
	font-size: 1.05rem;
	line-height: 1.6;
	color: rgba(43, 42, 40, 0.75);
	margin-bottom: 28px !important;
}

.detail__buy {
	display: flex;
	gap: 12px;
	align-items: center;
}

.quantity {
	display: flex;
	align-items: center;
	border: 1px solid rgba(43, 42, 40, 0.2);
	border-radius: 10px;
	height: 44px;
	padding: 0 2px;
}

.quantity__value {
	min-width: 28px;
	text-align: center;
	font-weight: 600;
}

.detail__facts {
	list-style: none;
	padding: 0;
	margin-top: 28px;
	border-top: 1px solid rgba(43, 42, 40, 0.1);
}

.detail__facts li {
	padding: 12px 0;
	border-bottom: 1px solid rgba(43, 42, 40, 0.1);
	color: rgba(43, 42, 40, 0.75);
	font-size: 0.95rem;
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
