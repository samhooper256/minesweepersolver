package minesweeper;

/**
 * Checked exception thrown to indicate that a {@link Solver} was unable to solve its board normally.
 * 
 * @see Solver
 * @author Sam Hooper
 *
 */
public final class SolveException extends Exception {
	
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 6778617506858450126L;
	
	public SolveException(String message) {
		super(message);
	}
}
