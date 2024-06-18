<template>
	<div id="cart_item">
		<v-card outlined>
			<v-card-text v-if="product !== null">
				<v-container>
					<v-row>
						<v-col xs="12" sm="6">
							<v-img contain :src="product.picture" @click="$router.push({name: 'Product', params: { id: product.id }})"/>
						</v-col>
						<v-col xs="12" sm="6">
							<br>
							<h3>{{ product.name }}</h3>
							<p>SKU: #{{ product.id }}</p>
							<br>
							<p>Quantity: {{ quantity }}</p>
							<p>{{ currency }} {{ price }}</p>
						</v-col>
					</v-row>	
				</v-container>		
			</v-card-text>
		</v-card>
	</div>
</template>

<script>
export default {
	name: 'CartItem',
	props: {
		product: Object,
		quantity: Number
	},
	data() {
		return {
			currency: 'USD',
			price: ''
		}
	},
	created() {
		if (this.product !== null)
			this.price = this.product.price_usd.units.toString() + '.' + this.product.price_usd.nanos.toString().substr(0, 2);
	},
	methods: {
  }
}
</script>

<style scoped>
#cart_item {
	text-align: center;
}
</style>
