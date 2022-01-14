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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;

import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.experimental.initializrcli.component.DefaultMultiItemSelector;
import org.springframework.experimental.initializrcli.component.DefaultMultiItemSelector.MultiItemSelectorContext;
import org.springframework.experimental.initializrcli.component.DefaultSingleItemSelector;
import org.springframework.experimental.initializrcli.component.DefaultSingleItemSelector.SingleItemSelectorContext;
import org.springframework.experimental.initializrcli.component.FieldInput;
import org.springframework.experimental.initializrcli.component.FieldInput.FieldInputContext;
import org.springframework.experimental.initializrcli.component.SelectorItem;
import org.springframework.experimental.initializrcli.wizard.InputWizard.InputWizardResult;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Wizart providing an implementation which allows more polished way to ask various inputs
 * from a user using shell style components for single text input, single select and
 * multi-select.
 *
 * @author Janne Valkealahti
 */
public interface InputWizard extends Wizard<InputWizardResult> {

	/**
	 * Run a wizard and returns a result from it.
	 *
	 * @return the input wizard result
	 */
	InputWizardResult run();

	/**
	 * Gets a new instance of an input wizard builder.
	 *
	 * @param terminal the terminal
	 * @return the input wizard builder
	 */
	public static Builder builder(Terminal terminal) {
		return new DefaultBuilder(terminal);
	}

	/**
	 * Results from a wizard run.
	 */
	interface InputWizardResult extends WizardResult {

		/**
		 * Gets text input results.
		 *
		 * @return text input results
		 */
		Map<String, String> textInputs();

		/**
		 * Gets single input results.
		 *
		 * @return single input results
		 */
		Map<String, String> singleInputs();

		/**
		 * Gets multi input results.
		 *
		 * @return multi input results
		 */
		Map<String, List<String>> multiInputs();

		/**
		 * Merge input wizard results.
		 *
		 * @param left left side results
		 * @param right right side results
		 * @return merged results
		 */
		public static InputWizardResult merge(InputWizardResult left, InputWizardResult right) {
			DefaultInputWizardResult inputWizardResult = new DefaultInputWizardResult();
			inputWizardResult.textInputs.putAll(left.textInputs());
			inputWizardResult.textInputs.putAll(right.textInputs());
			inputWizardResult.singleInputs.putAll(left.singleInputs());
			inputWizardResult.singleInputs.putAll(right.singleInputs());
			inputWizardResult.multiInputs.putAll(left.multiInputs());
			inputWizardResult.multiInputs.putAll(right.multiInputs());
			return inputWizardResult;
		}
	}

	/**
	 * Interface for text input spec builder.
	 */
	interface TextInputSpec {

		/**
		 * Sets a name.
		 *
		 * @param name the name
		 * @return a builder
		 */
		TextInputSpec name(String name);

		/**
		 * Sets a current value.
		 *
		 * @param currentValue the current value
		 * @return a builder
		 */
		TextInputSpec currentValue(String currentValue);

		/**
		 * Sets a default value.
		 *
		 * @param defaultValue the defult value
		 * @return a builder
		 */
		TextInputSpec defaultValue(String defaultValue);

		/**
		 * Sets a renderer function.
		 *
		 * @param renderer the renderer
		 * @return a builder
		 */
		TextInputSpec renderer(Function<FieldInputContext, List<AttributedString>> renderer);

		/**
		 * Build and return parent builder.
		 *
		 * @return the parent builder
		 */
		Builder and();
	}

	/**
	 * Interface for single input spec builder.
	 */
	interface SingleInputSpec {

		/**
		 * Sets a name.
		 *
		 * @param name the name
		 * @return a builder
		 */
		SingleInputSpec name(String name);

		/**
		 * Sets a current value.
		 *
		 * @param currentValue the current value
		 * @return a builder
		 */
		SingleInputSpec currentValue(String currentValue);

		/**
		 * Adds a select item.
		 *
		 * @param name the name
		 * @param item the item
		 * @return a builder
		 */
		SingleInputSpec selectItem(String name, String item);

