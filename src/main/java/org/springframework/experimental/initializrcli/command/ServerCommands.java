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

import java.util.Collections;
import java.util.stream.Stream;

import io.spring.initializr.generator.version.Version;
import io.spring.initializr.generator.version.VersionParser;
import io.spring.initializr.generator.version.VersionRange;

import org.springframework.experimental.initializrcli.client.model.Dependency;
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

	private final static VersionParser VERSION_PARSER_INSTANCE = new VersionParser(Collections.emptyList());

	@ShellMethod(key = "server info", value = "Show the Initializr server being used")
	public String info() {
		return client.info();
	}

	@ShellMethod(key = "server dependencies", value = "List supported dependencies")
	public Table dependencies(
		@ShellOption(help = "Search string to limit results", defaultValue = ShellOption.NULL) String search,
		@ShellOption(help = "Limit to compatibility version", defaultValue = ShellOption.NULL) String version
	) {
		Metadata metadata = client.getMetadata();

		Stream<String[]> header = Stream.<String[]>of(new String[] { "Id", "Name", "Description", "Required version" });
		Stream<String[]> rows = metadata.getDependencies().getValues().stream()
				.flatMap(dc -> dc.getValues().stream())
				.filter(d -> compatible(version, d))
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

	private static boolean compatible(String version, Dependency dependency) {
		if (!StringUtils.hasText(version) || !StringUtils.hasText(dependency.getVersionRange())) {
			return true;
		}
		Version parsedVersion = VERSION_PARSER_INSTANCE.parse(version);
		VersionRange parsedRange = VERSION_PARSER_INSTANCE.parseRange(dependency.getVersionRange());
		return parsedRange.match(parsedVersion);
	}
}
