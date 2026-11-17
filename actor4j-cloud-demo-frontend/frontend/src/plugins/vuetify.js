import 'vuetify/styles'
import '@/styles/legacy.css'
import { createVuetify } from 'vuetify'

export default createVuetify({
	theme: {
		defaultTheme: 'light'
	},
	display: {
		thresholds: {
			md: 960,
			lg: 1264,
			xl: 1904,
			xxl: 2560
		}
	},
	defaults: {
		VBtn: {
			class: 'text-uppercase'
		},
		VTextField: {
			variant: 'underlined'
		},
		VSelect: {
			variant: 'underlined'
		}
	}
})
