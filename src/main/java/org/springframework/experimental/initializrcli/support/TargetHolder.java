/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.experimental.initializrcli.support;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.experimental.initializrcli.client.InitializrClient;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class TargetHolder implements ApplicationEventPublisherAware {

	private Target target;

	private ApplicationEventPublisher applicationEventPublisher;

	private WebClient.Builder webClientBuilder;

	public TargetHolder(WebClient.Builder webClientBuilder) {
		this.webClientBuilder = webClientBuilder;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public Target getTarget() {
		return target;
	}

	public void changeTarget(Target target) {
		Assert.notNull(target, "The provided target must not be null.");
		this.target = target;
		attemptConnection();
	}

	private void attemptConnection() {
		InitializrClient client = null;
		try {
			InitializrClient c = InitializrClient.builder(webClientBuilder).target(target.getBaseUrl()).build();
			c.connect();
			client = c;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			applicationEventPublisher.publishEvent(new InitializrClientUpdatedEvent(client));
		}
	}
}
