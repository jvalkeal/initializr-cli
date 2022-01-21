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
package org.springframework.experimental.initializrcli.component.support;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp.Capability;

import org.springframework.experimental.initializrcli.component.context.BaseComponentContext;
import org.springframework.experimental.initializrcli.component.context.ComponentContext;
import org.springframework.experimental.initializrcli.component.support.AbstractSelectorComponent.SelectorComponentContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import static org.jline.keymap.KeyMap.ctrl;
import static org.jline.keymap.KeyMap.del;
import static org.jline.keymap.KeyMap.key;

/**
 * Base component for selectors which provide selectable lists.
 *
 * @author Janne Valkealahti
 */
public abstract class AbstractSelectorComponent<T, C extends SelectorComponentContext<T, I, C>, I extends Nameable & Matchable & Enableable & Itemable<T>>
		extends AbstractComponent<C> {

	protected final String name;
	private final List<I> items;
	private Comparator<I> comparator = (o1, o2) -> 0;
	private boolean exitSelects;
	private int maxItems = 5;
	private Function<T, String> itemMapper = item -> item.toString();

	public AbstractSelectorComponent(Terminal terminal, String name, List<I> items, boolean exitSelects,
			Comparator<I> comparator) {
		super(terminal);
		this.name = name;
		this.items = items;
		this.exitSelects = exitSelects;
		if (comparator != null) {
			this.comparator = comparator;
		}
	}

	public void setMaxItems(int maxItems) {
		Assert.state(maxItems > 0 || maxItems < 33, "maxItems has to be between 1 and 32");
		this.maxItems = maxItems;
	}

	public void setItemMapper(Function<T, String> itemMapper) {
		Assert.notNull(itemMapper, "itemMapper cannot be null");
		this.itemMapper = itemMapper;
	}

	public Function<T, String> getItemMapper() {
		return itemMapper;
	}

	protected List<I> getItems() {
		return items;
	}

	@Override
	protected void bindKeyMap(KeyMap<String> keyMap) {
		keyMap.bind(OPERATION_SELECT, " ");
		keyMap.bind(OPERATION_DOWN, ctrl('E'), key(getTerminal(), Capability.key_down));
		keyMap.bind(OPERATION_UP, ctrl('Y'), key(getTerminal(), Capability.key_up));
		keyMap.bind(OPERATION_EXIT, "\r");
		keyMap.bind(OPERATION_BACKSPACE, del());
		// skip 32 - SPACE, 127 - DEL
		for (char i = 33; i < KeyMap.KEYMAP_LENGTH - 1; i++) {
			keyMap.bind(OPERATION_CHAR, Character.toString(i));
		}
	}

	@Override
	protected C runInternal(C context) {
		C thisContext = getThisContext(context);
		ItemStateViewProjection buildItemStateView = buildItemStateView(start.get(), thisContext);
		List<ItemState<I>> itemStateView = buildItemStateView.items;
		thisContext.setItemStateView(itemStateView);
		thisContext.setCursorRow(start.get() + pos.get());
		return thisContext;
	}

	private boolean stale = false;
	AtomicInteger start = new AtomicInteger(0);
	AtomicInteger pos = new AtomicInteger(0);

	@Override
	protected boolean read(BindingReader bindingReader, KeyMap<String> keyMap, C context) {

		if (stale) {
			start.set(0);
			pos.set(0);
			stale = false;
		}
		C thisContext = getThisContext(context);
		ItemStateViewProjection buildItemStateView = buildItemStateView(start.get(), thisContext);
		List<ItemState<I>> itemStateView = buildItemStateView.items;
		String operation = bindingReader.readBinding(keyMap);
		switch (operation) {
			case OPERATION_SELECT:
				if (!exitSelects) {
					itemStateView.forEach(i -> {
						if (i.index == start.get() + pos.get() && i.enabled) {
							i.selected = !i.selected;
						}
					});
				}
				break;
			case OPERATION_DOWN:
				if (start.get() + pos.get() + 1 < itemStateView.size()) {
					pos.incrementAndGet();
				}
				else if (start.get() + pos.get() + 1 >= buildItemStateView.total) {
					start.set(0);
					pos.set(0);
				}
				else {
					start.incrementAndGet();
				}
				break;
			case OPERATION_UP:
				if (start.get() > 0 && pos.get() == 0) {
					start.decrementAndGet();
				}
				else if (start.get() + pos.get() >= itemStateView.size()) {
					pos.decrementAndGet();
				}
				else if (start.get() + pos.get() <= 0) {
					start.set(buildItemStateView.total - Math.min(maxItems, itemStateView.size()));
					pos.set(itemStateView.size() - 1);
				}
				else {
					pos.decrementAndGet();
				}
				break;
			case OPERATION_CHAR:
				String lastBinding = bindingReader.getLastBinding();
				String input1 = thisContext.getInput();
				if (input1 == null) {
					input1 = lastBinding;
				}
				else {
					input1 = input1 + lastBinding;
				}
				thisContext.setInput(input1);

				stale = true;
				break;
			case OPERATION_BACKSPACE:
				String input2 = thisContext.getInput();
				if (StringUtils.hasLength(input2)) {
					input2 = input2.substring(0, input2.length() - 1);
				}
				thisContext.setInput(input2);
				break;
			case OPERATION_EXIT:
				if (exitSelects) {
					itemStateView.forEach(i -> {
						if (i.index == start.get() + pos.get()) {
							i.selected = !i.selected;
						}
					});
				}
				List<I> values = thisContext.getItemStateView().stream()
						.filter(i -> i.selected)
						.map(i -> i.item)
						.collect(Collectors.toList());
				thisContext.setResultItems(values);
				return true;
			default:
				break;
		}
		thisContext.setCursorRow(start.get() + pos.get());
		buildItemStateView = buildItemStateView(start.get(), thisContext);
		thisContext.setItemStateView(buildItemStateView.items);
		return false;
	}

	private ItemStateViewProjection buildItemStateView(int skip, SelectorComponentContext<T, I, ?> context) {
		List<ItemState<I>> itemStates = context.getItemStates();
		if (itemStates == null) {
			AtomicInteger index = new AtomicInteger(0);
			itemStates = context.getItems().stream()
					.sorted(comparator)
					.map(item -> ItemState.of(item, item.getName(), index.getAndIncrement(), item.isEnabled()))
					.collect(Collectors.toList());
			context.setItemStates(itemStates);
		}
		AtomicInteger reindex = new AtomicInteger(0);
		List<ItemState<I>> filtered = itemStates.stream()
			.filter(i -> {
				return i.matches(context.getInput());
			})
			.map(i -> {
				i.index = reindex.getAndIncrement();
				return i;
			})
			.collect(Collectors.toList());
		List<ItemState<I>> items = filtered.stream()
			.skip(skip)
			.limit(maxItems)
			.collect(Collectors.toList());
		return new ItemStateViewProjection(items, filtered.size());
	}

	private class ItemStateViewProjection {
		List<ItemState<I>> items;
		int total;
		ItemStateViewProjection(List<ItemState<I>> items, int total) {
			this.items = items;
			this.total = total;
		}
	}

	public interface SelectorComponentContext<T, I extends Nameable & Matchable & Itemable<T>, C extends SelectorComponentContext<T, I, C>>
			extends ComponentContext<C> {
		String getName();
		void setName(String name);
		String getInput();
		void setInput(String input);
		List<ItemState<I>> getItemStates();
		void setItemStates(List<ItemState<I>> itemStateView);
		List<ItemState<I>> getItemStateView();
		void setItemStateView(List<ItemState<I>> itemStateView);
		boolean isResult();
		Integer getCursorRow();
		void setCursorRow(Integer cursorRow);
		List<I> getItems();
		void setItems(List<I> items);
		List<I> getResultItems();
		void setResultItems(List<I> items);

		static <T, I extends Nameable & Matchable & Itemable<T>, C extends SelectorComponentContext<T, I, C>> SelectorComponentContext<T, I, C> empty() {
			return new DefaultSelectorComponentContext<>();
		}

	}

	protected static class DefaultSelectorComponentContext<T, I extends Nameable & Matchable & Itemable<T>, C extends SelectorComponentContext<T, I, C>>
			extends BaseComponentContext<C> implements SelectorComponentContext<T, I, C> {

		private String name;
		private String input;
		private List<ItemState<I>> itemStates;// = new ArrayList<>();
		private List<ItemState<I>> itemStateView;// = new ArrayList<>();
		private Integer cursorRow;
		private List<I> items;
		private List<I> resultItems;

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String getInput() {
			return input;
		}

		@Override
		public void setInput(String input) {
			this.input = input;
		}

		@Override
		public List<ItemState<I>> getItemStates() {
			return itemStates;
		}

		@Override
		public void setItemStates(List<ItemState<I>> itemStates) {
			this.itemStates = itemStates;
		};

		@Override
		public List<ItemState<I>> getItemStateView() {
			return itemStateView;
		}

		@Override
		public void setItemStateView(List<ItemState<I>> itemStateView) {
			this.itemStateView = itemStateView;
		};

		@Override
		public boolean isResult() {
			return resultItems != null;
		}

		@Override
		public Integer getCursorRow() {
			return cursorRow;
		}

		public void setCursorRow(Integer cursorRow) {
			this.cursorRow = cursorRow;
		};

		@Override
		public List<I> getItems() {
			return items;
		}

		@Override
		public void setItems(List<I> items) {
			this.items = items;
		}

		@Override
		public List<I> getResultItems() {
			return resultItems;
		}

		@Override
		public void setResultItems(List<I> resultItems) {
			this.resultItems = resultItems;
		}

		@Override
		public String toString() {
			return "DefaultSelectorComponentContext [cursorRow=" + cursorRow + "]";
		}
	}

	public static class ItemState<I extends Matchable> implements Matchable {
		I item;
		String name;
		boolean selected;
		boolean enabled;
		int index;

		ItemState(I item, String name, int index, boolean enabled) {
			this.item = item;
			this.name = name;
			this.index = index;
			this.enabled = enabled;
		}

		public boolean matches(String match) {
			return item.matches(match);
		};

		public int getIndex() {
			return index;
		}

		public String getName() {
			return name;
		}

		public boolean isSelected() {
			return selected;
		}

		public boolean isEnabled() {
			return enabled;
		}

		static <I extends Matchable> ItemState<I> of(I item, String name, int index, boolean enabled) {
			return new ItemState<I>(item, name, index, enabled);
		}
	}

}
