import { podRequestMethod, APIService } from './APIService';

const domain = 'CheckoutService';

class CheckoutService {
	static post(order, auth) {
		let actorMessage = {
			alias: domain,
			tag: podRequestMethod.POST,
			payload: order, 
			reply: true
		};
		
		return APIService.post(actorMessage, auth);
	}
}

export { CheckoutService };