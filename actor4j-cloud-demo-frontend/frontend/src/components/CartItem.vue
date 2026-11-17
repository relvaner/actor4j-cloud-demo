<template>
	<div class="cart-item" v-if="product !== null">
		<div class="cart-item__image" @click="$router.push({name: 'Product', params: { id: product.id }})">
			<v-img cover :aspect-ratio="1" :src="product.picture" :alt="product.name"/>
		</div>
		<div class="cart-item__info">
			<div class="cart-item__name" @click="$router.push({name: 'Product', params: { id: product.id }})">{{ product.name }}</div>
			<div class="cart-item__meta">SKU #{{ product.id }}</div>
			<div class="cart-item__meta">Quantity: {{ quantity }} × {{ unitPrice }}</div>
		</div>
		<div class="cart-item__price">{{ linePrice }}</div>
	</div>
</template>

<script>
import { formatMoney } from '@/utils/money';

export default {
	name: 'CartItem',
	props: {
		product: Object,
		quantity: Number
	},
	computed: {
		unitPrice() {
			return formatMoney(this.product.price_usd);
		},
		linePrice() {
			const price = this.product.price_usd;
			const amount = (Number(price.units) + Number(price.nanos) / 1e9) * this.quantity;
			return formatMoney({ currency_code: price.currency_code, units: Math.trunc(amount), nanos: Math.round((amount % 1) * 1e9) });
		}
	}
}
</script>

<style scoped>
.cart-item {
	display: flex;
	align-items: center;
	gap: 16px;
	padding: 16px 0;
	border-bottom: 1px solid rgba(43, 42, 40, 0.1);
}

.cart-item:last-child {
	border-bottom: none;
}

.cart-item__image {
	width: 96px;
	flex: 0 0 96px;
	border-radius: 10px;
	overflow: hidden;
	background: #EFEAE2;
	cursor: pointer;
}

.cart-item__info {
	flex: 1 1 auto;
	min-width: 0;
}

.cart-item__name {
	font-weight: 600;
	color: rgb(var(--v-theme-primary));
	cursor: pointer;
	margin-bottom: 4px;
}

.cart-item__meta {
	font-size: 0.85rem;
	color: rgba(43, 42, 40, 0.6);
}

.cart-item__price {
	font-weight: 600;
	white-space: nowrap;
	color: rgb(var(--v-theme-secondary));
}
</style>
