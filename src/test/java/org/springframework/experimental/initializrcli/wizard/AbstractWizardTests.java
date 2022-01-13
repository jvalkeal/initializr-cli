package org.springframework.experimental.initializrcli.wizard;

import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.jline.keymap.KeyMap;
import org.jline.terminal.Terminal;
import org.jline.terminal.impl.DumbTerminal;
import org.jline.utils.AttributedString;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractWizardTests {

    private ExecutorService executorService;
    private PipedInputStream pipedInputStream;
    private PipedOutputStream pipedOutputStream;
    private LinkedBlockingQueue<byte[]> bytesQueue;
	private ByteArrayOutputStream consoleOut;
	private Terminal terminal;

	@BeforeEach
	public void setup() throws Exception {
		executorService = Executors.newFixedThreadPool(1);
		pipedInputStream = new PipedInputStream();
		pipedOutputStream = new PipedOutputStream();
		bytesQueue = new LinkedBlockingQueue<>();
		consoleOut = new ByteArrayOutputStream();

        pipedInputStream.connect(pipedOutputStream);
		terminal = new DumbTerminal("terminal", "ansi", pipedInputStream, consoleOut, StandardCharsets.UTF_8);

        executorService.execute(() -> {
            try {
                while (true) {
                    byte[] take = bytesQueue.take();
                    pipedOutputStream.write(take);
                    pipedOutputStream.flush();
                }
            } catch (Exception e) {
            }
        });

	}

	@AfterEach
	public void cleanup() {
		executorService.shutdown();
	}

	protected void write(byte[] bytes) {
		bytesQueue.add(bytes);
	}

	protected String consoleOut() {
        return AttributedString.fromAnsi(consoleOut.toString()).toString();
	}

	protected Terminal getTerminal() {
		return terminal;
	}

    protected class TestBuffer {
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();

        public TestBuffer() {
        }

        public TestBuffer(String str) {
            append(str);
        }

        public TestBuffer(char[] chars) {
            append(new String(chars));
        }

        @Override
        public String toString() {
            try {
                return out.toString(StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        public TestBuffer cr() {
			return append("\r");
        }

		public TestBuffer down() {
			return append("\033[B");
        }

		public TestBuffer ctrl(char let) {
            return append(KeyMap.ctrl(let));
        }

        public TestBuffer ctrlE() {
            return ctrl('R');
        }

        public TestBuffer ctrlY() {
            return ctrl('Y');
        }

		public TestBuffer space() {
			return append(" ");
        }

        public byte[] getBytes() {
            return out.toByteArray();
        }

        public TestBuffer append(final String str) {
            for (byte b : str.getBytes(StandardCharsets.UTF_8)) {
                append(b);
            }
            return this;
        }

        public TestBuffer append(final int i) {
            out.write((byte) i);
            return this;
        }
	}
}