		/**
		 * Adds a map of select items.
		 *
		 * @param selectItems the select items
		 * @return a builder
		 */
		SingleInputSpec selectItems(Map<String, String> selectItems);

		/**
		 * Sets a {@link Comparator} for sorting items.
		 *
		 * @param comparator the item comparator
		 * @return a builder
		 */
		SingleInputSpec sort(Comparator<SelectorItem<String>> comparator);

		/**
		 * Sets a renderer function.
		 *
		 * @param renderer the renderer
		 * @return a builder
		 */
		SingleInputSpec renderer(Function<SingleItemSelectorContext<SelectorItem<String>>, List<AttributedString>> renderer);

		/**
		 * Build and return parent builder.
		 *
		 * @return the parent builder
		 */
		Builder and();
	}

	/**
	 * Interface for multi input spec builder.
	 */
	interface MultiInputSpec {

		/**
		 * Sets a name.
		 *
		 * @param name the name
		 * @return a builder
		 */
		MultiInputSpec name(String name);

		/**
		 * Sets a current values.
		 *
		 * @param currentValues the current value
		 * @return a builder
		 */
		MultiInputSpec currentValue(List<String> currentValues);

		/**
		 * Adds a list of select items.
		 *
		 * @param selectItems the select items
		 * @return a builder
		 */
		MultiInputSpec selectItems(List<SelectItem> selectItems);

		/**
		 * Sets a {@link Comparator} for sorting items.
		 *
		 * @param comparator the item comparator
		 * @return a builder
		 */
		MultiInputSpec sort(Comparator<SelectorItem<String>> comparator);

		/**
		 * Sets a renderer function.
		 *
		 * @param renderer the renderer
		 * @return a builder
		 */
		MultiInputSpec renderer(Function<MultiItemSelectorContext<SelectorItem<String>>, List<AttributedString>> renderer);

		/**
		 * Build and return parent builder.
		 *
		 * @return the parent builder
		 */
		Builder and();
	}

	/**
	 * Interface for a wizard builder.
	 */
	interface Builder {

		/**
		 * Gets a builder for text input.
		 *
		 * @param id the identifier
		 * @return builder for text input
		 */
		TextInputSpec withTextInput(String id);

		/**
		 * Gets a builder for single input.
		 *
		 * @param id the identifier
		 * @return builder for single input
		 */
		SingleInputSpec withSingleInput(String id);

		/**
		 * Gets a builder for multi input.
		 *
		 * @param id the identifier
		 * @return builder for multi input
		 */
		MultiInputSpec withMultiInput(String id);

		/**
		 * Builds instance of input wizard.
		 *
		 * @return instance of input wizard
		 */
		InputWizard build();
	}

	static abstract class BaseBuilder implements Builder {

		private Terminal terminal;
		private final List<BaseTextInput> textInputs = new ArrayList<>();
		private final List<BaseSingleInput> singleInputs = new ArrayList<>();
		private final List<BaseMultiInput> multiInputs = new ArrayList<>();
		private final AtomicInteger order = new AtomicInteger();

		BaseBuilder(Terminal terminal) {
			this.terminal = terminal;
		}

		@Override
		public InputWizard build() {
			return new DefaultInputWizard(terminal, textInputs, singleInputs, multiInputs);
		}

		@Override
		public TextInputSpec withTextInput(String id) {
			return new DefaultTextInputSpec(this, id);
		}

		@Override
		public SingleInputSpec withSingleInput(String id) {
			return new DefaultSingleInputSpec(this, id);
		}

		@Override
		public MultiInputSpec withMultiInput(String id) {
			return new DefaultMultiInputSpec(this, id);
		}

		void addTextInput(BaseTextInput input) {
			input.setOrder(order.getAndIncrement());
			textInputs.add(input);
		}

		void addSingleInput(BaseSingleInput input) {
			input.setOrder(order.getAndIncrement());
			singleInputs.add(input);
		}

		void addMultiInput(BaseMultiInput input) {
			input.setOrder(order.getAndIncrement());
			multiInputs.add(input);
		}
	}

