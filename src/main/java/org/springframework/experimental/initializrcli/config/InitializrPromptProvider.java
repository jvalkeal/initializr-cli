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
package org.springframework.experimental.initializrcli.config;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import org.springframework.context.event.EventListener;
import org.springframework.experimental.initializrcli.client.InitializrClient;
import org.springframework.experimental.initializrcli.support.InitializrClientUpdatedEvent;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class InitializrPromptProvider implements PromptProvider {

	private InitializrClient client;

	@Override
	public AttributedString getPrompt() {
		if (client != null) {
			return new AttributedString("initializr:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
		}
		else {
			return new AttributedString("server-unknown:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
		}
	}

	@EventListener
	public void handle(InitializrClientUpdatedEvent event) {
		client = event.getClient();
	}
}
