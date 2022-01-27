package org.springframework.experimental.initializrcli.config;

import java.util.Base64;

import com.sun.jna.CallbackReference;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

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
import org.springframework.nativex.hint.FieldHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.MethodHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
	// options = { "-H:Log=registerResource:3", "-H:ReflectionConfigurationResources", "-H:JNIConfigurationResources" },
	// options = { "-H:+PrintAnalysisCallTree", "-H:GenerateDebugInfo=1" },
	resources = @ResourceHint(
		patterns = {
			"completion/.*",
			"template/.*.st",
			"com/sun/jna/win32-x86-64/jnidispatch.dll",
			"com/sun/jna/linux-x86-64/libjnidispatch.so"
		}),
	types = {
		@TypeHint(
			types = {
				ArtifactId.class, BootVersion.class, Dependencies.class, Dependency.class, DependencyCategory.class,
				Description.class, GroupId.class, IdName.class, JavaVersion.class, JavaVersionValues.class, Language.class,
				LanguageValues.class, Metadata.class, Name.class, PackageName.class, Packaging.class, PackagingValues.class,
				ProjectType.class, ProjectTypeValue.class, Version.class, CallbackReference.class, Native.class,
				NativeLong.class, PointerByReference.class, IntByReference.class, Base64.Decoder.class
			},
			typeNames = { "com.sun.jna.Klass" },
			access = {
				TypeAccess.PUBLIC_CLASSES, TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.PUBLIC_FIELDS,
				TypeAccess.PUBLIC_METHODS, TypeAccess.DECLARED_CLASSES, TypeAccess.DECLARED_CONSTRUCTORS,
				TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_METHODS
			}
		),
		@TypeHint(
			types = Structure.class,
			fields = {
				@FieldHint( name = "memory", allowWrite = true),
				@FieldHint( name = "typeInfo")
			},
			methods = {
				@MethodHint( name = "newInstance", parameterTypes = { Class.class, Pointer.class }),
				@MethodHint( name = "newInstance", parameterTypes = { Class.class, long.class }),
				@MethodHint( name = "newInstance", parameterTypes = { Class.class })
			},
			access = {
				TypeAccess.PUBLIC_CLASSES, TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.PUBLIC_FIELDS,
				TypeAccess.PUBLIC_METHODS, TypeAccess.DECLARED_CLASSES, TypeAccess.DECLARED_CONSTRUCTORS,
				TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_METHODS
			}
		),
		@TypeHint(
			typeNames = "com.sun.jna.Structure$FFIType",
			access = {
				TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.DECLARED_CLASSES, TypeAccess.DECLARED_CONSTRUCTORS,
				TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_METHODS
			}
		),
		@TypeHint(
			typeNames = "com.sun.jna.Structure$FFIType$size_t",
			access = {
				TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.DECLARED_CLASSES, TypeAccess.DECLARED_CONSTRUCTORS,
				TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_METHODS
			}
		),
		@TypeHint(
			typeNames = "org.jline.terminal.impl.jna.win.Kernel32$CHAR_INFO",
			access = {
				TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.DECLARED_CLASSES, TypeAccess.DECLARED_CONSTRUCTORS,
				TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_METHODS
			}
		),
		@TypeHint(
			typeNames = "org.jline.terminal.impl.jna.win.Kernel32$CONSOLE_CURSOR_INFO",
			access = {
				TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.DECLARED_CLASSES, TypeAccess.DECLARED_CONSTRUCTORS,
				TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_METHODS
			}
		),
		@TypeHint(
			typeNames = "org.jline.terminal.impl.jna.win.Kernel32$CONSOLE_SCREEN_BUFFER_INFO",
			access = {
				TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.DECLARED_CLASSES, TypeAccess.DECLARED_CONSTRUCTORS,
				TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_METHODS
			}
		),
		@TypeHint(
			typeNames = "org.jline.terminal.impl.jna.win.Kernel32$COORD",
			access = {
				TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.DECLARED_CLASSES, TypeAccess.DECLARED_CONSTRUCTORS,
				TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_METHODS
			}
		),
		@TypeHint(
			typeNames = "org.jline.terminal.impl.jna.win.Kernel32$INPUT_RECORD",
			access = {
				TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.DECLARED_CLASSES, TypeAccess.DECLARED_CONSTRUCTORS,
				TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_METHODS
			}
		),
		@TypeHint(
			typeNames = "org.jline.terminal.impl.jna.win.Kernel32$KEY_EVENT_RECORD",
			access = {
				TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.DECLARED_CLASSES, TypeAccess.DECLARED_CONSTRUCTORS,
				TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_METHODS
			}
		),
		@TypeHint(
			typeNames = "org.jline.terminal.impl.jna.win.Kernel32$MOUSE_EVENT_RECORD",
			access = {
				TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.DECLARED_CLASSES, TypeAccess.DECLARED_CONSTRUCTORS,
				TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_METHODS
			}
		),
		@TypeHint(
			typeNames = "org.jline.terminal.impl.jna.win.Kernel32$WINDOW_BUFFER_SIZE_RECORD",
			access = {
				TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.DECLARED_CLASSES, TypeAccess.DECLARED_CONSTRUCTORS,
				TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_METHODS
			}
		),
		@TypeHint(
			typeNames = "org.jline.terminal.impl.jna.win.Kernel32$MENU_EVENT_RECORD",
			access = {
				TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.DECLARED_CLASSES, TypeAccess.DECLARED_CONSTRUCTORS,
				TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_METHODS
			}
		),
		@TypeHint(
			typeNames = "org.jline.terminal.impl.jna.win.Kernel32$FOCUS_EVENT_RECORD",
			access = {
				TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.DECLARED_CLASSES, TypeAccess.DECLARED_CONSTRUCTORS,
				TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_METHODS
			}
		),
		@TypeHint(
			typeNames = "org.jline.terminal.impl.jna.win.Kernel32$SMALL_RECT",
			access = {
				TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.DECLARED_CLASSES, TypeAccess.DECLARED_CONSTRUCTORS,
				TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_METHODS
			}
		),
		@TypeHint(
			typeNames = "org.jline.terminal.impl.jna.linux.CLibrary$termios",
			access = {
				TypeAccess.DECLARED_CLASSES, TypeAccess.DECLARED_CONSTRUCTORS,
				TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_METHODS
			}
		),
		@TypeHint(
			typeNames = "org.jline.terminal.impl.jna.linux.CLibrary$winsize",
			access = {
				TypeAccess.DECLARED_CLASSES, TypeAccess.DECLARED_CONSTRUCTORS,
				TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_METHODS
			}
		)
	},
	jdkProxies = {
		@JdkProxyHint( typeNames = { "com.sun.jna.Library" }),
		@JdkProxyHint( typeNames = { "com.sun.jna.Callback" }),
		@JdkProxyHint( typeNames = { "org.jline.terminal.impl.jna.win.Kernel32" }),
		@JdkProxyHint( typeNames = { "org.jline.terminal.impl.jna.linux.CLibrary" })
	}
)
public class InitializrNativeConfiguration implements NativeConfiguration {

}
