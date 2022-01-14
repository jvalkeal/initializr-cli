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
package org.springframework.experimental.initializrcli.wizard;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import org.springframework.experimental.initializrcli.AbstractShellTests;
import org.springframework.experimental.initializrcli.wizard.InputWizard.InputWizardResult;
import org.springframework.experimental.initializrcli.wizard.InputWizard.SelectItem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.experimental.initializrcli.ShellAssertions.assertStringOrderThat;

public class InputWizardTests extends AbstractShellTests {

	@Test
	public void testSimpleFlow() throws InterruptedException {
		Map<String, String> single1SelectItems = new HashMap<>();
		single1SelectItems.put("key1", "value1");
		single1SelectItems.put("key2", "value2");
		List<SelectItem> multi1SelectItems = Arrays.asList(SelectItem.of("key1", "value1"),
				SelectItem.of("key2", "value2"), SelectItem.of("key3", "value3"));
		InputWizard wizard = InputWizard.builder(getTerminal())
				.withTextInput("field1")
					.name("field1")
					.defaultValue("defaultField1Value")
					.and()
				.withSingleInput("single1")
					.name("single1")
					.selectItems(single1SelectItems)
					.and()
				.withMultiInput("multi1")
					.name("multi1")
					.selectItems(multi1SelectItems)
					.and()
				.build();

		ExecutorService service = Executors.newFixedThreadPool(1);
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<InputWizardResult> result = new AtomicReference<>();

		service.execute(() -> {
			result.set(wizard.run());
			latch.countDown();
		});

		TestBuffer testBuffer = new TestBuffer().cr();
		write(testBuffer.getBytes());
		testBuffer = new TestBuffer().cr();
		write(testBuffer.getBytes());
		testBuffer = new TestBuffer().ctrlE().space().cr();
		write(testBuffer.getBytes());

		latch.await(4, TimeUnit.SECONDS);
		InputWizardResult inputWizardResult = result.get();
		assertThat(inputWizardResult).isNotNull();
		assertThat(inputWizardResult.textInputs()).containsEntry("field1", "defaultField1Value");
		assertThat(inputWizardResult.singleInputs()).containsEntry("single1", "value1");
		assertThat(inputWizardResult.multiInputs().get("multi1")).containsExactlyInAnyOrder("value2");
	}

	@Test
	public void testFlowWithDisabledItems() throws InterruptedException {
		List<SelectItem> selectItems = Arrays.asList(SelectItem.of("key1", "value1", true),
				SelectItem.of("key2", "value2", false));
		InputWizard wizard = InputWizard.builder(getTerminal())
				.withMultiInput("multi1")
					.name("multi1")
					.selectItems(selectItems)
					.and()
				.build();

		ExecutorService service = Executors.newFixedThreadPool(1);
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<InputWizardResult> result = new AtomicReference<>();

		service.execute(() -> {
			result.set(wizard.run());
			latch.countDown();
		});

		await().atMost(Duration.ofSeconds(4))
				.untilAsserted(() -> assertStringOrderThat(consoleOut()).containsInOrder("[ ] key1", "    key2"));
	}
}
