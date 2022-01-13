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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Attributes;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.Display;
import org.jline.utils.InfoCmp.Capability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import static org.jline.keymap.KeyMap.del;

public class FieldInput {

	private final static Logger log = LoggerFactory.getLogger(FieldInput.class);
	private final Terminal terminal;
	private final BindingReader bindingReader;
	private final KeyMap<Operation> keyMap = new KeyMap<>();
	private final static Character[] chars = new Character[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
			'l',
			'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', '.', '-' };
	private StringBuilder searchBuf = new StringBuilder();
	private String name;
	private String defaultValue;
	private Function<FieldInputContext, List<AttributedString>> renderer = new DefaultDisplay();

	public FieldInput(Terminal terminal, String name, String defaultValue) {
		this.terminal = terminal;
		this.name = name;
		this.defaultValue = defaultValue;
		this.bindingReader = new BindingReader(terminal.reader());
		init();
	}

	protected void init() {
		bindKeys(keyMap);
	}

	/**
	 * Sets a display renderer.
	 *
	 * @param renderer the display renderer function
	 */
	public void setRenderer(Function<FieldInputContext, List<AttributedString>> renderer) {
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
	public List<AttributedString> render(FieldInputContext context) {
		return renderer.apply(context);
	}

	/**
	 * Run a component feature and return result.
	 *
	 * @return result from a component run
	 */
	public String run() {
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

			while (true) {
				display.resize(size.getRows(), size.getColumns());
				display.update(render(FieldInputContext.ofRunning(name, defaultValue, searchBuf.toString())), 0);
				Operation operation = bindingReader.readBinding(keyMap);

				switch (operation) {
					case CHAR:
						String lastBinding = bindingReader.getLastBinding();
						log.debug("readBinding {} {}", operation, lastBinding);
						searchBuf.append(lastBinding);
						break;
					case BACKSPACE:
						if (searchBuf.length() > 0) {
							searchBuf.deleteCharAt(searchBuf.length() - 1);
						}
						break;
					case EXIT:
						String value = searchBuf.toString();
						return StringUtils.hasText(value) ? value : defaultValue;
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

	protected void bindKeys(KeyMap<Operation> map) {
		defaultBindKeys(map);
	}

	private void defaultBindKeys(KeyMap<Operation> map) {
		map.bind(Operation.EXIT, "\r");
		map.bind(Operation.BACKSPACE, del());
		for (Character character : chars) {
			map.bind(Operation.CHAR, character.toString());
		}
	}

	protected enum Operation {
		EXIT, CHAR, BACKSPACE;
	}

	public interface FieldInputContext {
		boolean isResult();
		String getName();
		String getValue();
		String getDefaultValue();
		String getFilter();

		public static FieldInputContext ofResult(String name, String value) {
			return new DefaultFieldInputContext(true, name, value, null, null);
		}

		public static FieldInputContext ofRunning(String name, String defaultValue, String filter) {
			return new DefaultFieldInputContext(false, name, null, defaultValue, filter);
		}

		static class DefaultFieldInputContext implements FieldInputContext {
			private final boolean result;
			private final String name;
			private final String value;
			private final String defaultValue;
			private final String filter;

			public DefaultFieldInputContext(boolean result, String name, String value, String defaultValue,
					String filter) {
				this.result = result;
				this.name = name;
				this.value = value;
				this.defaultValue = defaultValue;
				this.filter = filter;
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
			public String getValue() {
				return value;
			}

			@Override
			public String getDefaultValue() {
				return defaultValue;
			}

			@Override
			public String getFilter() {
				return filter;
			}
		}
	}

	private static class DefaultDisplay implements Function<FieldInputContext, List<AttributedString>> {

		@Override
		public List<AttributedString> apply(FieldInputContext context) {

			AttributedStringBuilder builder = new AttributedStringBuilder();
			builder.append(context.getName());
			builder.append(" ");

			if (context.isResult()) {
				builder.append(context.getValue());
			}
			else  {
				String filter = context.getFilter();
				if (StringUtils.hasText(filter)) {
					builder.append(filter);
				}
				else {
					builder.append("[Default " + context.getDefaultValue() + "]");
				}
			}

			return Arrays.asList(builder.toAttributedString());
		}
	}

}
