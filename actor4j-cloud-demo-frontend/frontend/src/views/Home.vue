<template>
	<div>
		<section class="hero">
			<div class="page hero__inner">
				<div class="hero__text">
					<p class="hero__kicker">Curated objects with character</p>
					<h1 class="hero__title">Good things for everyday life.</h1>
					<p class="hero__lead">A small boutique shop, served by actor pods and functions of actor4j.</p>
					<v-btn size="large" color="primary" variant="flat" @click="scrollToProducts">Shop now<v-icon icon="fa-solid fa-arrow-down" size="x-small" class="ml-2"/></v-btn>
				</div>
			</div>
		</section>

		<div class="page py-10" ref="products">
			<ProductCatalog/>
		</div>
	</div>
</template>

<script>
	import ProductCatalog from '@/components/ProductCatalog.vue'

	export default {
		name: 'Home',
		components: {
			ProductCatalog
		},
		methods: {
			scrollToProducts() {
				this.$refs.products.scrollIntoView({ behavior: 'smooth' });
			}
		}
	}
</script>

<style scoped>
.hero {
	position: relative;
	min-height: 420px;
	display: flex;
	align-items: center;
	background-image:
		linear-gradient(90deg, rgba(250, 247, 242, 0.96) 0%, rgba(250, 247, 242, 0.85) 38%, rgba(250, 247, 242, 0) 70%),
		url('/static/img/HeroBannerImage2.jpg');
	background-size: cover;
	background-position: center;
}

.hero__inner {
	width: 100%;
}

.hero__text {
	max-width: 520px;
	padding: 56px 0;
}

.hero__kicker {
	font-size: 0.8rem;
	letter-spacing: 0.12em;
	text-transform: uppercase;
	color: rgb(var(--v-theme-secondary));
	margin-bottom: 12px;
}

.hero__title {
	font-size: clamp(2rem, 4vw, 3.2rem);
	line-height: 1.1;
	font-weight: 700;
	letter-spacing: -0.02em;
	color: rgb(var(--v-theme-primary));
	margin-bottom: 16px;
}

.hero__lead {
	font-size: 1.1rem;
	color: rgba(43, 42, 40, 0.75);
	margin-bottom: 28px;
}

@media (max-width: 760px) {
	.hero {
		background-image:
			linear-gradient(180deg, rgba(250, 247, 242, 0.92) 0%, rgba(250, 247, 242, 0.92) 100%),
			url('/static/img/HeroBannerImage2.jpg');
	}
}
</style>
