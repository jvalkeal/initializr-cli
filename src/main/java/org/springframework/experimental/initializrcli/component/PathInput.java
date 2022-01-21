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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;

import org.springframework.experimental.initializrcli.component.PathInput.PathInputContext;
import org.springframework.experimental.initializrcli.component.context.ComponentContext;
import org.springframework.experimental.initializrcli.component.support.AbstractTextComponent;
import org.springframework.experimental.initializrcli.component.support.AbstractTextComponent.TextComponentContext;
import org.springframework.experimental.initializrcli.component.support.AbstractTextComponent.TextComponentContext.MessageLevel;
import org.springframework.util.StringUtils;;

/**
 * Component for a simple path input.
 *
 * @author Janne Valkealahti
 */
public class PathInput extends AbstractTextComponent<Path, PathInputContext> {

	private PathInputContext currentContext;
	private Function<String, Path> pathProvider = (path) -> Path.of(path);

	public PathInput(Terminal terminal) {
		this(terminal, null);
	}

	public PathInput(Terminal terminal, String name) {
		this(terminal, name, null);
	}

	public PathInput(Terminal terminal, String name, Function<PathInputContext, List<AttributedString>> renderer) {
		super(terminal, name, renderer != null ? renderer : new DefaultRenderer());
	}

	@Override
	protected PathInputContext getThisContext(ComponentContext<?> context) {
		if (context != null && currentContext == context) {
			return currentContext;
		}
		currentContext = PathInputContext.empty();
		currentContext.setName(getName());
		context.stream().forEach(e -> {
			currentContext.put(e.getKey(), e.getValue());
		});
		return currentContext;
	}

	@Override
	protected boolean read(BindingReader bindingReader, KeyMap<String> keyMap, PathInputContext context) {
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
				checkPath(input, context);
				break;
			case OPERATION_BACKSPACE:
				input = context.getInput();
				if (StringUtils.hasLength(input)) {
					input = input.substring(0, input.length() - 1);
				}
				context.setInput(input);
				checkPath(input, context);
				break;
			case OPERATION_EXIT:
				if (StringUtils.hasText(context.getInput())) {
					context.setResultValue(Paths.get(context.getInput()));
				}
				return true;
			default:
				break;
		}
		return false;
	}

	/**
	 * Sets a path provider.
	 *
	 * @param pathProvider the path provider
	 */
	public void setPathProvider(Function<String, Path> pathProvider) {
		this.pathProvider = pathProvider;
	}

	/**
	 * Resolves a {@link Path} from a given raw {@code path}.
	 *
	 * @param path the raw path
	 * @return a resolved path
	 */
	protected Path resolvePath(String path) {
		return this.pathProvider.apply(path);
	}

	private void checkPath(String path, PathInputContext context) {
		Path p = resolvePath(path);
		boolean isDirectory = Files.isDirectory(p);
		if (isDirectory) {
			context.setMessage("Directory exists", MessageLevel.ERROR);
		}
		else {
			context.setMessage("Path ok", MessageLevel.INFO);
		}
	}

	public interface PathInputContext extends TextComponentContext<Path, PathInputContext> {

		/**
		 * Gets an empty {@link PathInputContext}.
		 *
		 * @return empty path input context
		 */
		public static PathInputContext empty() {
			return new DefaultPathInputContext();
		}
	}

	private static class DefaultPathInputContext extends BaseTextComponentContext<Path, PathInputContext>
			implements PathInputContext {
	}

	private static class DefaultRenderer implements Function<PathInputContext, List<AttributedString>> {

		@Override
		public List<AttributedString> apply(PathInputContext context) {
			List<AttributedString> out = new ArrayList<>();
			AttributedStringBuilder builder = new AttributedStringBuilder();
			builder.append(context.getName());
			builder.append(" ");

			if (context.getResultValue() != null) {
				builder.append(context.getResultValue().toString());
			}
			else  {
				String input = context.getInput();
				if (StringUtils.hasText(input)) {
					builder.append(input);
				}
			}
			out.add(builder.toAttributedString());

			if (context.getResultValue() == null) {
				builder = new AttributedStringBuilder();
				if (StringUtils.hasText(context.getMessage())) {
					builder.append(messagaLevelString(context.getMessageLevel()));
					builder.append(" ");
					builder.append(context.getMessage());
					out.add(builder.toAttributedString());
				}
			}

			return out;
		}

		private String messagaLevelString(MessageLevel level) {
			if (level == MessageLevel.ERROR) {
				return ">>>";
			}
			else if (level == MessageLevel.WARN) {
				return ">>";
			}
			else {
				return ">";
			}
		}
	}
}
