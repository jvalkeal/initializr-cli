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
import java.util.List;
import java.util.stream.Collectors;

import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

import org.springframework.experimental.initializrcli.client.model.Metadata;
import org.springframework.experimental.initializrcli.component.SelectorItem;
import org.springframework.experimental.initializrcli.wizard.Wizard;
import org.springframework.experimental.initializrcli.wizard.Wizard.RunValuesHolder;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.ObjectUtils;

@ShellComponent
public class GenerateCommands extends AbstractInitializrCommands {

	@ShellMethod(key = "generate", value = "Generate project")
	public String generate(
		@ShellOption(help = "Path to extract") String path
	) {
		Metadata metadata = client.getMetadata();

		List<SelectorItem<String>> projectSelectorItems = metadata.getType().getValues().stream()
				.filter(ptv -> ObjectUtils.nullSafeEquals(ptv.getTags().get("format"), "project"))
				.map(ptv -> SelectorItem.of(ptv.getName(), ptv.getId()))
				.collect(Collectors.toList());

		List<SelectorItem<String>> languageSelectorItems = metadata.getLanguage().getValues().stream()
				.map(ptv -> SelectorItem.of(ptv.getName(), ptv.getId()))
				.collect(Collectors.toList());

		List<SelectorItem<String>> bootSelectorItems = metadata.getBootVersion().getValues().stream()
				.map(ptv -> SelectorItem.of(ptv.getName(), ptv.getId()))
				.collect(Collectors.toList());

		List<SelectorItem<String>> dependenciesSelectorItems = metadata.getDependencies().getValues().stream()
				.flatMap(dc -> dc.getValues().stream())
				.map(d -> SelectorItem.of(d.getName(), d.getId()))
				.collect(Collectors.toList());

		String defaultVersion = metadata.getVersion().getDefault();
		String defaultGroupId = metadata.getGroupId().getDefault();
		String defaultArtifact = metadata.getArtifactId().getDefault();
		String defaultName = metadata.getName().getDefault();
		String defaultDescription = metadata.getDescription().getDefault();
		String defaultPackageName = metadata.getPackageName().getDefault();

		List<SelectorItem<String>> packagingSelectorItems = metadata.getPackaging().getValues().stream()
				.map(ptv -> SelectorItem.of(ptv.getName(), ptv.getId()))
				.collect(Collectors.toList());

		List<SelectorItem<String>> javaVersionSelectorItems = metadata.getJavaVersion().getValues().stream()
				.map(ptv -> SelectorItem.of(ptv.getName(), ptv.getId()))
				.collect(Collectors.toList());

		Wizard wiz = new Wizard(getTerminal(), projectSelectorItems, languageSelectorItems, bootSelectorItems,
				dependenciesSelectorItems, defaultVersion, defaultGroupId, defaultArtifact, defaultName, defaultDescription,
				defaultPackageName, packagingSelectorItems, javaVersionSelectorItems);
		RunValuesHolder values = wiz.run();

		Path generated = client.generate(values.getProjectType(), values.getLanguageType(), values.getBootVersion(),
				values.getDependencies(), values.getVersion(), values.getGroupId(), values.getArtifact(), values.getName(),
				values.getDescription(), values.getPackageName(), values.getPackaging(), values.getJavaVersion());

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
}
