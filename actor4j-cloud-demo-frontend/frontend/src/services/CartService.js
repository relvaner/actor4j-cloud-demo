import { podRequestMethod, APIService } from './APIService';

const domain = 'CartService';

class CartService {
	static get(auth) {
		let actorMessage = {
			alias: domain,
			tag: podRequestMethod.GET,
			reply: true
		};
		
		return APIService.post(actorMessage, auth);
	}
	
	static post(cartItem, auth) {
		let actorMessage = {
			alias: domain,
			tag: podRequestMethod.POST,
			payload: cartItem,
			reply: true
		};
		
		return APIService.post(actorMessage, auth);
	}
	
	static del(auth) {
		let actorMessage = {
			alias: domain,
			tag: podRequestMethod.DELETE,
			reply: true
		};
		
		return APIService.post(actorMessage, auth);
	}
}

export { CartService };