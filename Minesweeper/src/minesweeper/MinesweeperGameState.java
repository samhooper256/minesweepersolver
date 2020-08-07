package minesweeper;

/**
 * An enum describing the possible states of a Minesweeper board.
 * 
 * @see MinesweeperBoard
 * @see ReadOnlyMinesweeperBoard
 * @author Sam Hooper
 *
 */
public enum MinesweeperGameState {
	/**
	 * Indicates that a Minesweeper board has not started; that is, no tiles have been uncovered.
	 */
	NOT_STARTED,
	/**
	 * Indicates that a Minesweeper board is currently in play; that is, one or more (but not all) tiles without
	 * mines have been uncovered, and no mine has been uncovered yet.
	 */
	ONGOING,
	/**
	 * Indicates that a Minesweeper board has ended with a win; that is, all tiles without mines have been uncovered.
	 */
	WIN,
	/**
	 * Indicates that a Minesweeper board has ended with a loss; that is, a mine has been uncovered.
	 */
	LOSS;
}
