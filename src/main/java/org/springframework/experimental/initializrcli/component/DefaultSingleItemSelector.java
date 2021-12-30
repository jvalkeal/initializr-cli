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
import java.util.Optional;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import org.springframework.util.StringUtils;

public class DefaultSingleItemSelector<T extends Nameable & Matchable>
		extends AbstractSelector<T, Optional<T>> {

	private String name;

	public DefaultSingleItemSelector(Terminal terminal, List<T> items, String name) {
		super(terminal, items, true);
		this.name = name;
	}

	@Override
	public Optional<T> select() {
		return run().stream().findFirst();
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
				builder.append("> " + e.name, AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.CYAN).bold());
			}
			else {
				builder.append("  " + e.name);
			}
			out.add(builder.toAttributedString());
		});

		return out;
	}
}
