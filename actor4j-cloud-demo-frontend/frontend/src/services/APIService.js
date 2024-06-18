import axios from 'axios';

const podRequestMethod = {
	GET       : 0,
	GET_ALL   : 1,
	POST	  : 2,
	PUT		  : 3,
	DELETE	  : 4,
	
	ACTION_1  : 11,
	ACTION_2  : 12,
	ACTION_3  : 13,
	ACTION_4  : 14,
	ACTION_5  : 15,
	ACTION_6  : 16,
	ACTION_7  : 17,
	ACTION_8  : 18,
	ACTION_9  : 19,
	ACTION_10 : 20
}

const contextPath = process.env.NODE_ENV === 'production'
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
			
			axios.post(apiUrl, actorMessage, config)
				.then(response => {
					if (response.data.pod.status>=400) {
						// 4XXX, 5XXX
						let error = {
							status: response.data.pod.status,
							message: response.data.pod.message + '[POD]',
							response: response,
						};
						reject(error);
					}
					else
						resolve(response);
				})
				.catch(error => {
					reject(error);
				});
		});
	}
}

export { podRequestMethod, APIService };