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
import java.util.List;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import org.springframework.util.StringUtils;

public class DefaultMultiItemSelector<T extends Nameable & Matchable>
		extends AbstractSelector<T, List<T>> {

	private String name;

	public DefaultMultiItemSelector(Terminal terminal, List<T> items, String name) {
		super(terminal, items, false);
		this.name  = name;
	}

	@Override
	public List<T> select() {
		return run();
	}

	@Override
	List<AttributedString> displayLines(int cursorRow, List<ItemState<T>> itemStateView) {
		List<AttributedString> out = new ArrayList<>();

		AttributedStringBuilder titleBuilder = new AttributedStringBuilder();
		titleBuilder.append("?", AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.GREEN).bold());
		titleBuilder.append(" ");
		titleBuilder.append(name, AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.WHITE).bold());
		titleBuilder.append(" ");
		String filterStr = StringUtils.hasText(getSearch()) ? ", filtering '" + getSearch() + "'" : ", type to filter";
		titleBuilder.append(String.format("[Use arrows to move%s]", filterStr),
				AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.BLUE));
		out.add(titleBuilder.toAttributedString());

		itemStateView.stream().forEach(e -> {
			AttributedStringBuilder builder = new AttributedStringBuilder();
			if (cursorRow == e.index) {
				builder.append("> ", AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.GREEN).bold());
			}
			else {
				builder.append("  ");
			}
			if (e.selected) {
				builder.append("[x]", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
			}
			else {
				builder.append("[ ]", AttributedStyle.DEFAULT.bold());
			}
			builder.append(" " + e.name);
			out.add(builder.toAttributedString());
		});

		return out;
	}

}
