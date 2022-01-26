package org.springframework.experimental.initializrcli.config;

// import com.sun.jna.Native;

import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
	options = { "-Djna.debug_load=true", "-Djna.debug_load.jna=true" },
	initialization = @InitializationHint(
		// types = { Native.class },
		// typeNames = { "com.sun.jna.Native" },
		initTime = InitializationTime.BUILD))
public class InitializrNativeConfiguration implements NativeConfiguration {
}
