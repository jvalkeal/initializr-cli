package org.springframework.experimental.initializrcli.config;

// import com.sun.jna.Native;
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


import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.TypeAccess;

import java.util.Base64;

// import com.sun.jna.Klass;
import com.sun.jna.CallbackReference;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import com.sun.jna.Library;
import com.sun.jna.Callback;
// import org.jline.terminal.impl.jna.win.Kernel32;

@NativeHint(
	options = { "-Djna.debug_load=true", "-Djna.debug_load.jna=true", "-H:Log=registerResource:3" },
	resources = @ResourceHint(patterns = {
		"completion/.*",
		"template/.*.st",
		"com/sun/jna/win32-x86-64/jnidispatch.dll"
	}),
	types = @TypeHint(
		types = {
			ArtifactId.class, BootVersion.class, Dependencies.class, Dependency.class, DependencyCategory.class,
			Description.class, GroupId.class, IdName.class, JavaVersion.class, JavaVersionValues.class, Language.class,
			LanguageValues.class, Metadata.class, Name.class, PackageName.class, Packaging.class, PackagingValues.class,
			ProjectType.class, ProjectTypeValue.class, Version.class,
			CallbackReference.class, Native.class, Structure.class, NativeLong.class, PointerByReference.class,
			IntByReference.class, Base64.Decoder.class
		},
		typeNames = {"com.sun.jna.Klass"},
		access = {
			TypeAccess.PUBLIC_CLASSES, TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.PUBLIC_FIELDS,
			TypeAccess.PUBLIC_METHODS, TypeAccess.DECLARED_CLASSES, TypeAccess.DECLARED_CONSTRUCTORS,
			TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_METHODS }),
		jdkProxies = @JdkProxyHint(
			// types = { Library.class, Callback.class},
			typeNames = {"com.sun.jna.Library", "com.sun.jna.Callback", "org.jline.terminal.impl.jna.linux.CLibrary", "org.jline.terminal.impl.jna.win.Kernel32"}
		)
)
public class InitializrNativeConfiguration implements NativeConfiguration {

}