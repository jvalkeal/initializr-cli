package org.springframework.experimental.initializrcli.old;

import static org.jline.keymap.KeyMap.ctrl;
import static org.jline.keymap.KeyMap.key;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Attributes;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.Display;
import org.jline.utils.InfoCmp.Capability;

public class RadioBoxSelector {

	private enum Operation {
		SELECT, FORWARD_ONE_LINE, BACKWARD_ONE_LINE, EXIT
	}

	private final Terminal terminal;
	private final Size size = new Size();
	private final BindingReader bindingReader;
	private final String title;
	private final List<OptionItem> optionStates;

	private static class OptionItem {
		String name;
		boolean selected;
		int index;

		OptionItem(String name, int index) {
			this.name = name;
			this.index = index;
		}

		static OptionItem of(String name, int index) {
			return new OptionItem(name, index);
		}
	}

	public RadioBoxSelector(Terminal terminal, String title, Collection<String> options) {
		this.terminal = terminal;
		this.title = title;
		this.bindingReader = new BindingReader(terminal.reader());
		AtomicInteger index = new AtomicInteger(1);
		optionStates = options.stream().map(o -> OptionItem.of(o, index.getAndIncrement())).collect(Collectors.toList());
	}

	public List<String> select() {
		// Display display = new Display(terminal, true);
		Display display = new Display(terminal, false);
		Attributes attr = terminal.enterRawMode();
		try {
			// terminal.puts(Capability.enter_ca_mode);
			terminal.puts(Capability.keypad_xmit);
			terminal.puts(Capability.cursor_invisible);
			terminal.writer().flush();
			size.copy(terminal.getSize());
			display.clear();
			display.reset();
			AtomicInteger selectRow = new AtomicInteger(1);
			KeyMap<Operation> keyMap = new KeyMap<>();
			bindKeys(keyMap);
			while (true) {
				display.resize(size.getRows(), size.getColumns());
				display.update(displayLines(selectRow.get()), size.cursorPos(0, this.title.length()));
				Operation op = bindingReader.readBinding(keyMap);
				switch (op) {
					case SELECT:
						this.optionStates.forEach(o -> {
							if (o.index == selectRow.get()) {
								o.selected = !o.selected;
							}
						});
						break;
					case FORWARD_ONE_LINE:
						selectRow.incrementAndGet();
						if (selectRow.get() > this.optionStates.size()) {
							selectRow.set(1);
						}
						break;
					case BACKWARD_ONE_LINE:
						selectRow.decrementAndGet();
						if (selectRow.get() < 1) {
							selectRow.set(this.optionStates.size());
						}
						break;
					case EXIT:
						return this.optionStates.stream().filter(o -> o.selected).map(o -> o.name)
								.collect(Collectors.toList());
				}
			}
		} finally {
			terminal.setAttributes(attr);
			// terminal.puts(Capability.exit_ca_mode);
			terminal.puts(Capability.keypad_local);
			terminal.puts(Capability.cursor_visible);
			terminal.writer().println();
			terminal.writer().println();
			terminal.writer().println();
			terminal.writer().println();
			terminal.writer().println();
			terminal.writer().flush();
		}
	}

	private List<AttributedString> displayLines(int cursorRow) {
		List<AttributedString> out = new ArrayList<>();
		out.add(new AttributedString(this.title));

		this.optionStates.stream().forEach(e -> {
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

	private void bindKeys(KeyMap<Operation> map) {
		map.bind(Operation.SELECT, " ");
		map.bind(Operation.FORWARD_ONE_LINE, "e", ctrl('E'), key(terminal, Capability.key_down));
		map.bind(Operation.BACKWARD_ONE_LINE, "y", ctrl('Y'), key(terminal, Capability.key_up));
		map.bind(Operation.EXIT, "\r");
	}
}
