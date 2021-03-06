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

import java.time.Duration;

import io.netty.resolver.DefaultAddressResolverGroup;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.web.reactive.function.client.ReactorNettyHttpClientMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.experimental.initializrcli.support.InitializeConnectionApplicationRunner;
import org.springframework.experimental.initializrcli.support.TargetHolder;
import org.springframework.http.client.reactive.ReactorResourceFactory;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(InitializrCliProperties.class)
public class InitializrCliConfiguration {

    @Bean
    public ReactorResourceFactory reactorClientResourceFactory() {
		// to change default 2s quiet period so that
		// context terminates more quick
        ReactorResourceFactory factory = new ReactorResourceFactory();
        factory.setShutdownQuietPeriod(Duration.ZERO);
        return factory;
    }

	@Bean
	ReactorNettyHttpClientMapper reactorNettyHttpClientMapper() {
        // workaround for native/graal issue
        // https://github.com/spring-projects-experimental/spring-native/issues/1319
		return httpClient -> httpClient.resolver(DefaultAddressResolverGroup.INSTANCE);
	}

    @Bean
    public ApplicationRunner initializeConnectionApplicationRunner(TargetHolder targetHolder,
            InitializrCliProperties initializrCliProperties) {
        return new InitializeConnectionApplicationRunner(targetHolder, initializrCliProperties);
    }
}
