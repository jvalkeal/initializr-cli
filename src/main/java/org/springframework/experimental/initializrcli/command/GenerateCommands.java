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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

import org.springframework.experimental.initializrcli.client.model.Metadata;
import org.springframework.experimental.initializrcli.component.Matchable;
import org.springframework.experimental.initializrcli.component.Nameable;
import org.springframework.experimental.initializrcli.component.SelectorItem;
import org.springframework.experimental.initializrcli.component.DefaultMultiItemSelector.MultiItemSelectorContext;
import org.springframework.experimental.initializrcli.component.DefaultSingleItemSelector.SingleItemSelectorContext;
import org.springframework.experimental.initializrcli.component.FieldInput.FieldInputContext;
import org.springframework.experimental.initializrcli.wizard.InputWizard;
import org.springframework.experimental.initializrcli.wizard.InputWizard.InputWizardResult;
import org.springframework.experimental.initializrcli.wizard.Wizard;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@ShellComponent
public class GenerateCommands extends AbstractInitializrCommands {

	private final static String PROJECT_NAME = "Project";
	private final static String PROJECT_ID = "project";
	private final static String LANGUAGE_NAME = "Language";
	private final static String LANGUAGE_ID = "language";
	private final static String BOOT_VERSION_NAME = "Spring Boot";
	private final static String BOOT_VERSION_ID = "bootVersion";
	private final static String VERSION_NAME = "Version";
	private final static String VERSION_ID = "version";
	private final static String GROUP_NAME = "Group";
	private final static String GROUP_ID = "group";
	private final static String ARTIFACT_NAME = "Artifact";
	private final static String ARTIFACT_ID = "artifact";
	private final static String NAME_NAME = "Name";
	private final static String NAME_ID = "name";
	private final static String DESCRIPTION_NAME = "Description";
	private final static String DESCRIPTION_ID = "description";
	private final static String PACKAGE_NAME_NAME = "Package Name";
	private final static String PACKAGE_NAME_ID = "packageName";
	private final static String DEPENDENCIES_NAME = "Dependencies";
	private final static String DEPENDENCIES_ID = "dependencies";
	private final static String PACKAGING_NAME = "Packaging";
	private final static String PACKAGING_ID = "packaging";
	private final static String JAVA_VERSION_NAME = "Java";
	private final static String JAVA_VERSION_ID = "javaVersion";

	private final static StyledFieldInputRenderer FIELD_INPUT_RENDERER = new StyledFieldInputRenderer();
	private final static StyledSingleItemSelectorRenderer<SelectorItem<String>> SINGLE_ITEM_SELECTOR_RENDERER = new StyledSingleItemSelectorRenderer<>();
	private final static StyledMultiItemSelectorRenderer<SelectorItem<String>> MULTI_ITEM_SELECTOR_RENDERER = new StyledMultiItemSelectorRenderer<>();

