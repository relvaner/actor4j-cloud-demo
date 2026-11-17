import axios from 'axios';
import { useMainStore } from '@/stores/main';
import { clearLogin } from '@/utils/auth';

// request methods by name, the server maps them to the pod request methods
const podRequestMethod = {
	GET         : 'GET',
	GET_ALL     : 'GET_ALL',
	POST        : 'POST',
	PUT         : 'PUT',
	DELETE      : 'DELETE',
	SUBSCRIBE   : 'SUBSCRIBE',
	UNSUBSCRIBE : 'UNSUBSCRIBE',
	PUBLISH     : 'PUBLISH',
	ACTION_1    : 'ACTION_1',
	ACTION_2    : 'ACTION_2',
	ACTION_3    : 'ACTION_3',
	ACTION_4    : 'ACTION_4',
	ACTION_5    : 'ACTION_5',
	ACTION_6    : 'ACTION_6',
	ACTION_7    : 'ACTION_7',
	ACTION_8    : 'ACTION_8',
	ACTION_9    : 'ACTION_9',
	ACTION_10   : 'ACTION_10',
	ACTION_11   : 'ACTION_11',
	ACTION_12   : 'ACTION_12',
	ACTION_13   : 'ACTION_13',
	ACTION_14   : 'ACTION_14',
	ACTION_15   : 'ACTION_15',
	ACTION_16   : 'ACTION_16',
	ACTION_17   : 'ACTION_17',
	ACTION_18   : 'ACTION_18',
	ACTION_19   : 'ACTION_19',
	ACTION_20   : 'ACTION_20',
}

const contextPath = import.meta.env.PROD
    ? '/demo/api/'
    : '/backend/';
const apiUrl = contextPath+'actors';

class APIService {
	static post(actorMessage, auth) {
		return new Promise( (resolve, reject) => {
			let config = {
				headers : {
					'X-Pod-Domain' : actorMessage.alias,
					'X-Pod-Request-Method' : actorMessage.tag
				}
			};
			if (auth!==undefined && auth!==null)
				config.headers['X-Pod-Authorization'] = 'Bearer ' + auth;
			
			// the server answers a pod status 4xx or 5xx with the same HTTP status (axios rejects it), pod.status without offset
			axios.post(apiUrl, actorMessage, config)
				.then(response => {
					resolve(response);
				})
				.catch(error => {
					// the token was rejected (expired, or the server has another key after a restart): logged out
					if (error.response?.status===401 && auth) {
						const store = useMainStore();
						if (store.user.auth===auth)
							clearLogin(store.user);
					}
					let pod = error.response?.data?.pod;
					if (pod && pod.status>=400)
						reject({
							status: pod.status,
							message: pod.message + '[POD]',
							response: error.response,
						});
					else
						reject(error);
				});
		});
	}
}

export { podRequestMethod, APIService };