package minesweeper;

import java.util.Collections;
import java.util.HashSet;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import minesweeper.Solver.TileProbability;

/**
 * <p>A {@link Pane} representing a {@link MinesweeperBoard}. This class extends {@link GridPane}, so an instance can
 * be added directly to the scenegraph.</p>
 * 
 * @author Sam Hooper
 *
 */
class SweeperGrid extends GridPane {
	/* Various Images and Backgrounds used in the SweeperGrid */
	private static final Image
			FLAG_IMAGE = new Image(SweeperGrid.class.getResourceAsStream(Minesweeper.RES_PREFIX + "flag.png")),
			SKULL_IMAGE = new Image(SweeperGrid.class.getResourceAsStream(Minesweeper.RES_PREFIX + "skullandcrossbones.png"));
	private static final Background
			LIGHT = new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)),
			DARK = new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)),
			LIGHT_UNCOVERED = new Background(new BackgroundFill(Color.rgb(252, 231, 177, 1.0), CornerRadii.EMPTY, Insets.EMPTY)),
			DARK_UNCOVERED = new Background(new BackgroundFill(Color.rgb(255, 218, 133, 1.0), CornerRadii.EMPTY, Insets.EMPTY));
	
	/**
	 * Debug variable indicating whether tiles should display their coordinates
	 */
	private static final boolean SHOW_COORDS = false;
	/**
	 * The {@link MinesweeperBoard} associated with this SweeperGrid
	 */
	private MinesweeperBoard minesweeperBoard;
	
	/**
	 * A grid of {@link SweeperTile}s representing the tiles in {@link #minesweeperBoard}.
	 */
	private SweeperTile[][] tiles;
	
	/**
	 * Constructs a new SweeperGrid. This is the solve constructor.
	 */
	public SweeperGrid() {
		super();
		minesweeperBoard = null;
		tiles = null;
	}
	
	/**
	 * @return the number of flags remaining in the board represented by this SweeperGrid, as determined by
	 * {@link MinesweeperBoard#getFlagsRemaining()}.
	 */
	public int getFlagsRemaining() {
		return minesweeperBoard.getFlagsRemaining();
	}
	
	/**
	 * Lays out this grid pane so that its tile positions and counts conform to the current board.
	 * After this method has been called, all tiles will appear to be undecided. This method invokes
	 * {@link #resetTiles()}.
	 */
	private void resetLayoutAndTiles(){
		getRowConstraints().clear();
		getColumnConstraints().clear();
		getChildren().clear();
		for(int i = 0; i < minesweeperBoard.getRows(); i++) {
			RowConstraints rc = new RowConstraints();
			rc.setPercentHeight(100.0 / minesweeperBoard.getRows());
			getRowConstraints().add(rc);
		}
		for(int i = 0; i < minesweeperBoard.getColumns(); i++) {
			ColumnConstraints cc = new ColumnConstraints();
			cc.setPercentWidth(100.0 / minesweeperBoard.getColumns());
			getColumnConstraints().add(cc);
		}
		resetTiles();
	}

	/**
	 * Assigns {@link tiles} to a fresh grid of {@link SweeperTile}s whose dimensions correspond to the board
	 * represented by this SweeperGrid.
	 */
	private void resetTiles() {
		tiles = new SweeperTile[minesweeperBoard.getRows()][minesweeperBoard.getColumns()];
		for(int i = 0; i < tiles.length; i++) {
			for(int j = 0; j < tiles[i].length; j++) {
				tiles[i][j] = new SweeperTile(i, j);
				add(tiles[i][j], j, i); //this method swaps row & col
			}
		}
	}
	
	/**
	 * Changes the board represented by this SweeperGrid to {@code board}.
	 * @param board the new board that this SweeperGrid will represent.
	 */
	public void changeBoard(MinesweeperBoard board) {
		this.minesweeperBoard = board;
		GamePanel.INSTANCE.setFlagCount(minesweeperBoard.getFlagsRemaining());
		resetLayoutAndTiles();
	}
	
	/**
	 * @return the {@link MinesweeperBoard} that this SweeperGrid represents.
	 */
	public MinesweeperBoard getBoard() {
		return minesweeperBoard;
	}
	
	/**
	 * Updates the flag count and displays of all the tiles in {@link #tiles} to be accurate
	 * with the {@link #minesweeperBoard}. This method invokes {@link #updateTiles()} and {@link #updateFlagCount()}.
	 */
	private void updateAll() {
		updateTiles();
		updateFlagCount();
	}
	
	/**
	 * Updates the displays of all of the {@link SweeperTile}s in {@link #tiles} to be accurate with the board
	 * represented by this SweeperGrid.
	 */
	private void updateTiles() {
		for(int i = 0; i < tiles.length; i++) {
			for(int j = 0; j < tiles[i].length; j++) {
				if(minesweeperBoard.isExploded(i, j)) {
					tiles[i][j].setExploded();
				}
				else if(minesweeperBoard.isFlagged(i, j)) {
					tiles[i][j].setFlagged();
				}
				else if(minesweeperBoard.isUncovered(i, j)) {
					tiles[i][j].setToUncoveredColor();
					int displayNumber = minesweeperBoard.getDisplayedNumber(i, j);
					if(displayNumber != 0)
						tiles[i][j].setNumber(displayNumber);
				}
				else {
					tiles[i][j].setToUndecided();
				}
			}
		}
		if(minesweeperBoard.isEndedWithWin()) {
			for(int i = 0; i < tiles.length; i++) {
				for(int j = 0; j < tiles[i].length; j++) {
					if(!minesweeperBoard.isUncovered(i, j)) {
						getChildren().remove(tiles[i][j]);
					}
				}
			}
		}
		requestLayout();
	}
	
	/**
	 * Invokes {@link GamePanel#setFlagCount(int)} to set the flag count of the {@link GamePanel} to the accurate value
	 * of this SweeperGrid's board, as given by {@link MinesweeperBoard#getFlagsRemaining()}.
	 */
	private void updateFlagCount() {
		GamePanel.INSTANCE.setFlagCount(minesweeperBoard.getFlagsRemaining());
	}
	
	/**
	 * Receives and performs the appropriate action for the mouse input of the given {@link MouseEvent}.
	 * @param event the {@link MouseEvent} to be handled by this method.
	 */
	private void acceptMouseInput(MouseEvent event) {
		Object objSource = event.getSource();
		if(!(objSource instanceof SweeperTile)) {
			return;
		}
		if(canInteract()) {
			SweeperTile source = (SweeperTile) objSource;
			int row = source.row, col = source.col;
			MouseButton button = event.getButton();
			acceptClick(button, row, col);
			SolverGUI.INSTANCE.solveCurrent();
		}
	}
	
	/**
	 * Accepts a click indicated by {@code button} on the tile indicated by {@code row} and {@code col}. If
	 * {@code button} represents a primary mouse button, the click unflags the indicated tile if it has a
	 * flag or uncovers the indicated tile if it is undecided. If {@code button} is a secondary mouse button,
	 * the click places a flag on the indicated tile if it does not currently have a flag (and does nothing otherwise).
	 * @param button the button of the mouse click
	 * @param row the row of the tile that was clicked
	 * @param col the column of the tile that was clicked
	 */
	private void acceptClick(final MouseButton button, final int row, final int col) {
		if(button == MouseButton.PRIMARY) {
			acceptLeft(row, col);
		}
		else if(button == MouseButton.SECONDARY){
			acceptRight(row, col);
		}
	}
	
	/**
	 * Handles a primary mouse click on the given tile, as defined by {@link #acceptClick(MouseButton, int, int)}.
	 * @param row the row of the tile that was clicked
	 * @param col the column of the tile that was clicked
	 */
	private void acceptLeft(int row, int col) {
		if(!minesweeperBoard.isUncovered(row, col)) {
			if(minesweeperBoard.isFlagged(row, col)) {
				minesweeperBoard.unflag(row, col);
			}
			else {
				minesweeperBoard.uncover(row, col);
			}
			updateAll();
		}
	}
	
	/**
	 * Handles a secondary mouse click on the given tile, as defined by {@link #acceptClick(MouseButton, int, int)}.
	 * @param row the row of the tile that was clicked
	 * @param col the column of the tile that was clicked
	 */
	private void acceptRight(int row, int col) {
		boolean success = minesweeperBoard.toggleFlag(row, col);
		if(success) {
			updateAll();
		}
	}
	
	/**
	 * Determines whether or not this SweeperGrid can be interacted with. A SweeperGrid can be interacted with
	 * if and only if the board represented by the SweeperGrid has not ended.
	 * @return {@code true} if and only if the underlying board has not ended, {@code false} otherwise.
	 */
	private boolean canInteract() {
		return !minesweeperBoard.isEnded();
	}
	
	/**
	 * <p>A class representing a tile in a SweeperGrid. SweeperTiles can indicate that they are flagged, uncovered,
	 * undecided, or exploded (as defined by {@link MinesweeperBoard})</p>
	 * @author Sam Hooper
	 *
	 */
	private class SweeperTile extends StackPane {
		/**
		 * The row of this tile
		 */
		private final int row;
		
		/**
		 * The column of this tile
		 */
		private final int col;
		
		/**
		 * The {@link Label} used to display a number on this tile, if necessary.
		 */
		private Label label;
		
		/**
		 * Indicates whether or not this tile has a flag on it
		 */
		private boolean isFlagged;
		
		/**
		 * The {@link ImageView} used to display the flag and exploded icons.
		 */
		private ImageView imv;
		
		/**
		 * Constructs a SweeperTile with the given row and column
		 * @param row the row of the newly created tile
		 * @param col the column of the newly created tile
		 */
		public SweeperTile(int row, int col) {
			this.row = row; this.col = col; this.isFlagged = false;
			if(SHOW_COORDS) {
				final Label l = new Label(row +","+col);
				l.setFont(Font.font(10));
				l.setTextFill(Color.BLUE);
				this.getChildren().add(new Pane(l));
			}
			label = new Label();
			this.getChildren().add(label);
			this.prefWidthProperty().bind(SweeperGrid.this.widthProperty().divide(minesweeperBoard.getColumns()));
			imv = new WrappedImageView(null);
			imv.setSmooth(true);
			imv.setPreserveRatio(true);
			this.getChildren().add(new StackPane(imv));
			if((row + col & 1) == 0)
				setBackground(LIGHT);
			else
				setBackground(DARK);
			this.setOnMousePressed(SweeperGrid.this::acceptMouseInput);
		}
		
		/**
		 * Sets this tile to display {@code number}.
		 * @param number the number to display on this tile.
		 */
		public void setNumber(int number) {
			this.label.setText(Integer.toString(number));
		}
		
		/**
		 * Sets this tile to display as an undecided tile. If this tile is uncovered, this method has no effect.
		 */
		public void setToUndecided() {
			setUnflagged();
			this.label.setText("");
		}
		
		/**
		 * Displays a flag on this tile, if it does not already have one.
		 */
		public void setFlagged() {
			if(!isFlagged) {
				imv.setImage(FLAG_IMAGE);
				isFlagged = true;
			}
		}
		
		/**
		 * Removes the flag from this tile, if it has one.
		 */
		public void setUnflagged() {
			if(isFlagged) {
				imv.setImage(null);
				isFlagged = false;
			}
		}
		
		/**
		 * Displays {@link SweeperGrid#SKULL_IMAGE} on this tile to indicate that it has exploded.
		 */
		public void setExploded() {
			imv.setImage(SKULL_IMAGE);
			isFlagged = false;
		}
		
		/**
		 * Sets this tile to a color that indicates that it has been uncovered.
		 */
		public void setToUncoveredColor() {
			setUnflagged();
			if((row + col & 1) == 0)
				setBackground(LIGHT_UNCOVERED);
			else
				setBackground(DARK_UNCOVERED);
		}
	}
}
