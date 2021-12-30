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
package org.springframework.experimental.initializrcli.command;

import java.util.stream.Stream;

import org.springframework.experimental.initializrcli.client.model.Metadata;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.Table;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;
import org.springframework.util.StringUtils;

@ShellComponent
public class ServerCommands extends AbstractInitializrCommands {

	@ShellMethod(key = "server info", value = "Show the Initializr server being used")
	public String info() {
		return client.info();
	}

	@ShellMethod(key = "server dependencies", value = "List supported dependencies")
	public Table dependencies(
		@ShellOption(help = "Search string to limit results", defaultValue = ShellOption.NULL) String search
	) {
		Metadata metadata = client.getMetadata();

		Stream<String[]> header = Stream.<String[]>of(new String[] { "Id", "Name", "Description", "Required version" });
		Stream<String[]> rows = metadata.getDependencies().getValues().stream()
				.flatMap(dc -> dc.getValues().stream())
				.map(d -> new String[] { d.getId(), d.getName(), d.getDescription(), d.getVersionRange() })
				.filter(d -> matches(d, search));
		String[][] data = Stream.concat(header, rows).toArray(String[][]::new);

		TableModel model = new ArrayTableModel(data);
		TableBuilder tableBuilder = new TableBuilder(model);
		return tableBuilder.addFullBorder(BorderStyle.fancy_light).build();
	}

	private static boolean matches(String[] array, String search) {
		if (!StringUtils.hasText(search)) {
			return true;
		}
		search = search.toLowerCase();
		for (String field : array) {
			if (StringUtils.hasText(field) && field.toLowerCase().contains(search)) {
				return true;
			}
		}
		return false;
	}
}
