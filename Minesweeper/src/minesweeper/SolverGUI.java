package minesweeper;

import java.util.HashSet;
import java.util.Objects;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import minesweeper.Solver.TileProbability;

/**
 * <p>An enum containing the entire GUI used to display information about a {@link Solver}. SolverGUI's
 * {@link #INSTANCE} represents a stand-alone window and has its own {@link Stage}.</p>
 * 
 * <p>SolverGUI is an enum because it should only have one instance</p>
 * @author Sam Hooper
 *
 */
enum SolverGUI {
	INSTANCE;
	
	/* Various colors and backgrounds used in the GUI */
	
	private static final Color SAFE_COLOR = Color.rgb(104, 255, 0), MINE_COLOR = Color.rgb(255, 0, 0);
	private static final Background
			KNOWN_SAFE_BACKGROUND = new Background(new BackgroundFill(Color.AQUA, CornerRadii.EMPTY, Insets.EMPTY)),
			KNOWN_MINE_BACKGROUND = new Background(new BackgroundFill(Color.PINK, CornerRadii.EMPTY, Insets.EMPTY)),
			EXPLODED_BACKGROUND = new Background(new BackgroundFill(Color.CRIMSON, CornerRadii.EMPTY, Insets.EMPTY));
	
	/* GUI Components */
	
	private Stage stage;
	private final StackPane stack;
	private final BorderPane borderPane;
	private final GridPane grid;
	private final Pane topPane;
	private final Label topMessage;
	/**
	 * A grid of {@link SolverTile}s used to display information about the tiles in the {@link #solver}'s board.
	 */
	private SolverTile[][] tiles;
	
	/* The Solver */
	
	/**
	 * The {@link Solver} that this GUI is currently displaying information for. A new Solver is created whenever the
	 * board changes.
	 */
	private Solver solver;
	
	/**
	 * Constructs {@link #INSTANCE}.
	 */
	SolverGUI() {
		solver = null;
		stack = new StackPane();
		borderPane = new BorderPane();
		grid = new GridPane();
		topPane = new VBox();
		topMessage = new Label();
		topMessage.setFont(Font.font(16));
		borderPane.setCenter(grid);
		borderPane.setTop(topPane);
		stack.getChildren().add(0, borderPane);
		Scene scene = new Scene(stack, 300, 300);
		
		stage = new Stage();
		stage.setScene(scene);
		stage.setTitle("Solver GUI");
	}
	
	/**
	 * @return the stage associated with this SolverGUI.
	 */
	public Stage getStage() {
		return stage;
	}
	
	/**
	 * Displays {@code text} at the top of the window.
	 * @param text the new message to display at the top of the screen
	 */
	public void setTopMessage(String text) {
		topMessage.setText(text);
		if(!topPane.getChildren().contains(topMessage)) {
			topPane.getChildren().add(topMessage);
		}
	}
	
	/**
	 * Clears the message at the top of the window (if any), hiding it from view.
	 */
	public void clearTopMessage() {
		if(topPane.getChildren().contains(topMessage)) {
			topPane.getChildren().remove(topMessage);
		}
	}
	
	/**
	 * Sets the {@link Solver} that this board is displaying information about to a freshly created Solver
	 * that is associated with the board given by {@link GamePanel#getBoard()}. This method also reconfigures
	 * this GUI so that is displaying the correct information for the board.
	 */
	public void alignWithMainBoard() {
		solver = new Solver(GamePanel.INSTANCE.getBoard());
		clearTopMessage();
		alignTiles();
		updateTilesWithKnown();
	}
	
	/**
	 * Lays out {@link #grid} to conform to the current board used by {@link #solver},
	 * creates a fresh set of {@link tiles}, and adds those new tiles to the {@link #grid}.
	 */
	private void alignTiles() {
		grid.getChildren().clear();
		grid.getRowConstraints().clear();
		grid.getColumnConstraints().clear();
		final int ROWS = solver.getBoard().getRows(), COLS = solver.getBoard().getColumns();
		for(int i = 0; i < ROWS; i++) {
			RowConstraints rc = new RowConstraints();
			rc.setPercentHeight(100.0 / ROWS);
			grid.getRowConstraints().add(rc);
		}
		for(int i = 0; i < COLS; i++) {
			ColumnConstraints cc = new ColumnConstraints();
			cc.setPercentWidth(100.0 / COLS);
			grid.getColumnConstraints().add(cc);
		}
		tiles = new SolverTile[ROWS][COLS];
		for(int i = 0; i < ROWS; i++) {
			for(int j = 0; j < COLS; j++) {
				SolverTile st = tiles[i][j] = new SolverTile(i, j);
				grid.add(st, j, i); //row and col are switched on this method
			}
		}
		stack.requestLayout();
	}
	
	/**
	 * A class representing a tile in the SolverGUI.
	 * @author Sam Hooper
	 *
	 */
	private class SolverTile extends StackPane{
		/**
		 * The row of this tile
		 */
		private final int row;
		
