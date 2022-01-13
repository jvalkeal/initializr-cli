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

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class DefaultMultiItemSelector<T extends Nameable & Matchable>
		extends AbstractSelector<T, List<T>> {

	private String name;
	private Function<MultiItemSelectorContext<T>, List<AttributedString>> renderer = new DefaultDisplay<>();

	public DefaultMultiItemSelector(Terminal terminal, List<T> items, String name, Comparator<T> comparator) {
		super(terminal, items, false, comparator);
		this.name  = name;
	}

	/**
	 * Sets a display renderer.
	 *
	 * @param renderer the display renderer function
	 */
	public void setRenderer(Function<MultiItemSelectorContext<T>, List<AttributedString>> renderer) {
		Assert.notNull(renderer, "renderer cannot be null");
		this.renderer = renderer;
	}

	/**
	 * Render to be shows content of a display with set display renderer using a
	 * given context.
	 *
	 * @param context the field input context
	 * @return list of attributed strings
	 */
	public List<AttributedString> render(MultiItemSelectorContext<T> context) {
		return renderer.apply(context);
	}

	@Override
	public List<T> select() {
		return run();
	}

	@Override
	List<AttributedString> render(int cursorRow, List<ItemState<T>> itemStateView) {
		return render(MultiItemSelectorContext.ofRunning(name, getSearch(), itemStateView, cursorRow));
	}

	public interface MultiItemSelectorContext<T extends Nameable & Matchable> {
		boolean isResult();
		String getName();
		List<String> getValues();
		String getFilter();
		List<ItemState<T>> getItemStateView();
		Integer getCursorRow();

		public static <T extends Nameable & Matchable> MultiItemSelectorContext<T> ofResult(String name, List<String> values) {
			return new DefaultMultiItemSelectorContext<>(true, name, values, null, null, null);
		}

		public static <T extends Nameable & Matchable> MultiItemSelectorContext<T> ofRunning(String name,
				String filter, List<ItemState<T>> itemStateView, Integer cursorRow) {
			return new DefaultMultiItemSelectorContext<>(false, name, null, filter, itemStateView, cursorRow);
		}

		static class DefaultMultiItemSelectorContext<T extends Nameable & Matchable> implements MultiItemSelectorContext<T> {
			private final boolean result;
			private final String name;
			private final List<String> values;
			private final String filter;
			private final List<ItemState<T>> itemStateView;
			private final Integer cursorRow;

			public DefaultMultiItemSelectorContext(boolean result, String name, List<String> values, String filter,
					List<ItemState<T>> itemStateView, Integer cursorRow) {
				this.result = result;
				this.name = name;
				this.values = values;
				this.filter = filter;
				this.itemStateView = itemStateView;
				this.cursorRow = cursorRow;
			}

			@Override
			public boolean isResult() {
				return result;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public List<String> getValues() {
				return values;
			}

			@Override
			public String getFilter() {
				return filter;
			}

			@Override
			public List<ItemState<T>> getItemStateView() {
				return itemStateView;
			}

			@Override
			public Integer getCursorRow() {
				return cursorRow;
			}
		}
	}

	private static class DefaultDisplay<T extends Nameable & Matchable>
			implements Function<MultiItemSelectorContext<T>, List<AttributedString>> {

		@Override
		public List<AttributedString> apply(MultiItemSelectorContext<T> context) {
			List<AttributedString> out = new ArrayList<>();
			AttributedStringBuilder titleBuilder = new AttributedStringBuilder();
			titleBuilder.append(context.getName());
			titleBuilder.append(" ");

			if (context.isResult()) {
				titleBuilder.append(StringUtils.collectionToCommaDelimitedString(context.getValues()));
				out.add(titleBuilder.toAttributedString());
			}
			else {
				String filterStr = StringUtils.hasText(context.getFilter()) ? ", filtering '" + context.getFilter() + "'" : ", type to filter";
				titleBuilder.append(String.format("[Use arrows to move%s]", filterStr));
				out.add(titleBuilder.toAttributedString());
				context.getItemStateView().stream().forEach(e -> {
					AttributedStringBuilder builder = new AttributedStringBuilder();
					if (context.getCursorRow().intValue() == e.index) {
						builder.append("> ");
					}
					else {
						builder.append("  ");
					}
					if (e.selected) {
						builder.append("[x]");
					}
					else {
						builder.append("[ ]");
					}
					builder.append(" " + e.name);
					out.add(builder.toAttributedString());
				});
			}

			return out;
		}
	}
}