	static abstract class BaseInput implements Ordered {

		private final BaseBuilder builder;
		private final String id;
		private int order;

		BaseInput(BaseBuilder builder, String id) {
			this.builder = builder;
			this.id = id;
		}

		@Override
		public int getOrder() {
			return order;
		}

		public void setOrder(int order) {
			this.order = order;
		}

		public BaseBuilder getBuilder() {
			return builder;
		}

		public String getId() {
			return id;
		}
	}

	static abstract class BaseTextInput extends BaseInput implements TextInputSpec {

		private String name;
		private String currentValue;
		private String defaultValue;
		private Function<FieldInputContext, List<AttributedString>> renderer;

		public BaseTextInput(BaseBuilder builder, String id) {
			super(builder, id);
		}

		@Override
		public TextInputSpec name(String name) {
			this.name = name;
			return this;
		}

		@Override
		public TextInputSpec currentValue(String currentValue) {
			this.currentValue = currentValue;
			return this;
		}

		@Override
		public TextInputSpec defaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		@Override
		public TextInputSpec renderer(Function<FieldInputContext, List<AttributedString>> renderer) {
			this.renderer = renderer;
			return this;
		}

		@Override
		public Builder and() {
			getBuilder().addTextInput(this);
			return getBuilder();
		}

		public String getName() {
			return name;
		}

		public String getCurrentValue() {
			return currentValue;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

		public Function<FieldInputContext, List<AttributedString>> getRenderer() {
			return renderer;
		}
	}

	static class DefaultTextInputSpec extends BaseTextInput {

		public DefaultTextInputSpec(BaseBuilder builder, String id) {
			super(builder, id);
		}
	}

	static abstract class BaseSingleInput extends BaseInput implements SingleInputSpec {

		private String name;
		private String currentValue;
		private Map<String, String> selectItems = new HashMap<>();
		private Comparator<SelectorItem<String>> comparator;
		private Function<SingleItemSelectorContext<SelectorItem<String>>, List<AttributedString>> renderer;

		public BaseSingleInput(BaseBuilder builder, String id) {
			super(builder, id);
		}

		@Override
		public SingleInputSpec name(String name) {
			this.name = name;
			return this;
		}

		@Override
		public SingleInputSpec currentValue(String currentValue) {
			this.currentValue = currentValue;
			return this;
		}

		@Override
		public SingleInputSpec selectItem(String name, String item) {
			this.selectItems.put(name, item);
			return this;
		}

		@Override
		public SingleInputSpec selectItems(Map<String, String> selectItems) {
			this.selectItems.putAll(selectItems);
			return this;
		}

		@Override
		public SingleInputSpec sort(Comparator<SelectorItem<String>> comparator) {
			this.comparator = comparator;
			return this;
		}

		@Override
		public SingleInputSpec renderer(Function<SingleItemSelectorContext<SelectorItem<String>>, List<AttributedString>> renderer) {
			this.renderer = renderer;
			return this;
		}

		@Override
		public Builder and() {
			getBuilder().addSingleInput(this);
			return getBuilder();
		}

		public String getName() {
			return name;
		}

		public String getCurrentValue() {
			return currentValue;
		}

		public Map<String, String> getSelectItems() {
			return selectItems;
		}

		public Comparator<SelectorItem<String>> getComparator() {
			return comparator;
		}

		public Function<SingleItemSelectorContext<SelectorItem<String>>, List<AttributedString>> getRenderer() {
			return renderer;
		}
	}

	static class DefaultSingleInputSpec extends BaseSingleInput {

		public DefaultSingleInputSpec(BaseBuilder builder, String id) {
			super(builder, id);
		}
	}

	public interface SelectItem {

		String name();
		String item();
		boolean enabled();

		public static SelectItem of(String name, String item) {
			return of(name, item, true);
		}

		public static SelectItem of(String name, String item, boolean enabled) {
			return new DefaultSelectItem(name, item, enabled);
		}
	}

	static class DefaultSelectItem implements SelectItem {

		private String name;
		private String item;
		private boolean enabled;