		/**
		 * The column of this tile
		 */
		private final int col;
		
		/**
		 * The label used to display numbers on this tile
		 */
		private final Label label;
		
		/**
		 * Constructs a new SolverTile with the given row and column coordinates.
		 * @param row the row of the new tile
		 * @param col the column of the new tile
		 */
		public SolverTile(int row, int col) {
			this.row = row;
			this.col = col;
			this.setBorder(new Border(new BorderStroke(
					Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
			label = new Label();
			getChildren().add(label);
			this.setMinSize(0, 0);
		}
		
		/**
		 * Sets this tile to display as a safe (uncovered) tile.
		 */
		public void setToKnownSafe() {
			label.setText("");
			this.setBackground(KNOWN_SAFE_BACKGROUND);
		}
		
		/**
		 * Sets this tile to display as a mine (flagged) tile.
		 */
		public void setToKnownMine() {
			label.setText("");
			this.setBackground(KNOWN_MINE_BACKGROUND);
		}
		
		/**
		 * Sets this tile to display as an exploded tile.
		 */
		public void setToExploded() {
			label.setText("");
			this.setBackground(EXPLODED_BACKGROUND);
		}
		
		/**
		 * Sets this tile to display as an unknown (undecided) tile.
		 */
		public void setToUnknown() {
			label.setText("");
			this.setBackground(null);
		}
		
		/**
		 * Sets this tile to display the probability of it being a mine, as determined by {@code prob}.
		 * @param prob the probability that this tile is a mine.
		 */
		public void setToProbability(final TileProbability prob) {
			label.setText(String.format("%.3f", 1.0 - prob.getProbability()));
			this.setBackground(new Background(new BackgroundFill(
					interpolateColor(prob.getProbability()), CornerRadii.EMPTY, Insets.EMPTY)));
		}
		
		/**
		 * @return a {@link String} representation of this SolverTile containing its row and column coordinates.
		 */
		@Override
		public String toString() {
			return String.format("SolverTile[row=%d, col=%d]", row, col);
		}
	}
	
	/**
	 * Interpolates a new {@link Color} that is between {@link SolverGUI#SAFE_COLOR} and {@link SolverGUI#MINE_COLOR}.
	 * The returned color will be closer to {@code SAFE_COLOR} the closer {@code probability} is to 1.0, and closer
	 * to {@code MINE_COLOR} the closer {@code probability} is to 0.0.
	 * @param probability the value used to interpolate the returned color
	 * @return a Color that is between {@link SolverGUI#SAFE_COLOR} and {@link SolverGUI#MINE_COLOR}.
	 */
	private static Color interpolateColor(double probability) {
		int r = (int) ((SAFE_COLOR.getRed() + (MINE_COLOR.getRed() - SAFE_COLOR.getRed()) * probability) * 255);
		int g = (int) ((SAFE_COLOR.getGreen() + (MINE_COLOR.getGreen() - SAFE_COLOR.getGreen()) * probability) * 255);
		int b = (int) ((SAFE_COLOR.getBlue() + (MINE_COLOR.getBlue() - SAFE_COLOR.getBlue()) * probability) * 255);
		return Color.rgb(r, g, b);
	}
	
	/**
	 * Solves the current board, displaying the calculated probabilities in the GUI.
	 * @return a {@link HashSet} containing the {@link Solver.TileProbability}s determined by the GUI's {@link Solver}.
	 */
	public HashSet<TileProbability> solveCurrent() {
		Objects.requireNonNull(solver);
		Objects.requireNonNull(tiles);
		updateTilesWithKnown();
		if(solver.getBoard().isEnded()) {
			return null;
		}
		HashSet<TileProbability> set = null;
		try {
			set = new HashSet<>();
			solver.solveCurrentState(set::add);
			for(TileProbability tp : set) {
				int row = tp.getRow(), col = tp.getColumn();
				tiles[row][col].setToProbability(tp);
			}
			clearTopMessage();
		}
		catch(SolveException e) {
			setTopMessage(e.getMessage());
			return null;
		}
		return set;
	}
	
	/**
	 * Sets each {@link SolverTile} in this GUI to display the state of the corresponding board's tile,
	 * where the "state" of a tile is defined by {@link ReadOnlyMinesweeperBoard}.
	 */
	void updateTilesWithKnown() {
		TrustableReadOnlyMinesweeperBoard board = solver.getBoard();
		for(int i = 0; i < tiles.length; i++) {
			for(int j = 0; j < tiles[i].length; j++) {
				if(board.isUncoveredTrusted(i, j)) {
					tiles[i][j].setToKnownSafe();
				}
				else if(board.isFlaggedTrusted(i, j)) {
					tiles[i][j].setToKnownMine();
				}
				else {
					tiles[i][j].setToUnknown();
				}
			}
		}
		if(board.isEnded()) {
			if(board.isEndedWithLoss()) {
				int[] spot = board.getExplodedTile();
				tiles[spot[0]][spot[1]].setToExploded();
			}
		}
	}
	
}
