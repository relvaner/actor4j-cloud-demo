import { podRequestMethod, APIService } from './APIService';

const domain = 'RecomendationService';

class RecomendationService {
	static getAll(products) {
		let actorMessage = {
			alias: domain,
			tag: podRequestMethod.GET_ALL,
			payload: products,
			reply: true
		};
		
		return APIService.post(actorMessage);
	}
}

export { RecomendationService };