		public DefaultSelectItem(String name, String item, boolean enabled) {
			this.name = name;
			this.item = item;
			this.enabled = enabled;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public String item() {
			return item;
		}

		@Override
		public boolean enabled() {
			return enabled;
		}
	}

	static abstract class BaseMultiInput extends BaseInput implements MultiInputSpec {

		private String name;
		private List<String> currentValues = new ArrayList<>();
		private List<SelectItem> selectItems = new ArrayList<>();
		private Comparator<SelectorItem<String>> comparator;
		private Function<MultiItemSelectorContext<SelectorItem<String>>, List<AttributedString>> renderer;

		public BaseMultiInput(BaseBuilder builder, String id) {
			super(builder, id);
		}

		@Override
		public MultiInputSpec name(String name) {
			this.name = name;
			return this;
		}

		@Override
		public MultiInputSpec currentValue(List<String> currentValues) {
			this.currentValues.addAll(currentValues);
			return this;
		}

		@Override
		public MultiInputSpec selectItems(List<SelectItem> selectItems) {
			this.selectItems = selectItems;
			return this;
		}

		@Override
		public MultiInputSpec sort(Comparator<SelectorItem<String>> comparator) {
			this.comparator = comparator;
			return this;
		}

		@Override
		public MultiInputSpec renderer(Function<MultiItemSelectorContext<SelectorItem<String>>, List<AttributedString>> renderer) {
			this.renderer = renderer;
			return this;
		}

		@Override
		public Builder and() {
			getBuilder().addMultiInput(this);
			return getBuilder();
		}

		public String getName() {
			return name;
		}

		public List<String> getCurrentValues() {
			return currentValues;
		}

		public List<SelectItem> getSelectItemsx() {
			return selectItems;
		}

		public Comparator<SelectorItem<String>> getComparator() {
			return comparator;
		}

		public Function<MultiItemSelectorContext<SelectorItem<String>>, List<AttributedString>> getRenderer() {
			return renderer;
		}
	}

	static class DefaultMultiInputSpec extends BaseMultiInput {

		public DefaultMultiInputSpec(BaseBuilder builder, String id) {
			super(builder, id);
		}
	}

	static class DefaultBuilder extends BaseBuilder {

		DefaultBuilder(Terminal terminal) {
			super(terminal);
		}
	}

	static class DefaultInputWizardResult implements InputWizardResult {

		private final Map<String, String> textInputs = new HashMap<>();
		private final Map<String, String> singleInputs = new HashMap<>();
		private final Map<String, List<String>> multiInputs = new HashMap<>();

		@Override
		public Map<String, String> textInputs() {
			return textInputs;
		}

		@Override
		public Map<String, String> singleInputs() {
			return singleInputs;
		}

		@Override
		public Map<String, List<String>> multiInputs() {
			return multiInputs;
		}

		void addTextInput(String key, String value) {
			textInputs.put(key, value);
		}

		void addSingleInput(String key, String value) {
			singleInputs.put(key, value);
		}

		void addMultiInput(String key, List<String> value) {
			multiInputs.put(key, value);
		}
	}

	static class DefaultInputWizard implements InputWizard {

		private final Terminal terminal;
		private final List<BaseTextInput> textInputs;
		private final List<BaseSingleInput> singleInputs;
		private final List<BaseMultiInput> multiInputs;

		DefaultInputWizard(Terminal terminal, List<BaseTextInput> textInputs, List<BaseSingleInput> singleInputs,
				List<BaseMultiInput> multiInputs) {
			this.terminal = terminal;
			this.textInputs = textInputs;
			this.singleInputs = singleInputs;
			this.multiInputs = multiInputs;
		}

		@Override
		public InputWizardResult run() {
			return runGetResults();
		}

