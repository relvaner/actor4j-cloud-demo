import { podRequestMethod, APIService } from './APIService';

const domain = 'ProductCatalogService';

class ProductCatalogService {
	static get(id) {
		let actorMessage = {
			alias: domain,
			tag: podRequestMethod.GET,
			payload: id,
			reply: true
		};
		
		return APIService.post(actorMessage);
	}
	
	static getAll() {
		let actorMessage = {
			alias: domain,
			tag: podRequestMethod.GET_ALL,
			reply: true
		};
		
		return APIService.post(actorMessage);
	}
	
	static searchByNameOrDescription(query) {
		let actorMessage = {
			alias: domain,
			tag: podRequestMethod.ACTION_1,
			payload: query,
			reply: true
		};
		
		return APIService.post(actorMessage);
	}
	
	static searchByCategory(query) {
		let actorMessage = {
			alias: domain,
			tag: podRequestMethod.ACTION_2,
			payload: query,
			reply: true
		};
		
		return APIService.post(actorMessage);
	}
}

export { ProductCatalogService };