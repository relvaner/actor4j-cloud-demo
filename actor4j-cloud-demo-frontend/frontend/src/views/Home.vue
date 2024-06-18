<template>
	<div>
		<ProductCatalog/>
	</div>
</template>

<script>
	// @ is an alias to /src
	import ProductCatalog from '@/components/ProductCatalog.vue'

	export default {
		name: 'Home',
		components: {
			ProductCatalog
		}
	}
</script>
