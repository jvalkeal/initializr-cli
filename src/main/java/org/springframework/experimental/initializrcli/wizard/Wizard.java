package org.springframework.experimental.initializrcli.wizard;

/**
 * Interface for a generic {@code Wizard}.
 *
 * @author Janne Valkealahti
 */
public interface Wizard<T extends WizardResult> {

	/**
	 * Run wizard and return results from it.
	 *
	 * @return wizard result
	 */
	T run();
}
