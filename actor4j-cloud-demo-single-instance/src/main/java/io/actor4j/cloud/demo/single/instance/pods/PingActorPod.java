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

package io.actor4j.cloud.demo.single.instance.pods;

import java.util.UUID;

import io.actor4j.cloud.demo.single.instance.actors.PingActor;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.ActorPod;
import io.actor4j.core.pods.RemotePodMessage;
import io.actor4j.core.pods.actors.DefaultPodActor;
import io.actor4j.core.pods.actors.RemoteHandlerPodActor;
import io.actor4j.core.pods.actors.PodActor;

public class PingActorPod extends ActorPod {
	@Override
	public PodActor create() {
		return new DefaultPodActor((groupId, context) -> new RemoteHandlerPodActor(domain(), groupId, context) {
				@Override
				public void handle(ActorMessage<?> message, UUID interaction) {
					sendViaAlias(ActorMessage.create(message.value(), message.tag(), self(), null, interaction, "", ""), getAbsoluteAlias("ping"));
				}

				@Override
				public void callback(ActorMessage<?> message, ActorMessage<?> originalMessage, ActorId dest, UUID interaction) {
					tell(message.value(), message.tag(), dest, interaction);
				}

				@Override
				public void handle(RemotePodMessage remoteMessage, UUID interaction) {
					sendViaAlias(ActorMessage.create(remoteMessage.remotePodMessageDTO().payload(), remoteMessage.remotePodMessageDTO().tag(), self(), null, interaction, "", ""), getAbsoluteAlias("ping"));
				}

				@Override
				public Object callback(ActorMessage<?> message, RemotePodMessage remoteMessage) {
					return message.value();
				}
			}) {
			@Override
			public void register() {
				addChild(() -> new PingActor(groupId, getContext()));
			}

			@Override
			public void receive(ActorMessage<?> message) {
				unhandled(message);
			}
		};
	}

	@Override
	public String domain() {
		return "ping";
	}
}
