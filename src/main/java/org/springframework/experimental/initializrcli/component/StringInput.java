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
import java.util.List;
import java.util.function.Function;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;

import org.springframework.experimental.initializrcli.component.StringInput.StringInputContext;
import org.springframework.experimental.initializrcli.component.context.ComponentContext;
import org.springframework.experimental.initializrcli.component.support.AbstractTextComponent;
import org.springframework.experimental.initializrcli.component.support.AbstractTextComponent.TextComponentContext;
import org.springframework.util.StringUtils;

/**
 * Component for a simple string input.
 *
 * @author Janne Valkealahti
 */
public class StringInput extends AbstractTextComponent<String, StringInputContext> {

	private final String defaultValue;
	private StringInputContext currentContext;

	public StringInput(Terminal terminal) {
		this(terminal, null, null, null);
	}

	public StringInput(Terminal terminal, String name, String defaultValue) {
		this(terminal, name, defaultValue, null);
	}

	public StringInput(Terminal terminal, String name, String defaultValue,
			Function<StringInputContext, List<AttributedString>> renderer) {
		super(terminal, name, renderer != null ? renderer : new DefaultRenderer());
		this.defaultValue = defaultValue;
	}

	@Override
	protected StringInputContext getThisContext(ComponentContext<?> context) {
		if (context != null && currentContext == context) {
			return currentContext;
		}
		currentContext = StringInputContext.of(defaultValue);
		currentContext.setName(getName());
		context.stream().forEach(e -> {
			currentContext.put(e.getKey(), e.getValue());
		});
		return currentContext;
	}

	@Override
	protected boolean read(BindingReader bindingReader, KeyMap<String> keyMap, StringInputContext context) {
		String operation = bindingReader.readBinding(keyMap);
		String input;
		switch (operation) {
			case OPERATION_CHAR:
				String lastBinding = bindingReader.getLastBinding();
				input = context.getInput();
				if (input == null) {
					input = lastBinding;
				}
				else {
					input = input + lastBinding;
				}
				context.setInput(input);
				break;
			case OPERATION_BACKSPACE:
				input = context.getInput();
				if (StringUtils.hasLength(input)) {
					input = input.substring(0, input.length() - 1);
				}
				context.setInput(input);
				break;
			case OPERATION_EXIT:
				if (StringUtils.hasText(context.getInput())) {
					context.setResultValue(context.getInput());
				}
				else if (context.getDefaultValue() != null) {
					context.setResultValue(context.getDefaultValue());
				}
				return true;
			default:
				break;
		}
		return false;
	}

	public interface StringInputContext extends TextComponentContext<String, StringInputContext> {

		/**
		 * Gets a default value.
		 *
		 * @return a default value
		 */
		String getDefaultValue();

		/**
		 * Sets a default value.
		 *
		 * @param defaultValue the default value
		 */
		void setDefaultValue(String defaultValue);

		/**
		 * Gets an empty {@link StringInputContext}.
		 *
		 * @return empty path input context
		 */
		public static StringInputContext empty() {
			return of(null);
		}

		/**
		 * Gets an {@link StringInputContext}.
		 *
		 * @return path input context
		 */
		public static StringInputContext of(String defaultValue) {
			return new DefaultStringInputContext(defaultValue);
		}
	}

	private static class DefaultStringInputContext extends BaseTextComponentContext<String, StringInputContext>
			implements StringInputContext {

		private String defaultValue;

		public DefaultStringInputContext(String defaultValue) {
			this.defaultValue = defaultValue;
		}

		@Override
		public String getDefaultValue() {
			return defaultValue;
		}

		@Override
		public void setDefaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
		}

	}

	private static class DefaultRenderer implements Function<StringInputContext, List<AttributedString>> {

		@Override
		public List<AttributedString> apply(StringInputContext context) {
			AttributedStringBuilder builder = new AttributedStringBuilder();
			builder.append(context.getName());
			builder.append(" ");

			if (context.getResultValue() != null) {
				builder.append(context.getResultValue());
			}
			else  {
				String input = context.getInput();
				if (StringUtils.hasText(input)) {
					builder.append(input);
				}
				else {
					builder.append("[Default " + context.getDefaultValue() + "]");
				}
			}

			return Arrays.asList(builder.toAttributedString());
		}
	}
}