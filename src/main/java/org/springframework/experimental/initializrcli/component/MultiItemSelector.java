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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;

import org.springframework.experimental.initializrcli.component.context.ComponentContext;
import org.springframework.experimental.initializrcli.component.support.AbstractSelectorComponent;
import org.springframework.experimental.initializrcli.component.support.Enableable;
import org.springframework.experimental.initializrcli.component.support.Itemable;
import org.springframework.experimental.initializrcli.component.support.Matchable;
import org.springframework.experimental.initializrcli.component.support.Nameable;
import org.springframework.experimental.initializrcli.component.support.AbstractSelectorComponent.SelectorComponentContext;
import org.springframework.util.StringUtils;
import org.springframework.experimental.initializrcli.component.MultiItemSelector.MultiItemSelectorContext;

/**
 * Component able to pick multiple items.
 *
 * @author Janne Valkealahti
 */
public class MultiItemSelector<T, I extends Nameable & Matchable & Enableable & Itemable<T>>
		extends AbstractSelectorComponent<T, MultiItemSelectorContext<T, I>, I> {

	private MultiItemSelectorContext<T, I> currentContext;

	public MultiItemSelector(Terminal terminal, List<I> items, String name, Comparator<I> comparator) {
		super(terminal, name, items, false, comparator);
		setRenderer(new DefaultRenderer());
	}

	@Override
	protected MultiItemSelectorContext<T, I> getThisContext(ComponentContext<?> context) {
		if (context != null && currentContext == context) {
			return currentContext;
		}
		currentContext = MultiItemSelectorContext.empty(getItemMapper());
		currentContext.setName(name);
		if (currentContext.getItems() == null) {
			currentContext.setItems(getItems());
		}
		context.stream().forEach(e -> {
			currentContext.put(e.getKey(), e.getValue());
		});
		return currentContext;
	}

	@Override
	protected MultiItemSelectorContext<T, I> runInternal(MultiItemSelectorContext<T, I> context) {
		super.runInternal(context);
		loop(context);
		return context;
	}

	/**
	 * Context {@link MultiItemSelector}.
	 */
	public interface MultiItemSelectorContext<T, I extends Nameable & Matchable & Itemable<T>>
			extends SelectorComponentContext<T, I, MultiItemSelectorContext<T, I>> {

		/**
		 * Gets a values.
		 *
		 * @return a values
		 */
		List<String> getValues();

		/**
		 * Creates an empty {@link MultiItemSelectorContext}.
		 *
		 * @return empty context
		 */
		static <T, I extends Nameable & Matchable & Itemable<T>> MultiItemSelectorContext<T, I> empty() {
			return new DefaultMultiItemSelectorContext<>();
		}

		/**
		 * Creates an {@link MultiItemSelectorContext}.
		 *
		 * @return context
		 */
		static <T, I extends Nameable & Matchable & Itemable<T>> MultiItemSelectorContext<T, I> empty(Function<T, String> itemMapper) {
			return new DefaultMultiItemSelectorContext<>(itemMapper);
		}
	}

	private static class DefaultMultiItemSelectorContext<T, I extends Nameable & Matchable & Itemable<T>> extends
			BaseSelectorComponentContext<T, I, MultiItemSelectorContext<T, I>> implements MultiItemSelectorContext<T, I> {

		private Function<T, String> itemMapper = item -> item.toString();

		DefaultMultiItemSelectorContext() {
		}

		DefaultMultiItemSelectorContext(Function<T, String> itemMapper) {
			this.itemMapper = itemMapper;
		}

		@Override
		public List<String> getValues() {
			return getResultItems().stream()
					.map(i -> i.getItem())
					.map(i -> itemMapper.apply(i))
					.collect(Collectors.toList());
		}
	}

	private class DefaultRenderer implements Function<MultiItemSelectorContext<T, I>, List<AttributedString>> {

		@Override
		public List<AttributedString> apply(MultiItemSelectorContext<T, I> context) {
			List<AttributedString> out = new ArrayList<>();
			AttributedStringBuilder titleBuilder = new AttributedStringBuilder();
			titleBuilder.append(context.getName());
			titleBuilder.append(" ");

			if (context.isResult()) {
				titleBuilder.append(StringUtils.collectionToCommaDelimitedString(context.getValues()));
				out.add(titleBuilder.toAttributedString());
			}
			else {
				String filterStr = StringUtils.hasText(context.getInput()) ? ", filtering '" + context.getInput() + "'" : ", type to filter";
				titleBuilder.append(String.format("[Use arrows to move%s]", filterStr));
				out.add(titleBuilder.toAttributedString());
				context.getItemStateView().stream().forEach(e -> {
					AttributedStringBuilder builder = new AttributedStringBuilder();
					if (context.getCursorRow().intValue() == e.getIndex()) {
						builder.append("> ");
					}
					else {
						builder.append("  ");
					}
					if (e.isSelected()) {
						builder.append("[x]");
					}
					else {
						if (e.isEnabled()) {
							builder.append("[ ]");
						}
						else {
							builder.append("   ");
						}
					}
					builder.append(" " + e.getName());
					out.add(builder.toAttributedString());
				});
			}

			return out;
		}
	}
}
