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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Attributes;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.Display;
import org.jline.utils.InfoCmp.Capability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.experimental.initializrcli.component.context.ComponentContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Base class for components.
 *
 * @author Janne Valkealahti
 */
public abstract class AbstractComponent<T extends ComponentContext<T>> {

	private final static Logger log = LoggerFactory.getLogger(AbstractComponent.class);
	public final static String OPERATION_EXIT = "EXIT";
	public final static String OPERATION_BACKSPACE = "BACKSPACE";
	public final static String OPERATION_CHAR = "CHAR";
	public final static String OPERATION_SELECT = "SELECT";
	public final static String OPERATION_DOWN = "DOWN";
	public final static String OPERATION_UP = "UP";

	private final Terminal terminal;
	private final BindingReader bindingReader;
	private final KeyMap<String> keyMap = new KeyMap<>();
	private final List<Consumer<T>> preRunHandlers = new ArrayList<>();
	private final List<Consumer<T>> postRunHandlers = new ArrayList<>();
	private Function<T, List<AttributedString>> renderer;
	private boolean printResults = true;

	public AbstractComponent(Terminal terminal) {
		Assert.notNull(terminal, "terminal must be set");
		this.terminal = terminal;
		this.bindingReader = new BindingReader(terminal.reader());
	}

	/**
	 * Gets a {@link Terminal}.
	 *
	 * @return a terminal
	 */
	public Terminal getTerminal() {
		return terminal;
	}

	/**
	 * Sets a display renderer.
	 *
	 * @param renderer the display renderer function
	 */
	public void setRenderer(Function<T, List<AttributedString>> renderer) {
		this.renderer = renderer;
	}

	/**
	 * Render to be shows content of a display with set display renderer using a
	 * given context.
	 *
	 * @param context the context
	 * @return list of attributed strings
	 */
	public List<AttributedString> render(T context) {
		log.debug("Rendering with context [{}] as class [{}] in [{}]", context, context.getClass(), this);
		return renderer.apply(context);
	}

	/**
	 * Adds a pre-run handler.
	 *
	 * @param handler the handler
	 */
	public void addPreRunHandler(Consumer<T> handler) {
		this.preRunHandlers.add(handler);
	}

	/**
	 * Adds a post-run handler.
	 *
	 * @param handler the handler
	 */
	public void addPostRunHandler(Consumer<T> handler) {
		this.postRunHandlers.add(handler);
	}

	/**
	 * Sets if results should be printed into a console, Defaults to {@code true}.
	 *
	 * @param printResults flag setting if results are printed
	 */
	public void setPrintResults(boolean printResults) {
		this.printResults = printResults;
	}

	/**
	 * Runs a component logic with a given context and returns updated context.
	 *
	 * @param context the context
	 * @return a context
	 */
	public final T run(ComponentContext<?> context) {
		bindKeyMap(keyMap);
		context = runPreRunHandlers(getThisContext(context));
		T run = runInternal(getThisContext(context));
		context = runPostRunHandlers(getThisContext(context));
		if (printResults) {
			printResults(context);
		}
		return run;
	}

	void printResults(ComponentContext<?> context) {
		log.debug("About to write result with incoming context [{}] as class [{}] in [{}]", context, context.getClass(),
				this);
		String out = render(getThisContext(context)).stream()
				.map(as -> as.toAnsi())
				.collect(Collectors.joining("\n"));
		log.debug("Writing result [{}] in [{}]", out, this);
		if (StringUtils.hasText(out)) {
			terminal.writer().println(out);
			terminal.writer().flush();
		}
	}

	protected abstract T getThisContext(ComponentContext<?> context);

	protected abstract boolean read(BindingReader bindingReader, KeyMap<String> keyMap, T context);

	protected abstract T runInternal(T context);

	protected abstract void bindKeyMap(KeyMap<String> keyMap);

	protected void loop(ComponentContext<?> context) {
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
				display.update(render(getThisContext(context)), 0);
				boolean exit = read(bindingReader, keyMap, getThisContext(context));
				if (exit) {
					break;
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

	protected T runPreRunHandlers(T context) {
		this.preRunHandlers.stream().forEach(c -> c.accept(context));
		return context;
	}

	protected T runPostRunHandlers(T context) {
		this.postRunHandlers.stream().forEach(c -> c.accept(context));
		return context;
	}

}
