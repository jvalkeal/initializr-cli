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
import java.util.Optional;
import java.util.function.Function;

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
import org.springframework.experimental.initializrcli.component.SingleItemSelector.SingleItemSelectorContext;
import org.springframework.util.StringUtils;

/**
 * Component able to pick single item.
 *
 * @author Janne Valkealahti
 */
public class SingleItemSelector<T, I extends Nameable & Matchable & Enableable & Itemable<T>>
		extends AbstractSelectorComponent<T, SingleItemSelectorContext<T, I>, I> {

	private SingleItemSelectorContext<T, I> currentContext;

	public SingleItemSelector(Terminal terminal, List<I> items, String name, Comparator<I> comparator) {
		super(terminal, name, items, true, comparator);
		setRenderer(new DefaultRenderer());
	}

	@Override
	protected SingleItemSelectorContext<T, I> getThisContext(ComponentContext<?> context) {
		if (context != null && currentContext == context) {
			return currentContext;
		}
		currentContext = SingleItemSelectorContext.empty(getItemMapper());
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
	protected SingleItemSelectorContext<T, I> runInternal(SingleItemSelectorContext<T, I> context) {
		super.runInternal(context);
		loop(context);
		return context;
	}

	public interface SingleItemSelectorContext<T, I extends Nameable & Matchable & Itemable<T>>
			extends SelectorComponentContext<T, I, SingleItemSelectorContext<T, I>> {
		Optional<I> getResultItem();
		Optional<String> getValue();

		static <X, I extends Nameable & Matchable & Itemable<X>> SingleItemSelectorContext<X, I> empty() {
			return new DefaultSingleItemSelectorContext<>();
		}
		static <X, I extends Nameable & Matchable & Itemable<X>> SingleItemSelectorContext<X, I> empty(Function<X, String> itemMapper) {
			return new DefaultSingleItemSelectorContext<>(itemMapper);
		}
	}

	private static class DefaultSingleItemSelectorContext<T, I extends Nameable & Matchable & Itemable<T>> extends
			DefaultSelectorComponentContext<T, I, SingleItemSelectorContext<T, I>> implements SingleItemSelectorContext<T, I> {

		private Function<T, String> itemMapper = item -> item.toString();

		DefaultSingleItemSelectorContext() {
		}

		DefaultSingleItemSelectorContext(Function<T, String> itemMapper) {
			this.itemMapper = itemMapper;
		}

		@Override
		public Optional<I> getResultItem() {
			return getResultItems().stream().findFirst();
		}

		@Override
		public Optional<String> getValue() {
			return getResultItem().map(item -> itemMapper.apply(item.getItem()));
		}

		@Override
		public String toString() {
			return "DefaultSingleItemSelectorContext [super=" + super.toString() + "]";
		}
	}

	private class DefaultRenderer implements Function<SingleItemSelectorContext<T, I>, List<AttributedString>> {

		@Override
		public List<AttributedString> apply(SingleItemSelectorContext<T, I> context) {
			List<AttributedString> out = new ArrayList<>();
			AttributedStringBuilder titleBuilder = new AttributedStringBuilder();
			titleBuilder.append(context.getName());
			titleBuilder.append(" ");

			if (context.isResult()) {
				if (context.getResultItem().isPresent()) {
					titleBuilder.append(context.getValue().orElse("<none>"));
				}

				out.add(titleBuilder.toAttributedString());
			}
			else {
				String filterStr = StringUtils.hasText(context.getInput()) ? ", filtering '" + context.getInput() + "'" : ", type to filter";
				titleBuilder.append(String.format("[Use arrows to move%s]", filterStr));
				out.add(titleBuilder.toAttributedString());
				context.getItemStateView().stream().forEach(e -> {
					AttributedStringBuilder builder = new AttributedStringBuilder();
					if (context.getCursorRow().intValue() == e.getIndex()) {
						builder.append("> " + e.getName());
					}
					else {
						builder.append("  " + e.getName());
					}
					out.add(builder.toAttributedString());
				});
			}

			return out;
		}
	}

}
