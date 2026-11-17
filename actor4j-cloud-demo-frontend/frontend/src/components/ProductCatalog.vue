<template>
	<div>
		<div class="catalog__head">
			<h2 class="section-title">Products</h2>
			<v-chip-group v-model="category" selected-class="chip--active" mandatory>
				<v-chip value="all" variant="outlined" size="small">All</v-chip>
				<v-chip v-for="c in categories" :key="c" :value="c" variant="outlined" size="small" class="text-capitalize">{{ c }}</v-chip>
			</v-chip-group>
		</div>

		<v-row v-if="loading">
			<v-col cols="12" sm="6" md="4" lg="3" v-for="n in 8" :key="n">
				<v-skeleton-loader type="image, list-item-two-line" class="rounded-lg"/>
			</v-col>
		</v-row>

		<v-alert v-else-if="error" type="warning" variant="tonal" class="mt-4">
			The products could not be loaded. Please try again later.
		</v-alert>

		<v-row v-else>
			<v-col cols="12" sm="6" md="4" lg="3" v-for="product in filtered" :key="product.id">
				<Product :product="product"/>
			</v-col>
		</v-row>
	</div>
</template>

<script>
import Product from "@/components/Product.vue";
import { ProductCatalogService } from '@/services/ProductCatalogService';

export default {
	name: 'ProductCatalog',
	components: {
		Product: Product
	},
	data() {
		return {
			productCatalog: [],
			category: 'all',
			loading: true,
			error: false
		}
	},
	computed: {
		categories() {
			return [...new Set(this.productCatalog.flatMap(product => product.categories || []))].sort();
		},
		filtered() {
			if (this.category==='all')
				return this.productCatalog;
			return this.productCatalog.filter(product => (product.categories || []).includes(this.category));
		}
	},
	created() {
		ProductCatalogService.getAll()
			.then(response => {
				this.productCatalog = response.data.data || [];
			})
			.catch(error => {
				console.log(error.status+":"+error.message);
				this.error = true;
			})
			.finally(() => {
				this.loading = false;
			});
	}
}
</script>

<style scoped>
.catalog__head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	flex-wrap: wrap;
	gap: 12px;
	margin-bottom: 20px;
}

.chip--active {
	background: rgb(var(--v-theme-primary)) !important;
	color: #fff !important;
}
</style>
