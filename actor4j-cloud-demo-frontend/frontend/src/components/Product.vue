<template>
	<div id="product">
		<v-card hover outlined @click="$router.push({name: 'Product', params: { id: product.id }})">
			<v-card-text>
				<v-img height="400" :src="product.picture"/>
				<br>
				<h3>{{ product.name }}</h3>
				<p>{{ currency }} {{ price }}</p>
			</v-card-text>
		</v-card>
	</div>
</template>

<script>
export default {
	name: 'Product',
	props: {
		product: Object
	},
	data() {
		return {
			currency: 'USD',
			price: ''
		}
	},
	created() {
		this.price = this.product.price_usd.units.toString() + '.' + this.product.price_usd.nanos.toString().substr(0, 2);
	},
	methods: {
  }
}
</script>

<style scoped>
#product {
	text-align: center;
}
</style>
