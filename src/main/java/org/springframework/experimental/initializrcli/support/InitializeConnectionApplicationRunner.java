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

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.experimental.initializrcli.config.InitializrCliProperties;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;

@Order(InteractiveShellApplicationRunner.PRECEDENCE - 10)
public class InitializeConnectionApplicationRunner implements ApplicationRunner {

	private TargetHolder targetHolder;

	private InitializrCliProperties initializrCliProperties;

	public InitializeConnectionApplicationRunner(TargetHolder targetHolder,
			InitializrCliProperties initializrCliProperties) {
		this.targetHolder = targetHolder;
		this.initializrCliProperties = initializrCliProperties;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		Target target = new Target(initializrCliProperties.getBaseUrl());
		targetHolder.changeTarget(target);
	}
}
