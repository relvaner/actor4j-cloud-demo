import { podRequestMethod, APIService } from './APIService';

const domain = 'AuthorizationService';

class AuthorizationService {
	static post(credentials) {
		let actorMessage = {
			alias: domain,
			tag: podRequestMethod.POST,
			payload: credentials,
			reply: true
		};
		
		return APIService.post(actorMessage);
	}
}

export { AuthorizationService };