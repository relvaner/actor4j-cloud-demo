import axios from 'axios';

const POD_STATUS_OFFSET = 10000;
const POD_REQUEST_METHOD_OFFSET = 11000;

const podRequestMethod = {
	GET         : POD_REQUEST_METHOD_OFFSET,
	GET_ALL     : POD_REQUEST_METHOD_OFFSET+1,
	POST        : POD_REQUEST_METHOD_OFFSET+2,
	PUT         : POD_REQUEST_METHOD_OFFSET+3,
	DELETE      : POD_REQUEST_METHOD_OFFSET+4,

	SUBSCRIBE   : POD_REQUEST_METHOD_OFFSET+6,
	UNSUBSCRIBE : POD_REQUEST_METHOD_OFFSET+7,
	PUBLISH     : POD_REQUEST_METHOD_OFFSET+8,

	ACTION_1    : POD_REQUEST_METHOD_OFFSET+11,
	ACTION_2    : POD_REQUEST_METHOD_OFFSET+12,
	ACTION_3    : POD_REQUEST_METHOD_OFFSET+13,
	ACTION_4    : POD_REQUEST_METHOD_OFFSET+14,
	ACTION_5    : POD_REQUEST_METHOD_OFFSET+15,
	ACTION_6    : POD_REQUEST_METHOD_OFFSET+16,
	ACTION_7    : POD_REQUEST_METHOD_OFFSET+17,
	ACTION_8    : POD_REQUEST_METHOD_OFFSET+18,
	ACTION_9    : POD_REQUEST_METHOD_OFFSET+19,
	ACTION_10   : POD_REQUEST_METHOD_OFFSET+20,

	ACTION_11   : POD_REQUEST_METHOD_OFFSET+21,
	ACTION_12   : POD_REQUEST_METHOD_OFFSET+22,
	ACTION_13   : POD_REQUEST_METHOD_OFFSET+23,
	ACTION_14   : POD_REQUEST_METHOD_OFFSET+24,
	ACTION_15   : POD_REQUEST_METHOD_OFFSET+25,
	ACTION_16   : POD_REQUEST_METHOD_OFFSET+26,
	ACTION_17   : POD_REQUEST_METHOD_OFFSET+27,
	ACTION_18   : POD_REQUEST_METHOD_OFFSET+28,
	ACTION_19   : POD_REQUEST_METHOD_OFFSET+29,
	ACTION_20   : POD_REQUEST_METHOD_OFFSET+30,
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
					let status = response.data.pod.status - POD_STATUS_OFFSET;
					if (status>=400) {
						// 4XXX, 5XXX
						let error = {
							status: status,
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