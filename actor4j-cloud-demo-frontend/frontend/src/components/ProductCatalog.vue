<template>
  <div>
	<v-row>
		<v-col xs="12" sm="6" lg="4" v-for="product in productCatalog" v-bind:key="product.id">
			<Product :product="product"></Product>
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
	props: {
	},
	data() {
		return {
			productCatalog: {}
		}
	},
	created() {
		ProductCatalogService.getAll()
			.then(response => {
				this.productCatalog = response.data.data;
			})
			.catch(error => {
				console.log(error.status+":"+error.message);
			});
	},
	methods: {
	}
}
</script>

<style scoped>
</style>
