import { podRequestMethod, APIService } from './APIService';

const domain = 'AdService';

class AdService {
	static getAll() {
		let actorMessage = {
			alias: domain,
			tag: podRequestMethod.GET_ALL,
			reply: true
		};
		
		return APIService.post(actorMessage);
	}
	
	static searchByCategory(category) {
		let actorMessage = {
			alias: domain,
			tag: podRequestMethod.ACTION_1,
			payload: category,
			reply: true
		};
		
		return APIService.post(actorMessage);
	}
	
	static randomAds() {
		let actorMessage = {
			alias: domain,
			tag: podRequestMethod.ACTION_2,
			reply: true
		};
		
		return APIService.post(actorMessage);
	}
}

export { AdService };