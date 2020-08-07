package minesweeper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * <p>A Solver provides an algorithm for solving a {@link TrustableReadOnlyMinesweeperBoard}. Its primary method,
 * {@link #solveCurrentState(Consumer)}, solves the current state of the board by returning the probabilities that
 * each undecided tile will or will not have a mine. The method accepts a {@link Consumer} that processes its results,
 * which are given in the form of {@link Solver.TileProbability} objects. A {@code TileProbability} contains
 * information about that probability that a certain tile will be a mine. The {@code solveCurrentState} method
 * provides probabilities only for tiles for which information can be deduced.</p>
 * 
 * @author Sam Hooper
 *
 */
public final class Solver {
	/**
	 * Below is the pseudo-code for solving algorithm. '##' denotes the start of a single-line comment.
	 * It is divided into two main parts.
	 * //////////////
	 * 	> Let an "UNCERTAIN" tile be one that the algorithm is not certain whether or not is has a mine.
	 * 	## By default, all tiles are UNCERTAIN.
	 * 	> Let a "MINE" tile be one that the algorithm is certain has a mine.
	 * 	> Let a "NON-MINE" tile be one that the algorithm is certain does not have a mine.
	 *  > If no tiles are uncovered on the board, pick one and uncover it. This tile is now a NON-MINE tile
	 *  and all other tiles are UNCERTAIN tiles.
	 *  ## The goal is to gather information about the board to deduce which tiles (if any) are MINE or NON-MINE
	 *  ## tiles using. The algorithm uses the following steps:
	 * 	## Step 1: find all regions. Do this using the following steps:
	 * 		> Let "L" be the list of all regions. It is empty by default.	
	 * 		> Let "S" be the set of all uncovered tiles that are adjacent to at least one UNCERTAIN tile. 
	 * 		## If S is empty at this point, then either the game has been won or everything is surrounded
	 * 		## by mines and thus nothing can be deduced.
	 * 		> while S is not empty:
	 * 			> remove any tile "t" from S.
	 * 			> Create a new Region "R"
	 * 			> add t to R
	 * 			> Create some Collection "C" as auxiliary storage
	 * 			> for each UNDECIDED tile "a" adjacent to t:
	 * 				> add a to C
	 * 			> while C is not empty:
	 * 				> remove any tile "a" from C.
	 * 				> for each uncovered tile "b" adjacent to a:
	 * 					> if b is not in R:
	 * 						> add b to R
	 * 						> remove b from S
	 * 						> for each undecided tile "c" adjacent to b:
	 * 							> if c has never been added to C before:
	 * 								> add c to C.
	 * 		## L now contains all the regions and is the result of step 1. It may be empty. If it is empty, nothing
	 * 		## can be deduced in step 2 and the algorithm can stop here.
	 * 	## Step 2: for each region, find the tiles which are the same in all legal permutations of the undecided tiles adjacent
	 *  ## to tiles in that region that region. Those spots are certain. Do this using the following steps:
	 * 		> for each region "R" found in the first step:
	 * 			> Create a list of all 'region-perms,' empty by default. Call it "L"
	 * 			> for each tile "t" in R:
	 * 				> if t is the first tile (i.e., L is empty):
	 * 					> add each valid permutation of the 8 tiles around this one as to L as partially-built
	 * 					region-perms
	 * 				> otherwise:
	 * 					> eliminate all region-perms in L that would not be legal given the info contained on t
	 * 					> for each region-perm "x" in L:
	 * 						> remove x from L
	 * 						> add to L all possible continuing permutations of x that conform to t and would also
	 *						be legal given the number of flags remaining to be placed.
	 *			## All region-perms contain the same tiles; each tile is set to either MINE or NON-MINE.
	 * 			> for each tile "t" that occurs in all region-perms in L:
	 * 				> if every region-perm in L has "t" as MINE tile, then t is certainly a mine.
	 * 				> otherwise, if every region-perm in L has "t" as a NON-MINE tile, then t is certainly not a mine.
	 * 				> otherwise, nothing can be deduced for certain about "t"
	 */
	
	/* This class uses two representations of tiles: one is an ordered pair of integers representing a row and a column
	 * respectively; the other is a single integer 'x' that represents the tile whose row is (x / board.getColumns())
	 * and whose column is (x % board.getColumns()). Conversions between these two representations are provided below:*/
	
	/**
	 * Converts a tile location given as an ordered pair to a tile location given as a single {@code int}.
	 * @param row the row of the tile
	 * @param col the column of the tile
	 * @return the location of the tile indicated by {@code row} and {@code col} as a single {@code int}.
	 */
	private int toInt(final int row, final int col) {
		return row * board.getColumns() + col;
	}
	
	/**
	 * Converts a tile location given as a single {@code int} to a tile location given as an ordered pair.
	 * The ordered pair is returned as an {@code int[]} of length 2.
	 * @param val the {@code int} representing the tile location
	 * @return an ordered pair, given as an {@code int[]} of length 2, representing the same tile location as
	 * {@code val}.
	 */
	private int[] toSpot(final int val) {
		return new int[] {val / board.getColumns(), val % board.getColumns()};
	}
	
	/* Several classes used by the algorithm are given below. The Region class represents the 'region' object
	 * mentioned in the pseudo-code and the RegionPerm class represents the 'region-perm' object mentioned.
	 * The third class provides the vehicle through which the algorithm produces its results. */
	
	/**
	 * <p>A class representing a <i>region</i> in a Minesweeper board. A region is a set of uncovered tiles, each of
	 * which are adjacent to at least one tile that undecided (as defined in {@link MinesweeperBoard}.
	 * The information contained within a region (that is, the numbers displayed on the tiles in the region) cannot
	 * influence the probability that any undecided tile that is not adjacent to one of the tiles in the region
	 * can have a mine.</p>
	 * 
	 * <p>The MinesweeperBoard is divided into regions (as opposed to trying to tackle the entire board at once)
	 * for efficiently reasons. Since each region, by definition, cannot influence the mine probability of any tiles
	 * adjacent to tiles in any other region, each region can be treated separately.</p>
	 * 
	 * <p>Region implements {@link Iterable}, The {@link Integer}s returned by the {@link Iterator} are the tiles
	 * in this region. Regions cannot have duplicate tiles.</p>
	 * @author Sam Hooper
	 *
	 */
	private static final class Region implements Iterable<Integer>{
		/**
		 * The tiles in this region
		 */
		private final Set<Integer> tiles;
		
		/**
		 * Creates a new Region with no tiles.
		 */
		public Region() {
			tiles = new HashSet<>();
		}
		
		/**
		 * Attempts to add {@code tile} to this Region. If this Region already contains {@code tile}, this method
		 * returns {@code false}. Otherwise, it adds {@code tile} to this Region and returns {@code true}.
		 * @param tile the tile to attempt to add to this Region, given as an {@link Integer}.
		 * @return {@code true} if and only if this Region did not contain {@code tile}, {@code false} otherwise.
		 */
		public boolean addTile(final Integer tile) {
			return tiles.add(tile);
		}
		
		/**
		 * @param tile the tile to be checked, given as an {@link Integer}.
		 * @return {@code true} if and only if this Region contains {@code tile}, {@code false} otherwise.
 		 */
		public boolean containsTile(final Integer tile) {
			return tiles.contains(tile);
		}
		
		/**
		 * @return an {@link Iterator} over the tiles in this region, given as {@link Integer}s.
		 */
		@Override
		public Iterator<Integer> iterator() {
			return tiles.iterator();
		}
	}
	
	/**
	 * <p>A class representing a legal permutation of undecided tiles. In other words, A RegionPerm represents one
	 * possibility of the tiles surrounding a particular {@link Region}. Each tile in a RegionPerm is either a mine
	 * or is not a mine (that is, it is safe). This class uses the constants {@link #MINE} and {@link #SAFE} to
	 * represent these two conditions, respectively.</p>
	 * 
	 * @author Sam Hooper
	 *
	 */
	private static final class RegionPerm{
		/**
		 * Indicates that a tile in a RegionPerm is a mine.
		 */
		public static final Boolean MINE = Boolean.TRUE;
		/**
		 * Indicates that a tile in a RegionPerm is not a mine (that is, it is safe).
		 */
		public static final Boolean SAFE = Boolean.FALSE;
		/**
		 * The tiles in this region, mapped to their state (MINE or SAFE).
		 */
		private final HashMap<Integer, Boolean> map;
		
		/**
		 * the number of tiles in this RegionPerm that are mines.
		 */
		private int minesInPerm;
		
		/**
		 * Creates a new RegionPerm with no tiles.
		 */
		public RegionPerm() {
			map = new HashMap<>();
			minesInPerm = 0;
		}
		
		/**
		 * Adds the tile indicated by {@code tile} to this RegionPerm with the specified {@code state}. If this
		 * RegionPerm already contains {@code tile}, this method overwrites the state of {@code tile} and returns
		 * the previous state of {@code tile}. Otherwise, this method adds {@code tile} with the given {@code state}
		 * and returns null.
		 * @param tile the tile to add to this RegionPerm.
		 * @param state indicator as to whether or not this tile is a mine or is safe; must be either {@link #MINE}
		 * or {@link #SAFE} or any future behavior of this object is undefined.
		 * @return the previous state of {@code tile}, or {@code null} if tile was not in this RegionPerm before this
		 * method was called.
		 */
		public Boolean putTile(final Integer tile, final Boolean state) {
			if(state.booleanValue() == MINE) {
				minesInPerm++;
			}
			return map.put(tile, state);
		}
		
		/**
		 * @return {@code true} if and only if this RegionPerm contains {@code tile} (with any state), {@code false}
		 * otherwise.
		 */
		public boolean containsTile(final Integer tile) {
			return map.containsKey(tile);
		}
		
		/**
		 * Returns the state of the given {@code tile}. This method returns return either {@link #MINE} or
		 * {@link #SAFE} if {@code tile} is in this {@code RegionPerm}, and {@code null} otherwise.
		 * @param tile the tile to query the state of
		 * @return the state of {@code tile}, or {@code null} if {@code tile} is not in this {@code RegionPerm}.
		 */
		public Boolean getTileState(final Integer tile) {
			return map.get(tile);
		}
		
		/**
		 * @return an {@link Iterator} over this tiles contained in this RegionPerm, given as {@link Integer}s.
		 */
		public Iterator<Integer> tileIterator(){
			return map.keySet().iterator();
		}
		
		/**
		 * @return the number of tiles in this RegionPerm that are mines.
		 */
		public int getMineCount() {
			return minesInPerm;
		}
		
		/**
		 * This method "duplicates" {@code this} and returns the duplicated copy. The returned RegionPerm will be
		 * a freshly created object that has the same tile-to-state mappings as this RegionPerm. The returned copy
		 * can be modified in any way without affecting {@code this} at all.
		 * @return a copy of {@code this}.
		 */
		public RegionPerm duplicate() {
			RegionPerm o = new RegionPerm();
			o.map.putAll(this.map);
			o.minesInPerm = this.minesInPerm;
			return o;
		}
		
		/**
		 * Duplicates {@code this} and adds {@code tile} to the duplicated copy with the state {@link #MINE}. This
		 * method uses {@link #duplicate()} to created the duplicated copy. This method also
		 * adds {@code tile} to {@code this} with the state {@link #SAFE}. This method returns the duplicated copy.<br>
		 * <br>The purpose of this method is to "fork" {@code this} into two RegionPerms on the given tile. After this
		 * method is called, {@code this} and the returned object will have the same tile-to-state mappings for all
		 * tiles except {@code tile}, in which case {@code this} will have {@link #SAFE} and the copy will have
		 * {@link #MINE}.
		 * @param tile the tile to add to the two RegionPerms ({@code this} and the duplicated copy).
		 * @return the duplicated copy
		 */
		public RegionPerm forkOtherMine(Integer tile) {
			RegionPerm o = duplicate();
			putTile(tile, SAFE);
			o.putTile(tile, MINE);
			return o;
		}
	}
	
	/**
	 * <p>A class representing the probability that a specific tile will be a mine. This class provides access to
	 * the row of the tile, the column of the tile, and the probability that the tile will be a mine. TileProbability
	 * objects are immutable.</p>
	 * 
	 * <p>This class implements {@link Comparable}. Objects are ordered first by their probability (see
	 * {@link TileProbability#getProbability()}). If their probabilities are equal, they are ordered
	 * by the location of their tiles in row-major order.</p>
	 * 
	 * <p>This class overrides {@link Object#equals(Object)} and {@link Object#hashCode()}.</p>
	 * 
	 * @author Sam Hooper
	 */
	public static final class TileProbability implements Comparable<TileProbability>{
		/**
		 * The row of the tile about which this object provides information
		 */
		private final int row;
		/**
		 * The column of the tile about which this object provides information
		 */
		private final int col;
		/**
		 * The probability that this tile will be a mine, from 0 to 1, where 1.0 represents a 100% chance
		 * that the tile will be a mine and 0.0 represents a 100% chance that this tile will be safe.
		 */
		private final double probability;
		/**
		 * Private constructor that initializes the instance fields. <b>This constructor assumes that its inputs
		 * are valid and performs no input verification. {@code probability} must be between 0 and 1.</b>
		 * @param row the row of the tile
		 * @param col the column of the tile
		 * @param probability the probablity that the tile will be a mine, from 0 to 1.
		 */
		private TileProbability(final int row, final int col, final double probability) {
			this.row = row;
			this.col = col;
			this.probability = probability;
		}
		
		/**
		 * @return the row of the tile about which this object provides information
		 */
		public int getRow() {
			return row;
		}
		
		/**
		 * @return the column of the tile about which this object provides information
		 */
		public int getColumn() {
			return col;
		}
		
		/**
		 * @return The probability that the tile about which this object provides information will be a mine, as a
		 * {@code double} randing from from 0 to 1, where 1.0 represents a 100% chance that the tile will be a mine
		 * and 0.0 represents a 100% chance that this tile will be safe (or, alternatively, a 0% chance that the tile
		 * will be a mine).
		 */
		public double getProbability() {
			return probability;
		}
		@Override
		public int compareTo(TileProbability o) {
			int c = Double.compare(probability, o.probability);
			if(c == 0) {
				c = Integer.compare(row, o.row);
				if(c == 0) {
					return Integer.compare(col, o.col);
				}
			}
			return c;
		}
		
		/**
		 * Returns a {@link String} representation of this TileProbability, containing the row and column of the tile
		 * this object provides information about as well as the probability that that tile will be a mine.
		 */
		@Override
		public String toString() {
			return String.format("TileProbability[row=%d, col=%d, probability=%.3f]", row, col, probability);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(col, probability, row);
		}
		
		/**
		 * Compares this TileProbability with {@code obj} for equality. Returns {@code true} if and only if
		 * {@code obj} is a TileProbability and the rows, columns, and probabilities of {@code this} and {@code obj}
		 * are equal. Returns {@code false} otherwise.
		 */
		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj == null) {
				return false;
			}
			if(getClass() != obj.getClass()) {
				return false;
			}
			TileProbability other = (TileProbability) obj;
			return col == other.col
					&& Double.doubleToLongBits(probability) == Double.doubleToLongBits(other.probability)
					&& row == other.row;
		}
	}
	
	/* Static Methods */
	/**
	 * A static utility method for removing the first element returned by the {@link Iterator} of a
	 * {@link Collection}.
	 * @param <T> a subtype of the type that the @{@link Collection} {@code c} holds and the return type for this
	 * method.
	 * @param c the {@code Collection} to return the first element of
	 * @return the first element returned by the {@link Iterator} of {@code c}.
	 */
	private static <T> T removeFirst(Collection<? extends T> c) {
		Iterator<? extends T> it = c.iterator();
		if (!it.hasNext()) { return null; }
	  	T removed = it.next();
		it.remove();
		return removed;
	}
	
	/* Instance Fields */
	
	/**
	 * The board used by this Solver
	 */
	private final TrustableReadOnlyMinesweeperBoard board;

	/**
	 * @return the board used by this Solver
	 */
	public TrustableReadOnlyMinesweeperBoard getBoard() {
		return board;
	}
	
	/* Constructor */

	/**
	 * Constructs a solver on the given board. Once constructed, the board of a Solver cannot be changed.
	 * @param board a {@link TrustableReadOnlyMinesweeperBoard} that this Solver can be used to solve.
	 * @throws NullPointerException if {@code board} is {@code null}.
	 */
	public Solver(TrustableReadOnlyMinesweeperBoard board) {
		Objects.requireNonNull(board);
		this.board = board;
	}
	
	/* Instance methods */
	
	/**
	 * Solves the board used by this {@code Solver}. The results of the solution are produced as
	 * {@link TileProbability} objects, which are passed to the provided {@link Consumer}. The solution produces a
	 * {@code TileProbability} object for each tile about which probability information can be deduced. It does not
	 * produce {@code TileProbability} objects for tiles that are flagged or uncovered, as those tiles are treated
	 * as certainly having a mine or being safe, respectively. Not all undecided tiles on the board will necessarily be
	 * represented by a TileProbability object. {@code onFound} must not be {@code null}.<br><br>
	 * <b>This method assumes that all flags currently placed on the board have mines underneath them.</b>
	 * @param onFound a {@link Consumer} providing the action to be performed on each {@link TileProbability} object
	 * produced in this method.
	 * @throws SolveException if this method cannot solve the current board because the flags placed on the board are
	 * inaccurate.
	 * @throws IllegalStateException if this {@code Solver}'s board has ended.
	 */
	public void solveCurrentState(Consumer<TileProbability> onFound) throws SolveException {
		if(board.isEnded()) {
			throw new IllegalStateException("Board has already ended!");
		}
		deduceProbabilities(onFound);
	}
	
	/**
	 * For all tiles for which probability information can be deduced, deduces that probability information and
	 * feeds it to the {@link Consumer} {@code onFound} as a {@link TileProbability} object.
	 * @param onFound a {@link Consumer} providing the action to be performed on each {@link TileProbability} object
	 * produced in this method.
	 * @throws SolveException if this method cannot solve the current board because the flags placed on the board are
	 * inaccurate.
	 */
	private void deduceProbabilities(Consumer<TileProbability> onFound) throws SolveException {
		/* Step 1 of the pseudo-code: */
		final Collection<Region> regions = getRegions();
		/* ************************** */
		
		/* Step 2 of the pseudo-code: */
		for(final Region R : regions) {
			final List<RegionPerm> L = getRegionPerms(R);
			getPermProbabilities(L, onFound);
		}
		/* ************************** */
	}
	
	/**
	 * Returns a {@link Collection} containing all of the {@link Region}s currently in this {@code Solver}'s board. It
	 * may return an empty {@code Collection}.
	 * @return the regions in the board.
	 */
	private Collection<Region> getRegions() {
		/* This method follows the psuedo-code for step 1. The variable names are the same as in the pseudo-code. */
		List<Region> L = new ArrayList<>();
		Set<Integer> S = getUncoveredEdges();
		while(!S.isEmpty()) {
			final Region R = new Region();
			final Integer t = removeFirst(S);
			R.addTile(t);
			final Set<Integer> C = new HashSet<>(); //contains only undecided tiles
			final Set<Integer> visited = new HashSet<>(); //used later to ensure that tiles are not added to C more
			//than once
			final int[] tspot = toSpot(t.intValue());
			final int trow = tspot[0], tcol = tspot[1];
			board.forEachAdjacent(trow, tcol, (nr, nc) -> {
				if(board.isInBounds(nr, nc) && board.isUndecidedTrusted(nr, nc)) {
					Integer asInteger = toInt(nr, nc);
					C.add(asInteger);
					visited.add(asInteger);
				}
			});
			while(!C.isEmpty()) {
				final Integer a = removeFirst(C);
				final int aspot[] = toSpot(a), arow = aspot[0], acol = aspot[1];
				board.forEachAdjacent(arow, acol, (nr, nc) -> {
					if(board.isUncoveredTrusted(nr, nc)) {
						final Integer b = toInt(nr, nc);
						if(!R.containsTile(b)) {
							R.addTile(b);
							S.remove(b);
							board.forEachAdjacent(nr, nc, (mr, mc) -> {
								if(board.isUndecidedTrusted(mr, mc)) {
									Integer cInteger = toInt(mr, mc);
									if(!visited.contains(cInteger)) {
										C.add(cInteger);
										visited.add(cInteger);
									}
								}
							});
						}
					}
				});
			}
			L.add(R);
		}
		return L;
	}
	
	/**
	 * Returns a {@link HashSet} of {@link Integer} objects representing all of the tiles in this board that are
	 * both uncovered and adjacent to at least one undecided tile. These are the "uncovered edges" of the board.
	 * @return the uncovered edges in the board
	 * @see #isOnEdge(int, int)
	 */
	private HashSet<Integer> getUncoveredEdges(){
		HashSet<Integer> set = new HashSet<>();
		for(int i = 0; i < board.getRows(); i++) {
			for(int j = 0; j < board.getColumns(); j++) {
				if(board.isUncoveredTrusted(i, j) && isOnEdge(i, j)) {
					set.add(toInt(i, j));
				}
			}
		}
		return set;
	}
	
	/**
	 * @param row the row of the tile to be checked
	 * @param col the column of the tile to be checked
	 * @return {@code true} if and only if the tile indicated by {@code row} and {@code col} is adjacent to one
	 * or more undecided tiles, {@code false} otherwise.
	 */
	private boolean isOnEdge(final int row, final int col) {
		for(int[] spot : ReadOnlyMinesweeperBoard.ADJACENT_TILES) {
			int nr = row + spot[0], nc = col + spot[1];
			if(board.isInBounds(nr, nc) && board.isUndecidedTrusted(nr, nc)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns an {@link List} containing all of the legal {@link RegionPerm}s for the set of all undecided tiles
	 * adjacent to any tile in the given {@link Region}.
	 * @param R the region to determine all legal {@link RegionPerm}s of
	 * @return all legal {@link RegionPerm}s for the undecided tiles surrounding {@code R}.
	 * @throws SolveException if this method cannot solve the current board because the flags placed on the board are
	 * inaccurate.
	 */
	private List<RegionPerm> getRegionPerms(final Region R) throws SolveException {
		final ArrayList<RegionPerm> L = new ArrayList<>();
		for(final Integer t : R) {
			final int[] tspot = toSpot(t.intValue());
			if(L.isEmpty()) {
				RegionPerm emptyPerm = new RegionPerm();
				L.add(emptyPerm);
				Integer[] adjacentUndecideds = getAdjacentUndecidedsAsIntegers(tspot[0], tspot[1]);
				int existingMines = countAdjacentFlagsOrMinesInPerm(emptyPerm, tspot[0], tspot[1]);
				propagateIn(L, emptyPerm, board.getDisplayedNumber(tspot[0], tspot[1]) - existingMines, adjacentUndecideds);
			}
			else {
				int dispNum = board.getDisplayedNumber(tspot[0], tspot[1]);
				clearBadPerms(L, t, dispNum);
				if(L.size() == 0){
					throw new SolveException("Cannot solve - The flags on the board are not accurate.");
				}
				Integer[] undecideds = getUndecidedAndNotInPermAsIntegers(L.get(0), tspot[0], tspot[1]);
				for(int i = L.size() - 1; i >= 0; i--) {
					RegionPerm x = L.get(i);
					int nearbyFlags = countAdjacentFlagsOrMinesInPerm(x, tspot[0], tspot[1]);
					propagateIn(L, x, dispNum - nearbyFlags, undecideds);
				}
			}
		}
		return L;
	}
	
	/**
	 * @param tileRow the row of the tile
	 * @param tileCol the column of the tile
	 * @return all of the adjacent tiles to the tile indicated by {@code tileRow} and {@code tileCol} that are
	 * undecided, given as an {@code Integer[]}.
	 */
	private Integer[] getAdjacentUndecidedsAsIntegers(final int tileRow, final int tileCol) {
		ArrayList<int[]> asSpots =
				board.getAdjacentsSatisfying(tileRow, tileCol, board::isUndecidedTrusted, ArrayList::new);
		Integer[] end = new Integer[asSpots.size()];
		int index = 0;
		for(int[] spot : asSpots) {
			end[index++] = Integer.valueOf(toInt(spot[0], spot[1]));
		}
		return end;
	}
	
	/**
	 * Returns the number of adjacent tiles to the tile indicated by {@code row} and {@code col} that are either
	 * flagged or labeled as mines in the given {@link RegionPerm}.
	 * @param perm the {@link RegionPerm}
	 * @param row the row of the tile to
	 * @param col the column of the tile
	 * @return the number of adjacent tiles to the tile indicated by {@code row} and {@code col} that are either
	 * flagged or labeled as mines in the given {@link RegionPerm}.
	 */
	private int countAdjacentFlagsOrMinesInPerm(RegionPerm perm, int row, int col) {
		return board.countAdjacentsSatisfying(row, col, (nr, nc) -> 
			board.isFlaggedTrusted(nr, nc) ||
			perm.getTileState(toInt(nr, nc)) == RegionPerm.MINE
		);
	}
	
	/**
	 * Returns the tiles that are adjacent to the tile indicated by {@code tileRow} and {@code tileCol}
	 * that are both undecided and not in the given {@link RegionPerm}.
	 * @param perm the {@link RegionPerm}
	 * @param tileRow the row of the tile
	 * @param tileCol the column of the tile
	 * @return the tiles that are adjacent to the tile indicated by {@code tileRow} and {@code tileCol}
	 * that are both undecided and not in the given {@link RegionPerm}, returned as an {@code Integer[]}.
	 */
	private Integer[] getUndecidedAndNotInPermAsIntegers(RegionPerm perm, final int tileRow, final int tileCol) {
		ArrayList<Integer> list = new ArrayList<>(ReadOnlyMinesweeperBoard.ADJACENT_TILES.length);
		board.forEachAdjacent(tileRow, tileCol, (nr, nc) -> {
			Integer obj;
			if(board.isUndecidedTrusted(nr, nc) && !perm.containsTile(obj = toInt(nr, nc))) {
				list.add(obj);
			}
		});
		return list.toArray(Integer[]::new);
	}
	
	/**
	 * <p>"Propagates" the given {@link RegionPerm} in the given {@link Collection}, placing {@code numFlags} flags
	 * in {@code tiles}, a set of tiles that are not currently in {@code perm}.</p>
	 * 
	 * <p>This method, after verifying its input, is equivalent to:</p>
	 * <blockquote><pre>
	 * propagateIn(c, perm, numFlags, 0, tiles);
	 * </pre></blockquote>
	 * @param c the {@link Collection}
	 * @param perm the {@link RegionPerm}
	 * @param numFlags the number of flags to be placed in {@code tiles}
	 * @param tiles an {@code Integer[]} of tiles that are not currently in {@code perm}
	 * @throws NullPointerException if one or more of {@code c}, {@code perm}, or {@code tiles} are null.
	 * @see #propagateIn(Collection, RegionPerm, int, int, Integer...)
	 */
	private void propagateIn(Collection<? super RegionPerm> c, RegionPerm perm, int numFlags, Integer... tiles) {
		Objects.requireNonNull(c);
		Objects.requireNonNull(perm);
		Objects.requireNonNull(tiles);
		if(numFlags > tiles.length) {
			throw new IllegalArgumentException("numFlags cannot be greater than tiles.length");
		}
		propagateIn(c, perm, numFlags, 0, tiles);
	}
	
	/**
	 * <p>Helper method for {@link #propagateIn(Collection, RegionPerm, int, Integer...)}.</p>
	 * 
	 * <p>This method finds all of the possible legal ways that {@code flagsRemaining} flags could be placed in the tiles
	 * given by the subarray of {@code tiles} from {@code tileIndex} (inclusive) to {@code tiles.length} (exclusive).
	 * It then 'splits' {@code perm} into a set of {@link RegionPerm}s containing, for each permuation <i>p</i> of flag placements:
	 * all the tiles originally in {@code perm} as well as all of the tiles in <i>p</i> mapped to their appropriate state.
	 * All elements of this new set of {code RegionPerm}s (except for {@code perm})  are then added to {@code c}.
	 * One of these resulting {@code RegionPerm}s will be {@code perm}. It will not added to {@code c} again.
	 * This method may also remove {@code perm} from {@code c} if it is found to be an invalid permutation. If
	 * {@code perm} is invalid, no {@code RegionPerm}s will be added to {@code c}.
	 * This means that <i>{@code perm} can be modified during this method and even removed from {@code c},</i></p>
	 * 
	 * <p>If {@code tileIndex >= tiles.length}, this method returns silently has no effect.</p>
	 * 
	 * <p>Precondition: {@code perm} is an element of {@code c}.<br>
	 * Precondition: {@code perm} does not contain any tile in the aforementioned subarray of {@code tiles}.</p>
	 * @param c the {@link Collection} of {@link RegionPerms} to add new permutations to
	 * @param perm the {@code RegionPerm} used to generate new permutations. It must be an element of {@code c}.
	 * @param flagsRemaining the number of flags to be placed in the newly created permutations.
	 * @param tileIndex the first index (inclusive) of the subarray of {@code tile}s to be considered.
	 * @param tiles an {@code Integer[]} of tiles to place flags into. Only the subarray of {@code tiles} from
	 * {@code tilesIndex} (inclusive) to {@code tiles.length} (exclusive) will be considered.
	 * @see #propagateIn(Collection, RegionPerm, int, Integer...)
	 */
	private void propagateIn(Collection<? super RegionPerm> c, RegionPerm perm, int flagsRemaining, int tileIndex, Integer... tiles) {
		if(tileIndex >= tiles.length) {
			return;
		}
		else if(flagsRemaining == 0) {
			for(int i = tileIndex; i < tiles.length; i++) {
				perm.putTile(tiles[i], RegionPerm.SAFE);
			}
			return;
		}
		else if(flagsRemaining == tiles.length - tileIndex) {
			if(perm.getMineCount() + flagsRemaining <= board.getFlagsRemaining()) {
				for(int i = tileIndex; i < tiles.length; i++) {
					perm.putTile(tiles[i], RegionPerm.MINE);
				}
			}
			else {
				boolean wasRemoved = c.remove(perm);
				assert wasRemoved : "perm was not in c\nperm="+perm+"\nc="+c;
			}
			return;
		}
		RegionPerm fork = perm.forkOtherMine(tiles[tileIndex]);
		if(fork.getMineCount() + (flagsRemaining - 1) <= board.getFlagsRemaining()) {
			c.add(fork);
			propagateIn(c, fork, flagsRemaining - 1, tileIndex + 1, tiles);
		}
		propagateIn(c, perm, flagsRemaining, tileIndex + 1, tiles);
	}
	
	/**
	 * Produces a {@link TileProbability} object for each tile that is in all {@link RegionPerm}s in {@code L} and
	 * performs the given action on that object.
	 * @param L the {@link List} of {@link RegionPerm}s to be considered
	 * @param onFound a {@link Consumer} providing the action to be performed on each {@link TileProbabilitiy}.
	 * @throws SolveException if this method cannot solve the current board because the flags placed on the board are
	 * inaccurate.
	 */
	private void getPermProbabilities(final List<RegionPerm> L, final Consumer<TileProbability> onFound) throws SolveException{
		if(L.size() == 0){
			throw new SolveException("Cannot solve - The flags on the board are not accurate.");
		}
		RegionPerm basePerm = L.get(0);
		for(Iterator<Integer> tileItr = basePerm.tileIterator(); tileItr.hasNext();) {
			Integer t = tileItr.next();
			int mines = 0;
			int safes = 0;
			for(RegionPerm perm : L) {
				if(perm.getTileState(t) == RegionPerm.MINE) { //tile is a mine
					mines++;
				}
				else { //tile is safe
					safes++;
				}
			}
			int tspot[] = toSpot(t.intValue()), trow = tspot[0], tcol = tspot[1];
			double mineProbability = ((double) mines) / (mines + safes);
			if(onFound != null) {
				onFound.accept(new TileProbability(trow, tcol, mineProbability));
			}
		}
	}

	/**
	 * Returns the number of tiles in adjacent to the tile indicated by {@code row} and {@code col} that are both
	 * undecided and not in the provided {@link RegionPerm}.
	 * @param perm the {@link RegionPerm} to be considered
	 * @param row the row of the tile
	 * @param col the column of the tile
	 * @return the number of tiles in adjacent to the tile indicated by {@code row} and {@code col} that are both
	 * undecided and not in {@code perm}
	 */
	private int countUndecidedAndNotInPerm(final RegionPerm perm, final int row, final int col) {
		return board.countAdjacentsSatisfying(row, col, (nr, nc) -> 
			board.isUndecidedTrusted(nr, nc) && !perm.containsTile(toInt(nr, nc))
		);
	}

	/**
	 * Removes all {@link RegionPerm}s in the given {@link ArrayList} that are not legal, given the exact number of
	 * mines adjacent to {@code tile}.
	 * @param list the {@link ArrayList} of {@link RegionPerm}s to filter illegal permutations out of.
	 * @param tile a tile that is known to be adjacent to exactly {@code mines} mines, given as an {@link Integer}.
	 * @param mines the number of mines adjacent to {@code tile}.
	 */
	private void clearBadPerms(final ArrayList<RegionPerm> list, final Integer tile, final int mines) {
		final int tileSpot[] = toSpot(tile);
		for(int i = list.size() - 1; i >= 0; i--) {
			RegionPerm perm = list.get(i);
			int knownMines = countAdjacentFlagsOrMinesInPerm(perm, tileSpot[0], tileSpot[1]);
			if(knownMines > mines) {
				list.remove(i);
			}
			else {
				int undecided = countUndecidedAndNotInPerm(perm, tileSpot[0], tileSpot[1]);
				if(mines - knownMines > undecided) {
					list.remove(i);
				}
			}
		}
	}
}
