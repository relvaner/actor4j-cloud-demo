<template>
	<v-card class="product" flat @click="$router.push({name: 'Product', params: { id: product.id }})">
		<div class="product__image">
			<v-img cover :aspect-ratio="compact ? 4/3 : 3/2" :src="product.picture" :alt="product.name"/>
		</div>
		<div class="product__body">
			<div>
				<div class="product__name">{{ product.name }}</div>
				<div v-if="!compact && product.categories" class="product__category">{{ product.categories.join(' · ') }}</div>
			</div>
			<div class="product__price">{{ price }}</div>
		</div>
	</v-card>
</template>

<script>
import { formatMoney } from '@/utils/money';

export default {
	name: 'Product',
	props: {
		product: Object,
		compact: Boolean
	},
	computed: {
		price() {
			return formatMoney(this.product.price_usd);
		}
	}
}
</script>

<style scoped>
.product {
	background: transparent !important;
	transition: transform 0.2s ease;
}

.product__image {
	overflow: hidden;
	border-radius: 12px;
	background: #EFEAE2;
	box-shadow: 0 1px 2px rgba(43, 42, 40, 0.06);
	transition: box-shadow 0.2s ease;
}

.product__image :deep(.v-img__img) {
	transition: transform 0.4s ease;
}

.product:hover {
	transform: translateY(-3px);
}

.product:hover .product__image {
	box-shadow: 0 12px 28px rgba(43, 42, 40, 0.14);
}

.product:hover .product__image :deep(.v-img__img) {
	transform: scale(1.04);
}

.product__body {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	gap: 12px;
	padding: 12px 2px 4px;
}

.product__name {
	font-weight: 600;
	color: rgb(var(--v-theme-primary));
}

.product__category {
	font-size: 0.8rem;
	text-transform: capitalize;
	color: rgba(var(--v-theme-on-surface), 0.55);
	margin-top: 2px;
}

.product__price {
	font-weight: 600;
	white-space: nowrap;
	color: rgb(var(--v-theme-secondary));
}
</style>
