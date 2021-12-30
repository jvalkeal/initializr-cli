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
package org.springframework.experimental.initializrcli.wizard;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import org.springframework.experimental.initializrcli.component.DefaultMultiItemSelector;
import org.springframework.experimental.initializrcli.component.DefaultSingleItemSelector;
import org.springframework.experimental.initializrcli.component.FieldInput;
import org.springframework.experimental.initializrcli.component.SelectorItem;
import org.springframework.util.StringUtils;

public class Wizard {

	private final Terminal terminal;
	private final List<SelectorItem<String>> projectSelectorItems;
	private final List<SelectorItem<String>> languageSelectorItems;
	private final List<SelectorItem<String>> bootSelectorItems;
	private final List<SelectorItem<String>> dependenciesSelectorItems;
	private final String defaultVersion;
	private final String defaultGroupId;
	private final String defaultArtifact;
	private final String defaultName;
	private final String defaultDescription;
	private final String defaultPackageName;
	private final List<SelectorItem<String>> packagingSelectorItems;
	private final List<SelectorItem<String>> javaVersionSelectorItems;

	public Wizard(Terminal terminal, List<SelectorItem<String>> projectSelectorItems,
			List<SelectorItem<String>> languageSelectorItems, List<SelectorItem<String>> bootSelectorItems,
			List<SelectorItem<String>> dependenciesSelectorItems, String defaultVersion, String defaultGroupId, String defaultArtifact,
			String defaultName, String defaultDescription, String defaultPackageName,
			List<SelectorItem<String>> packagingSelectorItems, List<SelectorItem<String>> javaVersionSelectorItems) {
		this.terminal = terminal;
		this.projectSelectorItems = projectSelectorItems;
		this.languageSelectorItems = languageSelectorItems;
		this.bootSelectorItems = bootSelectorItems;
		this.dependenciesSelectorItems = dependenciesSelectorItems;
		this.defaultVersion = defaultVersion;
		this.defaultGroupId = defaultGroupId;
		this.defaultArtifact = defaultArtifact;
		this.defaultName = defaultName;
		this.defaultDescription = defaultDescription;
		this.defaultPackageName = defaultPackageName;
		this.packagingSelectorItems = packagingSelectorItems;
		this.javaVersionSelectorItems = javaVersionSelectorItems;
	}

	public RunValuesHolder run() {
		String projectType = runSingleSelector(projectSelectorItems, "Project");
		writeSingleSelector("Project", projectType);

		String languageType = runSingleSelector(languageSelectorItems, "Language");
		writeSingleSelector("Language", projectType);

		String bootVersion = runSingleSelector(bootSelectorItems, "Spring Boot");
		writeSingleSelector("Spring Boot", bootVersion);

		List<String> dependencies = runMultiSelector(dependenciesSelectorItems, "Dependencies");
		writeMultiSelector("Dependencies", dependencies);

		String version = runInputField("Version", defaultVersion);
		writeInputField("Version", version);

		String groupId = runInputField("Group", defaultGroupId);
		writeInputField("Group", groupId);

		String artifact = runInputField("Artifact", defaultArtifact);
		writeInputField("Artifact", artifact);

		String name = runInputField("Name", defaultName);
		writeInputField("Name", name);

		String description = runInputField("Description", defaultDescription);
		writeInputField("Description", description);

		String packageName = runInputField("Package Name", defaultPackageName);
		writeInputField("Package Name", packageName);

		String packaging = runSingleSelector(packagingSelectorItems, "Packaging");
		writeSingleSelector("Packaging", packaging);

		String javaVersion = runSingleSelector(javaVersionSelectorItems, "Java");
		writeSingleSelector("Java", javaVersion);

		return new RunValuesHolder(projectType, languageType, bootVersion, dependencies, version, groupId, artifact, name,
				description, packageName, packaging, javaVersion);
	}

	private List<String> runMultiSelector(List<SelectorItem<String>> selectorItems, String name) {
		DefaultMultiItemSelector<SelectorItem<String>> selector = new DefaultMultiItemSelector<>(terminal,
				selectorItems, name);
		List<SelectorItem<String>> select = selector.select();
		return select.stream().map(i -> i.getItem()).collect(Collectors.toList());
	}

	private void writeMultiSelector(String name, List<String> values) {
		AttributedStringBuilder builder = new AttributedStringBuilder();
		builder.append("?", AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.GREEN).bold());
		builder.append(" ");
		builder.append(name, AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.WHITE).bold());
		builder.append(" ");
		builder.append(StringUtils.collectionToCommaDelimitedString(values),
				AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.BLUE));
		terminal.writer().println(builder.toAnsi());
		terminal.writer().flush();
	}

	private String runSingleSelector(List<SelectorItem<String>> selectorItems, String name) {
		DefaultSingleItemSelector<SelectorItem<String>> selector = new DefaultSingleItemSelector<>(terminal,
				selectorItems, name);
		Optional<SelectorItem<String>> select = selector.select();
		String type = select.map(i -> i.getItem()).orElse("<none>");
		return type;
	}

	private void writeSingleSelector(String name, String value) {
		AttributedStringBuilder builder = new AttributedStringBuilder();
		builder.append("?", AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.GREEN).bold());
		builder.append(" ");
		builder.append(name, AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.WHITE).bold());
		builder.append(" ");
		builder.append(value, AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.BLUE));
		terminal.writer().println(builder.toAnsi());
		terminal.writer().flush();
	}

	private String runInputField(String name, String defaultValue) {
		FieldInput selector = new FieldInput(terminal, name, defaultValue);
		return selector.run();
	}

	private void writeInputField(String name, String value) {
		AttributedStringBuilder builder = new AttributedStringBuilder();
		builder.append("?", AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.GREEN).bold());
		builder.append(" ");
		builder.append(name, AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.WHITE).bold());
		builder.append(" ");
		builder.append(value, AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.BLUE));
		terminal.writer().println(builder.toAnsi());
		terminal.writer().flush();
	}

	public static class RunValuesHolder {

		private String projectType;
		private String languageType;
		private String bootVersion;
		private List<String> dependencies;
		private String version;
		private String groupId;
		private String artifact;
		private String name;
		private String description;
		private String packageName;
		private String packaging;
		private String javaVersion;

		public RunValuesHolder(String projectType, String languageType, String bootVersion, List<String> dependencies,
				String version, String groupId, String artifact, String name, String description, String packageName, String packaging,
				String javaVersion) {
			this.projectType = projectType;
			this.languageType = languageType;
			this.bootVersion = bootVersion;
			this.dependencies = dependencies;
			this.version = version;
			this.groupId = groupId;
			this.artifact = artifact;
			this.name = name;
			this.description = description;
			this.packageName = packageName;
			this.packaging = packaging;
			this.javaVersion = javaVersion;
		}

		public String getProjectType() {
			return projectType;
		}

		public String getLanguageType() {
			return languageType;
		}

		public String getBootVersion() {
			return bootVersion;
		}

		public List<String> getDependencies() {
			return dependencies;
		}

		public String getVersion() {
			return version;
		}

		public String getGroupId() {
			return groupId;
		}

		public String getArtifact() {
			return artifact;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public String getPackageName() {
			return packageName;
		}

		public String getPackaging() {
			return packaging;
		}

		public String getJavaVersion() {
			return javaVersion;
		}
	}
}
