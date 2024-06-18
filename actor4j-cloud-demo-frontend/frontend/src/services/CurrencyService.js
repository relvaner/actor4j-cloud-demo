import { podRequestMethod, APIService } from './APIService';

const domain = 'CurrencyService';

class CurrencyService {
	static getAll() {
		let actorMessage = {
			alias: domain,
			tag: podRequestMethod.GET_ALL,
			reply: true
		};
		
		return APIService.post(actorMessage);
	}
	
	static convertByCurrency(convert) {
		let actorMessage = {
			alias: domain,
			tag: podRequestMethod.ACTION_1,
			payload: convert,
			reply: true
		};
		
		return APIService.post(actorMessage);
	}
}

export { CurrencyService };