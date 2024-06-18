<template>
	<div id="ceckout_view">
		<v-row>
			<v-col></v-col>
			<v-col xs="12" md="6" lg="4">
				<v-card outlined v-if="checkoutLoaded">
					<v-card-text>
						<b><h1>Your order is complete!</h1></b>
						<br>
						<br>
						<h2>Order Confirmation ID</h2>
						<b><h2>{{ orderResult.order_id }}</h2></b>
						<br>
						<h2>Shipping Tracking ID</h2>
						<b><h2>{{ orderResult.shipping_tracking_id }}</h2></b>
						<br>
						<h2>Shipping Cost</h2>
						<b><h2>{{ currency }} {{ moneyAsString(orderResult.shipping_cost) }}</h2></b>
						<br>
						<h2>Total Paid</h2>
						<b><h2>{{ currency }} {{ moneyAsString(orderResult.total_cost) }}</h2></b>
					</v-card-text>
					<v-divider></v-divider>					
					<v-card-actions>
						<v-btn block @click="$router.push({name: 'Home'})">Keep Browsing</v-btn>
					</v-card-actions>
				</v-card>
			</v-col>
			<v-col></v-col>
		</v-row>
	</div>
</template>

<script>
import { CheckoutService } from '@/services/CheckoutService';

export default {
	name: 'CheckoutView',
	props: {
	},
	data() {
		return {
			currency: 'USD',
			orderResult: {},
			checkoutLoaded: false
		}
	},
	created() {
		this.checkout();
	},
	methods: {
		moneyAsString(money) {
				return money.units.toString() + '.' + money.nanos.toString().substr(0, 2);
			},
		checkout() {
			this.checkoutLoaded = false;
		
			if (this.$store.state.order !== null)
				CheckoutService.post(this.$store.state.order, this.$store.state.user.auth)
				.then(response => {
					this.orderResult = response.data.data;
					this.checkoutLoaded = true;
				})
				.catch(error => {
					console.log(error.status+":"+error.message+":"+error.response.data.data);
					this.$router.push({name: 'Cart'});
				});
		}
  }
}
</script>

<style scoped>
#ceckout_view {
	text-align: center;
}
</style>