	@ShellMethod(key = "init", value = "Initialize project")
	public String init(
		@ShellOption(help = "Path to extract") String path,
		@ShellOption(help = "Project", defaultValue = ShellOption.NULL) String project,
		@ShellOption(help = "Language", defaultValue = ShellOption.NULL) String language,
		@ShellOption(help = "Language", defaultValue = ShellOption.NULL) String bootVersion,
		@ShellOption(help = "Version", defaultValue = ShellOption.NULL) String version,
		@ShellOption(help = "Group", defaultValue = ShellOption.NULL) String group,
		@ShellOption(help = "Artifact", defaultValue = ShellOption.NULL) String artifact,
		@ShellOption(help = "Name", defaultValue = ShellOption.NULL) String name,
		@ShellOption(help = "Description", defaultValue = ShellOption.NULL) String description,
		@ShellOption(help = "Package Name", defaultValue = ShellOption.NULL) String packageName,
		@ShellOption(help = "Dependencies", defaultValue = ShellOption.NULL) List<String> dependencies,
		@ShellOption(help = "Packaging", defaultValue = ShellOption.NULL) String packaging,
		@ShellOption(help = "Java", defaultValue = ShellOption.NULL) String javaVersion
	) {
		Metadata metadata = client.getMetadata();

		Map<String, String> projectSelectItems = metadata.getType().getValues().stream()
				.filter(v -> ObjectUtils.nullSafeEquals(v.getTags().get("format"), "project"))
				.collect(Collectors.toMap(v -> v.getName(), v -> v.getId()));
		Map<String, String> languageSelectItems = metadata.getLanguage().getValues().stream()
				.collect(Collectors.toMap(v -> v.getName(), v -> v.getId()));
		Map<String, String> bootSelectItems = metadata.getBootVersion().getValues().stream()
				.collect(Collectors.toMap(v -> v.getName(), v -> v.getId()));
		String defaultVersion = metadata.getVersion().getDefault();
		String defaultGroupId = metadata.getGroupId().getDefault();
		String defaultArtifact = metadata.getArtifactId().getDefault();
		String defaultName = metadata.getName().getDefault();
		String defaultDescription = metadata.getDescription().getDefault();
		String defaultPackageName = metadata.getPackageName().getDefault();
		dependencies = dependencies == null ? Collections.emptyList() : dependencies;
		Map<String, String> dependenciesSelectItems = metadata.getDependencies().getValues().stream()
				.flatMap(dc -> dc.getValues().stream())
				.collect(Collectors.toMap(dc -> dc.getName(), dc -> dc.getId()));
		Map<String, String> packagingSelectItems = metadata.getPackaging().getValues().stream()
				.collect(Collectors.toMap(v -> v.getName(), v -> v.getId()));
		Map<String, String> javaVersionSelectItems = metadata.getJavaVersion().getValues().stream()
				.collect(Collectors.toMap(v -> v.getName(), v -> v.getId()));

		Comparator<SelectorItem<String>> nameComparator = (o1, o2) -> {
			return o1.getName().compareTo(o2.getName());
		};

		Comparator<SelectorItem<String>> javaVersionComparator = (o1, o2) -> {
			try {
				Integer oo1 = Integer.valueOf(o1.getName());
				Integer oo2 = Integer.valueOf(o2.getName());
				return oo1.compareTo(oo2);
			} catch (Exception e) {
			}
			return nameComparator.compare(o1, o2);
		};

		Wizard<InputWizardResult> wizard = InputWizard.builder(getTerminal())
				.withSingleInput(PROJECT_ID)
					.name(PROJECT_NAME)
					.currentValue(project)
					.selectItems(projectSelectItems)
					.sort(nameComparator)
					.renderer(SINGLE_ITEM_SELECTOR_RENDERER)
					.and()
				.withSingleInput(LANGUAGE_ID)
					.name(LANGUAGE_NAME)
					.currentValue(language)
					.selectItems(languageSelectItems)
					.sort(nameComparator)
					.renderer(SINGLE_ITEM_SELECTOR_RENDERER)
					.and()
				.withSingleInput(BOOT_VERSION_ID)
					.name(BOOT_VERSION_NAME)
					.currentValue(bootVersion)
					.selectItems(bootSelectItems)
					.sort(nameComparator.reversed())
					.renderer(SINGLE_ITEM_SELECTOR_RENDERER)
					.and()
				.withTextInput(VERSION_ID)
					.name(VERSION_NAME)
					.defaultValue(defaultVersion)
					.currentValue(version)
					.renderer(FIELD_INPUT_RENDERER)
					.and()
				.withTextInput(GROUP_ID)
					.name(GROUP_NAME)
					.defaultValue(defaultGroupId)
					.currentValue(group)
					.renderer(FIELD_INPUT_RENDERER)
					.and()
				.withTextInput(ARTIFACT_ID)
					.name(ARTIFACT_NAME)
					.defaultValue(defaultArtifact)
					.currentValue(artifact)
					.renderer(FIELD_INPUT_RENDERER)
					.and()
				.withTextInput(NAME_ID)
					.name(NAME_NAME)
					.defaultValue(defaultName)
					.currentValue(name)
					.renderer(FIELD_INPUT_RENDERER)
					.and()
				.withTextInput(DESCRIPTION_ID)
					.name(DESCRIPTION_NAME)
					.defaultValue(defaultDescription)
					.currentValue(description)
					.renderer(FIELD_INPUT_RENDERER)
					.and()
				.withTextInput(PACKAGE_NAME_ID)
					.name(PACKAGE_NAME_NAME)
					.defaultValue(defaultPackageName)
					.currentValue(packageName)
					.renderer(FIELD_INPUT_RENDERER)
					.and()
				.withMultiInput(DEPENDENCIES_ID)
					.name(DEPENDENCIES_NAME)
					.currentValue(dependencies)
					.selectItems(dependenciesSelectItems)
					.sort(nameComparator)
					.renderer(MULTI_ITEM_SELECTOR_RENDERER)
					.and()
				.withSingleInput(PACKAGING_ID)
					.name(PACKAGING_NAME)
					.currentValue(packaging)
					.selectItems(packagingSelectItems)
					.sort(nameComparator)
					.renderer(SINGLE_ITEM_SELECTOR_RENDERER)
					.and()
				.withSingleInput(JAVA_VERSION_ID)
					.name(JAVA_VERSION_NAME)
					.currentValue(javaVersion)
					.selectItems(javaVersionSelectItems)
					.sort(javaVersionComparator)
					.renderer(SINGLE_ITEM_SELECTOR_RENDERER)
					.and()
				.build();

		InputWizardResult result = wizard.run();

		Path generated = client.generate(result.singleInputs().get(PROJECT_ID),
				result.singleInputs().get(LANGUAGE_ID),
				result.singleInputs().get(BOOT_VERSION_ID),
				result.multiInputs().get(DEPENDENCIES_ID),
				result.textInputs().get(VERSION_ID),
				result.textInputs().get(GROUP_ID),
				result.textInputs().get(ARTIFACT_ID),
				result.textInputs().get(NAME_ID),
				result.textInputs().get(DESCRIPTION_ID),
				result.textInputs().get(PACKAGE_NAME_ID),
				result.singleInputs().get(PACKAGING_ID),
				result.singleInputs().get(JAVA_VERSION_ID));

		Path outPath = Paths.get(path);
		File outFile = outPath.toFile();
		if (!outFile.mkdirs()) {
			throw new RuntimeException(String.format("Can't create path %s", outFile.getAbsolutePath()));
		}
		Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");
		try {
			archiver.extract(generated.toFile(), outFile);
		} catch (Exception e) {
			throw new RuntimeException(String.format("Extraction error from %s to %s",
					generated.toFile().getAbsolutePath(), outFile.getAbsolutePath()), e);
		}
		return String.format("Extracted to %s", outFile.getAbsolutePath());
	}

