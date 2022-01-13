package org.springframework.experimental.initializrcli.wizard;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import org.springframework.experimental.initializrcli.wizard.InputWizard.InputWizardResult;

import static org.assertj.core.api.Assertions.assertThat;

public class InputWizardTests extends AbstractWizardTests {

	@Test
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	public void testSimpleFlow() throws InterruptedException {
		Map<String, String> single1SelectItems = new HashMap<>();
		single1SelectItems.put("key1", "value1");
		single1SelectItems.put("key2", "value2");
		Map<String, String> multi1SelectItems = new HashMap<>();
		multi1SelectItems.put("key1", "value1");
		multi1SelectItems.put("key2", "value2");
		multi1SelectItems.put("key3", "value3");
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
		assertThat(inputWizardResult.multiInputs().get("multi1")).containsExactlyInAnyOrder("value1");
	}
}
