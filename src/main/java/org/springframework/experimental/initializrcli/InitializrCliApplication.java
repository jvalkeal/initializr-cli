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
package org.springframework.experimental.initializrcli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.experimental.initializrcli.client.model.ArtifactId;
import org.springframework.experimental.initializrcli.client.model.BootVersion;
import org.springframework.experimental.initializrcli.client.model.Dependencies;
import org.springframework.experimental.initializrcli.client.model.Dependency;
import org.springframework.experimental.initializrcli.client.model.DependencyCategory;
import org.springframework.experimental.initializrcli.client.model.Description;
import org.springframework.experimental.initializrcli.client.model.GroupId;
import org.springframework.experimental.initializrcli.client.model.IdName;
import org.springframework.experimental.initializrcli.client.model.JavaVersion;
import org.springframework.experimental.initializrcli.client.model.JavaVersion.JavaVersionValues;
import org.springframework.experimental.initializrcli.client.model.Language;
import org.springframework.experimental.initializrcli.client.model.Language.LanguageValues;
import org.springframework.experimental.initializrcli.client.model.Metadata;
import org.springframework.experimental.initializrcli.client.model.Name;
import org.springframework.experimental.initializrcli.client.model.PackageName;
import org.springframework.experimental.initializrcli.client.model.Packaging;
import org.springframework.experimental.initializrcli.client.model.Packaging.PackagingValues;
import org.springframework.experimental.initializrcli.client.model.ProjectType;
import org.springframework.experimental.initializrcli.client.model.ProjectType.ProjectTypeValue;
import org.springframework.experimental.initializrcli.client.model.Version;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;

@TypeHint(types = {
		ArtifactId.class, BootVersion.class, Dependencies.class, Dependency.class, DependencyCategory.class,
		Description.class, GroupId.class, IdName.class, JavaVersion.class, JavaVersionValues.class, Language.class,
		LanguageValues.class, Metadata.class, Name.class, PackageName.class, Packaging.class, PackagingValues.class,
		ProjectType.class, ProjectTypeValue.class, Version.class
}, access = { TypeAccess.PUBLIC_CLASSES, TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.PUBLIC_FIELDS,
		TypeAccess.PUBLIC_METHODS, TypeAccess.DECLARED_CLASSES, TypeAccess.DECLARED_CONSTRUCTORS,
		TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_METHODS })
@SpringBootApplication
public class InitializrCliApplication {

	public static void main(String[] args) {
		SpringApplication.run(InitializrCliApplication.class, args);
	}
}