	private static class StyledFieldInputRenderer implements Function<FieldInputContext, List<AttributedString>> {

		@Override
		public List<AttributedString> apply(FieldInputContext context) {
			AttributedStringBuilder builder = new AttributedStringBuilder();
			builder.append("?", AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.GREEN).bold());
			builder.append(" ");
			builder.append(context.getName(), AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.WHITE).bold());
			builder.append(" ");

			if (context.isResult()) {
				builder.append(context.getValue(), AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.BLUE));
			}
			else  {
				String filter = context.getFilter();
				if (StringUtils.hasText(filter)) {
					builder.append(filter);
				}
				else {
					builder.append("[Default " + context.getDefaultValue() + "]",
						AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.BLUE));
				}
			}

			return Arrays.asList(builder.toAttributedString());
		}
	}

	private static class StyledSingleItemSelectorRenderer<T extends Nameable & Matchable> implements Function<SingleItemSelectorContext<T>, List<AttributedString>> {

		@Override
		public List<AttributedString> apply(SingleItemSelectorContext<T> context) {
			List<AttributedString> out = new ArrayList<>();
			AttributedStringBuilder titleBuilder = new AttributedStringBuilder();
			titleBuilder.append("?", AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.GREEN).bold());
			titleBuilder.append(" ");
			titleBuilder.append(context.getName(), AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.WHITE).bold());
			titleBuilder.append(" ");

			if (context.isResult()) {
				titleBuilder.append(context.getValue(), AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.BLUE));
				out.add(titleBuilder.toAttributedString());
			}
			else {
				String filterStr = StringUtils.hasText(context.getFilter()) ? ", filtering '" + context.getFilter() + "'" : ", type to filter";
				titleBuilder.append(String.format("[Use arrows to move%s]", filterStr),
						AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.BLUE));
				out.add(titleBuilder.toAttributedString());
				context.getItemStateView().stream().forEach(e -> {
					AttributedStringBuilder builder = new AttributedStringBuilder();
					if (context.getCursorRow().intValue() == e.getIndex()) {
						builder.append("> " + e.getName(), AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.CYAN).bold());
					}
					else {
						builder.append("  " + e.getName());
					}
					out.add(builder.toAttributedString());
				});
			}

			return out;
		}
	}

	private static class StyledMultiItemSelectorRenderer<T extends Nameable & Matchable> implements Function<MultiItemSelectorContext<T>, List<AttributedString>> {

		@Override
		public List<AttributedString> apply(MultiItemSelectorContext<T> context) {
			List<AttributedString> out = new ArrayList<>();
			AttributedStringBuilder titleBuilder = new AttributedStringBuilder();
			titleBuilder.append("?", AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.GREEN).bold());
			titleBuilder.append(" ");
			titleBuilder.append(context.getName(), AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.WHITE).bold());
			titleBuilder.append(" ");

			if (context.isResult()) {
				titleBuilder.append(StringUtils.collectionToCommaDelimitedString(context.getValues()),
						AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.BLUE));
				out.add(titleBuilder.toAttributedString());
			}
			else {
				String filterStr = StringUtils.hasText(context.getFilter()) ? ", filtering '" + context.getFilter() + "'" : ", type to filter";
				titleBuilder.append(String.format("[Use arrows to move%s]", filterStr),
						AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.BLUE));
				out.add(titleBuilder.toAttributedString());
				context.getItemStateView().stream().forEach(e -> {
					AttributedStringBuilder builder = new AttributedStringBuilder();
					if (context.getCursorRow().intValue() == e.getIndex()) {
						builder.append("> ", AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.GREEN).bold());
					}
					else {
						builder.append("  ");
					}
					if (e.isSelected()) {
						builder.append("[x]", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
					}
					else {
						builder.append("[ ]", AttributedStyle.DEFAULT.bold());
					}
					builder.append(" " + e.getName());
					out.add(builder.toAttributedString());
				});
			}

			return out;
		}
	}
}
