<template>
	<div class="checkout">
		<div v-if="!checkoutLoaded && !failed" class="card card--center">
			<v-progress-circular indeterminate color="secondary" size="48" width="4" class="mb-6"/>
			<h2 class="card__title">Placing your order…</h2>
			<p class="muted">Cart, catalog, currency, shipping, payment and e-mail are being processed by the pods.</p>
		</div>

		<div v-else-if="failed" class="card card--center">
			<v-icon icon="fa-solid fa-triangle-exclamation" size="40" class="mb-4 warn"/>
			<h2 class="card__title">The order could not be placed</h2>
			<p class="muted">{{ failure }}</p>
			<v-btn color="primary" variant="flat" size="large" class="mt-4" @click="$router.push({name: 'Cart'})">Back to cart</v-btn>
		</div>

		<div v-else class="card">
			<div class="card--center">
				<div class="check"><v-icon icon="fa-solid fa-check" size="28"/></div>
				<h1 class="card__headline">Thank you, your order is complete!</h1>
				<p class="muted">A confirmation has been sent by e-mail.</p>
			</div>

			<dl class="details">
				<div class="details__row">
					<dt>Order confirmation ID</dt>
					<dd class="mono">{{ orderResult.order_id }}</dd>
				</div>
				<div class="details__row">
					<dt>Shipping tracking ID</dt>
					<dd class="mono">{{ orderResult.shipping_tracking_id }}</dd>
				</div>
				<div class="details__row">
					<dt>Shipping cost</dt>
					<dd>{{ formatMoney(orderResult.shipping_cost) }}</dd>
				</div>
				<div class="details__row details__total">
					<dt>Total paid</dt>
					<dd>{{ formatMoney(orderResult.total_cost) }}</dd>
				</div>
			</dl>

			<v-btn block color="primary" variant="flat" size="large" @click="$router.push({name: 'Home'})">Keep browsing</v-btn>
		</div>
	</div>
</template>

<script>
import { CheckoutService } from '@/services/CheckoutService';
import { formatMoney } from '@/utils/money';
import { mapStores } from 'pinia';
import { useMainStore } from '@/stores/main';

export default {
	name: 'CheckoutView',
	data() {
		return {
			orderResult: {},
			checkoutLoaded: false,
			failed: false,
			failure: ''
		}
	},
	computed: {
		...mapStores(useMainStore)
	},
	created() {
		this.checkout();
	},
	methods: {
		formatMoney,
		checkout() {
			this.checkoutLoaded = false;

			// e.g. after a reload of this page there is no order anymore
			if (this.mainStore.order === null) {
				this.$router.replace({name: 'Cart'});
				return;
			}

			CheckoutService.post(this.mainStore.order, this.mainStore.user.auth)
			.then(response => {
				this.orderResult = response.data.data;
				this.checkoutLoaded = true;
				this.mainStore.order = null; // no second order with Back
			})
			.catch(error => {
				console.log(error.status+":"+error.message+":"+error.response?.data?.data);
				this.failure = typeof error.response?.data?.data === 'string' && error.response.data.data ? error.response.data.data : 'Please try again later.';
				this.failed = true;
			});
		}
	}
}
</script>

<style scoped>
.checkout {
	max-width: 640px;
	margin: 0 auto;
}

.card {
	background: #fff;
	border-radius: 16px;
	padding: 40px 32px 32px;
	box-shadow: 0 8px 28px rgba(43, 42, 40, 0.08);
}

.card--center {
	text-align: center;
}

.card__title {
	font-size: 1.4rem;
	font-weight: 600;
	color: rgb(var(--v-theme-primary));
	margin-bottom: 8px;
}

.card__headline {
	font-size: clamp(1.5rem, 3vw, 2rem);
	font-weight: 700;
	letter-spacing: -0.02em;
	color: rgb(var(--v-theme-primary));
	margin-bottom: 8px;
}

.check {
	width: 64px;
	height: 64px;
	margin: 0 auto 20px;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	background: rgba(78, 125, 91, 0.14);
	color: rgb(var(--v-theme-success));
}

.warn {
	color: rgb(var(--v-theme-warning));
}

.details {
	margin: 28px 0;
	border-top: 1px solid rgba(43, 42, 40, 0.1);
}

.details__row {
	display: flex;
	justify-content: space-between;
	gap: 16px;
	padding: 14px 0;
	border-bottom: 1px solid rgba(43, 42, 40, 0.1);
}

.details__row dt {
	color: rgba(43, 42, 40, 0.65);
}

.details__row dd {
	font-weight: 600;
	color: rgb(var(--v-theme-primary));
	text-align: right;
	word-break: break-all;
}

.details__total dd {
	font-size: 1.2rem;
}

.mono {
	font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
	font-size: 0.9rem;
}
</style>
