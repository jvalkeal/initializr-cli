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
package org.springframework.experimental.initializrcli.component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.experimental.initializrcli.AbstractShellTests;
import org.springframework.experimental.initializrcli.component.context.ComponentContext;
import org.springframework.experimental.initializrcli.component.support.SelectorItem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.experimental.initializrcli.ShellAssertions.assertStringOrderThat;

public class SingleItemSelectorTests extends AbstractShellTests {

	private static SimplePojo SIMPLE_POJO_1 = SimplePojo.of("data1");
	private static SimplePojo SIMPLE_POJO_2 = SimplePojo.of("data2");
	private static SimplePojo SIMPLE_POJO_3 = SimplePojo.of("data3");
	private static SimplePojo SIMPLE_POJO_4 = SimplePojo.of("data4");
	private static SimplePojo SIMPLE_POJO_5 = SimplePojo.of("data5");
	private static SimplePojo SIMPLE_POJO_6 = SimplePojo.of("data6");
	private static SelectorItem<SimplePojo> SELECTOR_ITEM_1 = SelectorItem.of("simplePojo1", SIMPLE_POJO_1);
	private static SelectorItem<SimplePojo> SELECTOR_ITEM_2 = SelectorItem.of("simplePojo2", SIMPLE_POJO_2);
	private static SelectorItem<SimplePojo> SELECTOR_ITEM_3 = SelectorItem.of("simplePojo3", SIMPLE_POJO_3);
	private static SelectorItem<SimplePojo> SELECTOR_ITEM_4 = SelectorItem.of("simplePojo4", SIMPLE_POJO_4);
	private static SelectorItem<SimplePojo> SELECTOR_ITEM_5 = SelectorItem.of("simplePojo5", SIMPLE_POJO_5);
	private static SelectorItem<SimplePojo> SELECTOR_ITEM_6 = SelectorItem.of("simplePojo6", SIMPLE_POJO_6);

	private ExecutorService service;
	private CountDownLatch latch;
	private AtomicReference<Optional<SelectorItem<SimplePojo>>> result;

	@BeforeEach
	public void setupMulti() {
		service = Executors.newFixedThreadPool(1);
		latch = new CountDownLatch(1);
		result = new AtomicReference<>();
	}

	@AfterEach
	public void cleanupMulti() {
		latch = null;
		result = null;
		if (service != null) {
			service.shutdown();
		}
		service = null;
	}

	@Test
	public void testItemsShownFirstHovered() {
		scheduleSelect();
		// await().atMost(Duration.ofSeconds(4))
		// 		.untilAsserted(() -> assertStringOrderThat(consoleOut()).containsInOrder("simplePojo1", "simplePojo2",
		// 				"simplePojo3", "simplePojo4"));
		await().atMost(Duration.ofSeconds(4))
				.untilAsserted(() -> {
					assertStringOrderThat(consoleOut()).containsInOrder("> simplePojo1", "simplePojo2", "simplePojo3", "simplePojo4");
				});

	}

	// @Test
	public void testItemsShownSecondHovered() {
		scheduleSelect();
		TestBuffer testBuffer = new TestBuffer().ctrlE().ctrlE();
		write(testBuffer.getBytes());

		await().atMost(Duration.ofSeconds(4))
				.untilAsserted(() -> {
					assertStringOrderThat(consoleOut()).containsInOrder("simplePojo1", "> simplePojo2", "simplePojo3", "simplePojo4");
				});

	}

	@Test
	public void testMaxItems() {
		scheduleSelect(Arrays.asList(SELECTOR_ITEM_1, SELECTOR_ITEM_2, SELECTOR_ITEM_3, SELECTOR_ITEM_4,
				SELECTOR_ITEM_5, SELECTOR_ITEM_6), 6);
		await().atMost(Duration.ofSeconds(4))
				.untilAsserted(() -> assertStringOrderThat(consoleOut()).containsInOrder("simplePojo1", "simplePojo2",
						"simplePojo3", "simplePojo4", "simplePojo5", "simplePojo6"));
	}

	@Test
	public void testSelectFirst() throws InterruptedException {
		scheduleSelect();

		TestBuffer testBuffer = new TestBuffer().cr();
		write(testBuffer.getBytes());

		awaitLatch();

		Optional<SelectorItem<SimplePojo>> selected = result.get();
		assertThat(selected).isNotEmpty();
		Stream<String> datas = selected.stream().map(SelectorItem::getItem).map(SimplePojo::getData);
		// assertThat(result.get().getItem().getData()).isEqualTo("data");
		assertThat(datas).containsExactlyInAnyOrder("data1");
		assertThat(consoleOut()).contains("testSimple data1");
	}

	@Test
	public void testSelectSecond() throws InterruptedException {
		scheduleSelect();

		TestBuffer testBuffer = new TestBuffer().ctrlE().cr();
		write(testBuffer.getBytes());

		awaitLatch();

		Optional<SelectorItem<SimplePojo>> selected = result.get();
		assertThat(selected).isNotEmpty();
		Stream<String> datas = selected.stream().map(SelectorItem::getItem).map(SimplePojo::getData);
		assertThat(datas).containsExactlyInAnyOrder("data2");
		// assertThat(result.get().getItem().getData()).isEqualTo("data2");
	}

	@Test
	public void testSelectLastBackwards() throws InterruptedException {
		scheduleSelect();

		TestBuffer testBuffer = new TestBuffer().ctrlY().cr();
		write(testBuffer.getBytes());

		awaitLatch();

		Optional<SelectorItem<SimplePojo>> selected = result.get();
		assertThat(selected).isNotEmpty();
		Stream<String> datas = selected.stream().map(SelectorItem::getItem).map(SimplePojo::getData);
		assertThat(datas).containsExactlyInAnyOrder("data4");
		// assertThat(result.get().getItem().getData()).isEqualTo("data4");
	}

	private void scheduleSelect() {
		scheduleSelect(Arrays.asList(SELECTOR_ITEM_1, SELECTOR_ITEM_2, SELECTOR_ITEM_3,
				SELECTOR_ITEM_4));
	}

	private void scheduleSelect(List<SelectorItem<SimplePojo>> items) {
		scheduleSelect(items, null);
	}

	private void scheduleSelect(List<SelectorItem<SimplePojo>> items, Integer maxItems) {
		// SelectorItem<String> of1 = SelectorItem.of("name", "item");
		// Itemable<String> of2 = SelectorItem.of("name", "item");
		SingleItemSelector<SimplePojo, SelectorItem<SimplePojo>> selector = new SingleItemSelector<>(getTerminal(),
				items, "testSimple", null);
		// SingleItemSelector<SimplePojo> selector = new SingleItemSelector<>(getTerminal(),
		// 		items, "testSimple", null);
		// new SingleItemSelector<SimplePojo, SelectorItem<SimplePojo>>(getTerminal(),items, "testSimple", null);
		// new SingleItemSelector<SimplePojo, SelectorItem>(null, null, null, null);
		selector.setPrintResults(true);
		if (maxItems != null) {
			selector.setMaxItems(maxItems);
		}
		service.execute(() -> {
			ComponentContext<?> context = ComponentContext.empty();
			result.set(selector.run(context).getResultItem());
			latch.countDown();
		});
	}

	private void awaitLatch() throws InterruptedException {
		latch.await(4, TimeUnit.SECONDS);
	}

	private static class SimplePojo {
		String data;

		SimplePojo(String data) {
			this.data = data;
		}

		public String getData() {
			return data;
		}

		static SimplePojo of(String data) {
			return new SimplePojo(data);
		}

		@Override
		public String toString() {
			return data;
		}
	}
}
