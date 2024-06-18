import { podRequestMethod, APIService } from './APIService';

const domain = 'ShippingService';

class ShippingService {
	static quote(shipOrder) {
		let actorMessage = {
			alias: domain,
			tag: podRequestMethod.ACTION_1,
			payload: shipOrder,
			reply: true
		};
		
		return APIService.post(actorMessage);
	}
}

export { ShippingService };