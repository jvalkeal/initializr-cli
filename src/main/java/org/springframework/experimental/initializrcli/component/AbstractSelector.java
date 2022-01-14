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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Attributes;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.Display;
import org.jline.utils.InfoCmp.Capability;

import org.springframework.util.Assert;

import static org.jline.keymap.KeyMap.ctrl;
import static org.jline.keymap.KeyMap.del;
import static org.jline.keymap.KeyMap.key;

public abstract class AbstractSelector<T extends Nameable & Matchable & Enableable, S> {

	private final Terminal terminal;
	private final BindingReader bindingReader;
	private final KeyMap<Operation> keyMap = new KeyMap<>();
	private final List<T> items;
	private List<ItemState<T>> itemStates;
	private boolean exitSelects;
	private Comparator<T> comparator = (o1, o2) -> 0;
	private int maxItems = 5;

	public AbstractSelector(Terminal terminal, List<T> items, boolean exitSelects, Comparator<T> comparator) {
		Assert.notNull(terminal, "terminal must be set");
		this.terminal = terminal;
		this.items = items;
		this.exitSelects = exitSelects;
		this.bindingReader = new BindingReader(terminal.reader());
		if (comparator != null) {
			this.comparator = comparator;
		}
		init();
	}

	public void setMaxItems(int maxItems) {
		Assert.state(maxItems > 0 || maxItems < 33, "maxItems has to be between 1 and 32");
		this.maxItems = maxItems;
	}

	public abstract S select();

	protected Terminal getTerminal() {
		return terminal;
	}

	protected void init() {
		bindKeys(keyMap);
		buildStates();
	}

	private void buildStates() {
		AtomicInteger index = new AtomicInteger(0);
		this.itemStates = items.stream()
				.sorted(comparator)
				.map(item -> ItemState.of(item, item.getName(), index.getAndIncrement(), item.isEnabled()))
				.collect(Collectors.toList());
	}

	private ItemStateViewProjection buildItemStateView(int skip) {
		AtomicInteger reindex = new AtomicInteger(0);
		List<ItemState<T>> filtered = this.itemStates.stream()
			.filter(i -> {
				return i.matches(getSearch());
			})
			.map(i -> {
				i.index = reindex.getAndIncrement();
				return i;
			})
			.collect(Collectors.toList());
		List<ItemState<T>> items = filtered.stream()
			.skip(skip)
			.limit(maxItems)
			.collect(Collectors.toList());
		return new ItemStateViewProjection(items, filtered.size());
	}

	private class ItemStateViewProjection {
		List<ItemState<T>> items;
		int total;
		ItemStateViewProjection(List<ItemState<T>> items, int total) {
			this.items = items;
			this.total = total;
		}
	}

	private boolean stale = false;

	protected List<T> run() {
		Display display = new Display(terminal, false);
		Attributes attr = terminal.enterRawMode();
		Size size = new Size();
		try {
			terminal.puts(Capability.keypad_xmit);
			terminal.puts(Capability.cursor_invisible);
			terminal.writer().flush();
			size.copy(terminal.getSize());
			display.clear();
			display.reset();
			AtomicInteger start = new AtomicInteger(0);
			AtomicInteger pos = new AtomicInteger(0);
			while (true) {
				if (stale) {
					start.set(0);
					pos.set(0);
					stale = false;
				}
				display.resize(size.getRows(), size.getColumns());
				ItemStateViewProjection buildItemStateView = buildItemStateView(start.get());
				List<ItemState<T>> itemStateView = buildItemStateView.items;
				display.update(render(start.get() + pos.get(), itemStateView), 0);
				Operation operation = bindingReader.readBinding(keyMap);
				switch (operation) {
					case SELECT:
						if (!exitSelects) {
							itemStateView.forEach(i -> {
								if (i.index == start.get() + pos.get() && i.enabled) {
									i.selected = !i.selected;
								}
							});
						}
						break;
					case DOWN:
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
					case UP:
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
					case CHAR:
						String lastBinding = bindingReader.getLastBinding();
						searchBuf.append(lastBinding);
						stale = true;
						break;
					case BACKSPACE:
						if (searchBuf.length() > 0) {
							searchBuf.deleteCharAt(searchBuf.length() - 1);
						}
						break;
					case EXIT:
						if (exitSelects) {
							itemStateView.forEach(i -> {
								if (i.index == start.get() + pos.get()) {
									i.selected = !i.selected;
								}
							});
						}
						return this.itemStates.stream()
								.filter(i -> i.selected)
								.map(i -> i.item)
								.collect(Collectors.toList());
				}
			}
		}
		finally {
			terminal.setAttributes(attr);
			terminal.puts(Capability.keypad_local);
			terminal.puts(Capability.cursor_visible);
			display.update(Collections.emptyList(), 0);
		}
	}

	private StringBuilder searchBuf = new StringBuilder();

	protected String getSearch() {
		return this.searchBuf.toString();
	}

	protected void bindKeys(KeyMap<Operation> map) {
		defaultBindKeys(map);
	};

	private void defaultBindKeys(KeyMap<Operation> map) {
		map.bind(Operation.SELECT, " ");
		map.bind(Operation.DOWN, ctrl('E'), key(terminal, Capability.key_down));
		map.bind(Operation.UP, ctrl('Y'), key(terminal, Capability.key_up));
		map.bind(Operation.EXIT, "\r");
		map.bind(Operation.BACKSPACE, del());
		// skip 32 - SPACE, 127 - DEL
		for (char i = 33; i < KeyMap.KEYMAP_LENGTH - 1; i++) {
			map.bind(Operation.CHAR, Character.toString(i));
		}
	}

	abstract List<AttributedString> render(int cursorRow, List<ItemState<T>> itemStateView);

	protected enum Operation {
		SELECT, UP, DOWN, EXIT, CHAR, BACKSPACE;
	}

	public static class ItemState<T extends Matchable> implements Matchable {
		T item;
		String name;
		boolean selected;
		boolean enabled;
		int index;

		ItemState(T item, String name, int index, boolean enabled) {
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

		static <T extends Matchable> ItemState<T> of(T item, String name, int index, boolean enabled) {
			return new ItemState<T>(item, name, index, enabled);
		}
	}
}
