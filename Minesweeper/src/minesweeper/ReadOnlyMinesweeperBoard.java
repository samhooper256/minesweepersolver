package minesweeper;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * <p>An interface describing the contract for a read-only board for the game Minesweeper. A Minesweeper board must
 * be rectangular; that is, it must have a fixed number of rows and columns.</p>
 * 
 * <p>This interface uses the notion of a <i>row</i> and <i>column</i> on a Minesweeper board. Rows start from the
 * top of the board at 0 and increase downward, and columns start at the left of the board at 0 and increase going
 * right. For example, the tile at (0, 0) is the tile in the top left corner and the tile at
 * (({@link #getRows()} - 1, {@link #getColumns()} - 1) is the tile in bottom right corner.</p>
 * 
 * <p>Each tile on a Minesweeper board has up to eight adjacent tiles, which are the tiles that are both one or fewer
 * columns away and one or fewer rows away. A tile may have fewer than eight adjacent tiles if it is on the edge. 
 * {@link #ADJACENT_TILES} is a convenience {@code int[][]} describing the locations of adjacent tiles.</p>
 * 
 * <p>The <i>visible tiles</i> of a Minesweeper board are the tiles that the user of a ReadOnlyMinesweeperBoard can
 * view. Visible tiles are separate from any other notion of "tile" defined by a specific implementation. Visible
 * tiles can be in one of three states:
 * <ul>
 * <li> <b>Uncovered</b>: the tile has been uncovered and either had a mine on it or did not. If it did not have
 * a mine its display number (as given by {@link #getDisplayedNumber(int, int)}) is the number of mines that are
 * adjacent to that tile. If the tile did have a mine on it, it is additionally considered to be <i>exploded</i>.
 * <li> <b>Flagged</b>: the tile currently has a flag on it.
 * <li> <b>Undecided</b>: the tile is neither uncovered nor flagged.
 * </ul>
 * The {@link #isUncovered(int, int)}, {@link #isFlagged(int, int)}, {@link #isUndecided(int, int)}, and
 * {@link #isExploded(int, int)} methods correspond the above classifications.
 * </p>
 * 
 * <p> A Minesweeper board has ended (see {@link #isEnded()}) when a tile with a mine on it has been uncovered
 * (that is, there is an exploded tile). In this case, the board has ended with a loss
 * (See {@link #isEndedWithLoss()}). A board also ends when all tiles that do not have mines have been uncovered.
 * In this case, the board has ended with a win (See {@link #isEndedWithWin()}).</p>
 * 
 * <p>A Minesweeper board also has a nonnegative number of flags that can be placed at any given time.
 * This value can be accessed via {@link #getFlagsRemaining()}.</p>
 * @author Sam Hooper
 *
 */
public interface ReadOnlyMinesweeperBoard {
	
	/**
	 * A functional interface for a specialized {@link java.util.function.BiConsumer} that accepts two {@code int}
	 * parameters.
	 * @author Sam Hooper
	 *
	 */
	@FunctionalInterface
	public interface TileConsumer{
		void apply(int row, int col);
	}
	
	/**
	 * A functional interface for a specialized {@link java.util.function.BiPredicate} that accepts two {@code int}
	 * parameters.
	 * @author Sam Hooper
	 *
	 */
	@FunctionalInterface
	interface TilePredicate{
		boolean test(int row, int col);
	}
	
	/**
	 * Convenience {@code int[][]} for describing the locations of the 8 tiles that are "potentially adjacent" to
	 * each tile. A potentially adjacent tile is adjacent unless it is out of bounds.
	 * The 8 potentially adjacent tiles to the tile at {@code (row, col)} are the tiles at
	 * {@code (row + ADJACENT_TILES[i][0], col + ADJACENT_TILES[i][1])} for 
	 * <i>0 <= i < ADJACENT_TILES.length</i>
	 */
	int[][] ADJACENT_TILES = new int[][] {
			{0,1}, {0,-1}, {1,1}, {1,0}, {1,-1}, {-1,1}, {-1,0}, {-1,-1}
	};
	
	/**
	 * Performs the given action on each adjacent tile to the tile indicated by {@code row} and {@code col}.
	 * The arguments passed to the functional method of {@code consumer} are the row and column of each
	 * adjacent tile, in that order.
	 * @param row the row of the tile
	 * @param col the column of the tile
	 * @param consumer a {@link TileConsumer} representing the action to be performed on each adjacent tile to the
	 * tile indicated by {@code row} and {@code col}.
	 */
	default void forEachAdjacent(final int row, final int col, final TileConsumer consumer) {
		for(int[] spot : ADJACENT_TILES) {
			final int nr = row + spot[0];
			final int nc = col + spot[1];
			if(isInBounds(nr, nc))
				consumer.apply(nr, nc);
		}
	}
	
	/**
	 * Counts the number of adjacent tiles to the tile indicated by {@code row} and {@code col} that satisfy the given
	 * {@link TilePredicate}. The arguments passed to the functional method of
	 * {@code predicate} are the row and column of each adjacent tile, in that order.
	 * @param row the row of the tile
	 * @param col the column of the tile
	 * @param predicate the {@link TilePredicate} used to test each adjacent tile
	 * @return the number of tiles that are adjacent to the tile indicated by {@code row} and {@code col} that
	 * satisfy {@code predicate}.
	 */
	default int countAdjacentsSatisfying(final int row, final int col, final TilePredicate predicate) {
		int count = 0;
		for(int[] spot : ADJACENT_TILES) {
			final int nr = row + spot[0];
			final int nc = col + spot[1];
			if(isInBounds(nr, nc) && predicate.test(nr, nc))
				count++;
		}
		return count;
	}
	
	/**
	 * Returns a {@link Collection} containing the tiles that are adjacent to the tile indicated by {@code row}
	 * and {@code col} that satisfy the given {@link TilePredicate}. The tiles are returned as {@code int[]}
	 * objects of length 2 containing the row and column of the tile, in that order. The arguments passed to the
	 * functional method of {@code predicate} are the row and the column of each adjacent tile, in that order.
	 * @param <C> the type of the collection to be returned
	 * @param row the row of the tile
	 * @param col the column of the tile
	 * @param predicate the {@link TilePredicate} used to test each adjacent tile
	 * @param collectionSupplier a {@link Supplier} for the collection to return the results in.
	 * @return a {@link Collection} containing the tiles that are adjacent to the tile indicated by {@code row} and
	 * {@code col} that satisfy predicate as {@code int[]} objects.
	 */
	default <C extends Collection<? super int[]>>
	C getAdjacentsSatisfying(final int row, final int col, TilePredicate predicate, Supplier<C> collectionSupplier) {
		C collection = collectionSupplier.get();
		for(int[] spot : ADJACENT_TILES) {
			final int nr = row + spot[0];
			final int nc = col + spot[1];
			if(isInBounds(nr, nc) && predicate.test(nr, nc))
				collection.add(new int[] {nr, nc});
			
		}
		return collection;
	}
	
	/**
	 * Returns {@code true} if and only if this board has ended (with a win or loss), {@code false} otherwise.
	 * @return {@code true} iff this board has ended, {@code false} otherwise.
	 */
	boolean isEnded();

	/**
	 * Returns {@code true} if and only if this board has ended and the result of the game was a win (that is,
	 * all non-mine tiles have been uncovered and no mine tiles have been uncovered). Returns {@code false}
	 * otherwise.
	 * @return {@code true} iff this board has ended with a win, {@code false} otherwise.
	 */
	boolean isEndedWithWin();

	/**
	 * Returns {@code true} if and only if this board has ended and the result of the game was a loss (that is,
	 * a mine tile has been uncovered). Returns {@code false} otherwise.
	 * @return {@code true} iff this board has ended with a loss, {@code false} otherwise.
	 */
	boolean isEndedWithLoss();

	/**
	 * @return the number of rows in this board
	 */
	int getRows();

	/**
	 * @return the number of columns in this board
	 */
	int getColumns();

	/**
	 * @return the number of flags that can currently be placed.
	 */
	int getFlagsRemaining();

	/**
	 * Returns {@code true} if and only if the tile indicated by {@code row} and {@code col} has been uncovered,
	 * {@code false} otherwise<br>
	 * Note that a tile that was uncovered and had a mine underneath (that is,
	 * a tile for which {@link #isExploded(int, int)} returns true) is still considered uncovered.
	 * 
	 * @param row the row of the tile
	 * @param col the column of the tile
	 * @return {@code true} iff the tile indicated by {@code row} and {@code col} has been uncovered, {@code false}
	 * otherwise.
	 * @throws IllegalArgumentException if the tile indicated by {@code row} and {@code col} is out of bounds for this
	 * board.
	 */
	boolean isUncovered(int row, int col);

	/**
	 * Returns {@code true} if any only if the tile indicated by {@code row} and {@code col} currently has a flag
	 * on it.
	 * @param row the row of the tile
	 * @param col the column of the tile
	 * @return {@code true} iff the tile indicated by {@code row} and {@code col} currently has a flag on it,
	 * {@code false} otherwise
	 */
	boolean isFlagged(int row, int col);

	/**
	 * Returns {@code true} if and only if the tile indicated by {@code row} and {@code col} is undecided,
	 * {@code false} otherwise.
	 * @param row the row of the tile
	 * @param col the column of the tile
	 * @return {@code true} iff the tile indicated by {@code row} and {@code col} is undecided, {@code false} otherwise.
	 * @throws IllegalArgumentException if the tile indicated by {@code row} and {@code col} is out of bounds for
	 * this board.
	 */
	boolean isUndecided(int row, int col);

	/**
	 * Returns {@code true} if and only if the tile indicated by {@code row} and {@code col} has been uncovered and
	 * had a mine on it, {@code false} otherwise. Note that once a tile has been exploded, the game ends.
	 * @param row the row of the tile
	 * @param col the column of the tile
	 * @return {@code true} iff the tile indicated by {@code row} and {@code col} has been uncovered and
	 * had a mine on it, {@code false} otherwise.
	 * @throws IllegalArgumentException if the tile indicated by {@code row} and {@code col} is out of bounds for this
	 * board.
	 */
	boolean isExploded(int row, int col);

	/**
	 * Returns the number to be displayed on the tile indicated by {@code row} and {@code col}. The number to
	 * be displayed on a tile is the number of tiles adjacent to it that are mines. This method returns zero
	 * if there are no adjacent mines to a tile, and it returns -1 if the tile should not be displaying a number;
	 * that is, if it has not been uncovered or it is exploded.
	 * @param row the row of the tile
	 * @param col the column of the tile
	 * @return the number to display on this tile. Returns -1 if this tile should not be displaying a number.
	 * @throws IllegalArgumentException if the tile indicated by {@code row} and {@code col} is out of bounds
	 * for this board.
	 */
	int getDisplayedNumber(int row, int col);

	/**
	 * Returns true if and only if the tile indicated by {@code row} and {@code col} is in bounds. The tile is in
	 * bounds if and only if {@code (row >= 0 && row < getRows() && col >= 0 && col < getColumns())}.
	 * @param row the row of the tile
	 * @param col the column of the tile
	 * @return true iff the tile indicated by {@code row} and {@code col} is in bounds, false otherwise.
	 */
	default boolean isInBounds(final int row, final int col) {
		return row >= 0 && row < getRows() && col >= 0 && col < getColumns();
	}
	
	/**
	 * Returns an {@code int[]} of length 2 where index 0 holds the row and index 1 holds the column of the tile
	 * that was a mine and caused the game to end.
	 * @return an {@code int[]} containing the location of the tile of the mine that exploded.
	 * @throws IllegalStateException if {@link #isEndedWithLoss()} returns {@code false}.
	 */
	int[] getExplodedTile();
}