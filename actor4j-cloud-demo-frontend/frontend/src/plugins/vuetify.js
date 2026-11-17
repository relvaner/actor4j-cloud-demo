import 'vuetify/styles'
import '@/styles/legacy.css'
import '@/styles/boutique.css'
import '@fortawesome/fontawesome-free/css/all.css'
import { createVuetify } from 'vuetify'
import { aliases, fa } from 'vuetify/iconsets/fa'

export default createVuetify({
	icons: {
		defaultSet: 'fa',
		aliases,
		sets: { fa }
	},
	theme: {
		defaultTheme: 'boutique',
		themes: {
			// warm, calm colors that match the product photos
			boutique: {
				dark: false,
				colors: {
					background: '#FAF7F2',
					surface: '#FFFFFF',
					primary: '#2B2A28',
					secondary: '#8A6E4B',
					accent: '#C9A97E',
					error: '#B3261E',
					info: '#4A6572',
					success: '#4E7D5B',
					warning: '#B7791F'
				}
			}
		}
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
			rounded: 'lg',
			class: 'text-none font-weight-medium'
		},
		VCard: {
			rounded: 'lg'
		},
		VTextField: {
			variant: 'underlined'
		},
		VSelect: {
			variant: 'underlined'
		}
	}
})
