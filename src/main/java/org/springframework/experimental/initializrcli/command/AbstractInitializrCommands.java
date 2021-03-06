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
package org.springframework.experimental.initializrcli.command;

import org.springframework.context.event.EventListener;
import org.springframework.experimental.initializrcli.client.InitializrClient;
import org.springframework.experimental.initializrcli.support.InitializrClientUpdatedEvent;
import org.springframework.shell.standard.AbstractShellComponent;

public abstract class AbstractInitializrCommands extends AbstractShellComponent {

	protected InitializrClient client;

	@EventListener
	public void handle(InitializrClientUpdatedEvent event) {
		client = event.getClient();
	}
}
