package minesweeper;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>An abstract class describing a <i>trustable</i> read-only Minesweeper board. This class provides
 * 3 package-private methods, all with default implementations. The default implementations fall back to
 * the corresponding methods in {@link ReadOnlyMinesweeperBoard}, which this class implements. The
 * methods in this class provided "trusted" access to certain information that is already accessible via
 * methods in {@code ReadOnlyMinesweeperBoard}. What defines "trusted" and hence what is meant by trustable is up
 * to the subclass.</p>
 * 
 * <p><b>A TrustableReadOnlyMinesweeperBoard is still read-only. It must not expose any information that would not
 * be accessible from a {@link ReadOnlyMinesweeperBoard}.</b></p>
 * @author Sam Hooper
 *
 */
public abstract class TrustableReadOnlyMinesweeperBoard implements ReadOnlyMinesweeperBoard {
	/**
	 * Can be overridden to allow "trusted" access. What defines "trusted" is specified by the subclass. As such,
	 * no contract is defined here. The default implementation is equivalent to calling the non-trusted form of
	 * this method, {@link #isUncovered(int, int)}.
	 * @param row the row of the tile
	 * @param col the column of the tile
	 */
	boolean isUncoveredTrusted(final int row, final int col) {
		return isUncovered(row, col);
	}
	
	/**
	 * Can be overridden to allow "trusted" access. What defines "trusted" is specified by the subclass. As such,
	 * no contract is defined here. The default implementation is equivalent to calling the non-trusted form of
	 * this method, {@link #isFlagged(int, int)}.
	 * @param row the row of the tile
	 * @param col the column of the tile
	 */
	boolean isFlaggedTrusted(final int row, final int col) {
		return isFlagged(row, col);
	}
	
	/**
	 * Can be overridden to allow "trusted" access. What defines "trusted" is specified by the subclass. As such,
	 * no contract is defined here. The default implementation is equivalent to calling the non-trusted form of
	 * this method, {@link #isUndecided(int, int)}.
	 * @param row the row of the tile
	 * @param col the column of the tile
	 */
	boolean isUndecidedTrusted(final int row, final int col) {
		return isUndecided(row, col);
	}
}