		private DefaultInputWizardResult runGetResults() {

			final DefaultInputWizardResult result = new DefaultInputWizardResult();

			Stream<OrderedInputOperation> textInputsStream = textInputs.stream().map(input -> {
				Supplier<String> operation = () -> {
					String out = null;
					String currentValue = input.getCurrentValue();
					if (StringUtils.hasText(currentValue)) {
						result.addTextInput(input.getId(), currentValue);
					}
					else {
						FieldInput selector = new FieldInput(terminal, input.getName(), input.getDefaultValue());
						if (input.getRenderer() != null) {
							selector.setRenderer(input.getRenderer());
						}
						String value = selector.run();
						result.addTextInput(input.getId(), value);
						out = selector.render(FieldInputContext.ofResult(input.getName(), value))
								.stream()
								.map(as -> as.toAnsi())
								.collect(Collectors.joining("\n"));
					}
					return out;
				};
				return OrderedInputOperation.of(input.getOrder(), operation);
			});

			Stream<OrderedInputOperation> singleInputsStream = singleInputs.stream().map(input -> {
				Supplier<String> operation = () -> {
					String out = null;
					String currentValue = input.getCurrentValue();
					if (StringUtils.hasText(currentValue)) {
						result.addSingleInput(input.getId(), currentValue);
					}
					else {
						List<SelectorItem<String>> selectorItems = input.getSelectItems().entrySet().stream()
								.map(e -> SelectorItem.of(e.getKey(), e.getValue()))
								.collect(Collectors.toList());
						DefaultSingleItemSelector<SelectorItem<String>> selector = new DefaultSingleItemSelector<>(terminal,
								selectorItems, input.getName(), input.getComparator());
						if (input.getRenderer() != null) {
							selector.setRenderer(input.getRenderer());
						}
						Optional<SelectorItem<String>> select = selector.select();
						String type = select.map(i -> i.getItem()).orElse("<none>");
						result.addSingleInput(input.getId(), type);
						out = selector.render(SingleItemSelectorContext.ofResult(input.getName(), type))
								.stream()
								.map(as -> as.toAnsi())
								.collect(Collectors.joining("\n"));
					}
					return out;
				};
				return OrderedInputOperation.of(input.getOrder(), operation);
			});

			Stream<OrderedInputOperation> multiInputsStream = multiInputs.stream().map(input -> {
				Supplier<String> operation = () -> {
					String out = null;
					List<String> currentValues = input.getCurrentValues();
					if (!ObjectUtils.isEmpty(currentValues)) {
						result.addMultiInput(input.getId(), currentValues);
					}
					else {
						List<SelectorItem<String>> selectorItems = input.getSelectItemsx().stream()
								.map(si -> SelectorItem.of(si.name(), si.item(), si.enabled()))
								.collect(Collectors.toList());

						DefaultMultiItemSelector<SelectorItem<String>> selector = new DefaultMultiItemSelector<>(terminal,
								selectorItems, input.getName(), input.getComparator());
						if (input.getRenderer() != null) {
							selector.setRenderer(input.getRenderer());
						}
						List<SelectorItem<String>> select = selector.select();
						List<String> values = select.stream().map(i -> i.getItem()).collect(Collectors.toList());
						result.addMultiInput(input.getId(), values);
						out = selector.render(MultiItemSelectorContext.ofResult(input.getName(), values))
								.stream()
								.map(as -> as.toAnsi())
								.collect(Collectors.joining("\n"));
					}
					return out;
				};
				return OrderedInputOperation.of(input.getOrder(), operation);
			});

			Stream.of(textInputsStream, singleInputsStream, multiInputsStream)
					.flatMap(oio -> oio)
					.sorted(OrderComparator.INSTANCE)
					.forEach(oio -> {
						String out = oio.getOperation().get();
						if (StringUtils.hasText(out)) {
							terminal.writer().println(out);
							terminal.writer().flush();
						}
					});
			return result;
		}
	}

	static class OrderedInputOperation implements Ordered {

		private int order;
		private Supplier<String> operation;

		@Override
		public int getOrder() {
			return order;
		}

		public Supplier<String> getOperation() {
			return operation;
		}

		static OrderedInputOperation of(int order, Supplier<String> operation) {
			OrderedInputOperation oio = new OrderedInputOperation();
			oio.order = order;
			oio.operation = operation;
			return oio;
		}
	}
}
