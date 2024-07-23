/*
 * Copyright (c) 2015-2024, David A. Bauer. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.actor4j.cloud.demo.module.backend.adservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import io.actor4j.cloud.demo.module.backend.utils.Utils;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.actors.PodChildActor;
import io.actor4j.core.pods.utils.PodRequestMethod;
import io.actor4j.core.pods.utils.PodStatus;
import io.vertx.core.json.JsonArray;

import static io.actor4j.core.logging.ActorLogger.*;

public class AdServiceActor extends PodChildActor {
	public static final int SEARCH_BY_CATEGORY = PodRequestMethod.ACTION_1;
	public static final int RANDOM_ADS = PodRequestMethod.ACTION_2;
	
	protected static int MAX_RANDOM_ADS = 2;
	
	protected final List<AdDTO> ads;
	protected final Map<String, List<AdDTO>> adsMap;
	
	protected final Random random;
	
	public AdServiceActor(UUID groupId, PodContext context) {
		super(groupId, context);
		
		AdDTO camera = new AdDTO("/product/2ZYFJ3GM2N", "Film camera for sale. 50% off.");
		AdDTO lens   = new AdDTO("/product/66VCHSJNUP", "Vintage camera lens for sale. 20% off.");
		AdDTO recordPlayer = new AdDTO("/product/0PUK6V6EV0", "Vintage record player for sale. 30% off.");
		AdDTO bike = new AdDTO("/product/9SIQT8TOJO", "City Bike for sale. 10% off.");
		AdDTO baristaKit = new AdDTO("/product/1YMWWN1N4O", "Home Barista kitchen kit for sale. Buy one, get second kit for free");
		AdDTO airPlant = new AdDTO("/product/6E92ZMYYFZ", "Air plants for sale. Buy two, get third one for free");
		AdDTO terrarium = new AdDTO("/product/L9ECAV7KIM", "Terrarium for sale. Buy one, get second one for free");
		
		ads = List.of(camera, lens, recordPlayer, bike, baristaKit, airPlant, terrarium);
		
		adsMap = new HashMap<>();
		adsMap.put("photography", List.of(camera, lens));
		adsMap.put("vintage", List.of(camera, lens, recordPlayer));
		adsMap.put("cycling", List.of(bike));
		adsMap.put("cookware", List.of(baristaKit));
		adsMap.put("gardening", List.of(airPlant, terrarium));
		
		random = new Random();
	}

	@Override
	public void preStart() {
		setAlias(context.domain());
	}

	@Override
	public void receive(ActorMessage<?> message) {
		logger().log(DEBUG, String.format("[%s] %s", context.domain(), message.value()));
		
		Object result = null;
		int tag = PodStatus.OK;
		if (message.tag()==PodRequestMethod.GET_ALL) {
			result = Utils.mapFrom(ads);
		}
		else if (message.tag()==SEARCH_BY_CATEGORY && message.value()!=null && message.value() instanceof String) {
			List<AdDTO> list = adsMap.get(message.valueAsString());
			if (list!=null)
				result = Utils.mapFrom(list);
			else
				result = new JsonArray();
		}
		else if (message.tag()==RANDOM_ADS)
			result = Utils.mapFrom(getRandomAds());
		else
			tag = PodStatus.METHOD_NOT_ALLOWED;
		
		tell(result, tag, message.source(), message.interaction());
	}
	
	protected List<AdDTO> getRandomAds() {
		List<AdDTO> result = new ArrayList<>(MAX_RANDOM_ADS);
		
		for (int i=0; i<MAX_RANDOM_ADS; i++)
			result.add(ads.get(random.nextInt(ads.size())));
	    
	    return result;
	}
}
