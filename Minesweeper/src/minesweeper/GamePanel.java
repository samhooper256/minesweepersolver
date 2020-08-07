package minesweeper;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * <p>An enum containing the main {@link Pane} used in this {@link javafx.application.Application}.</p>
 * 
 * <p> The {@link Pane} returned by {@link #getPane} is the content of the {@link javafx.scene.Scene} used for the
 * {@link javafx.stage.Stage} stage in this {@link javafx.application.Application} (see {@link Main}). The sole
 * instance of the GamePanel enum manages the {@link SweeperGrid} for the application, which in turn manages the
 * {@link MinesweeperBoard}.</p>
 * 
 * @author Sam Hooper
 *
 */
enum GamePanel {
	/**
	 * The sole GamePanel instance
	 */
	INSTANCE;
	/**
	 * The root of the scenegraph
	 */
	private final StackPane stack;
	
	/**
	 * The {@link SweeperGrid} associated with this {@link Application}
	 */
	private final SweeperGrid sweeperGrid;
	
	/**
	 * the Label used to display the number of "Flags Remaining." It can be changed by
	 * {@link #setFlagCount(int)}.
	 */
	private final Label flagsLabel;
	
	/**
	 * Constructs {@link #INSTANCE}.
	 */
	GamePanel() {
		stack = new StackPane();
		sweeperGrid = new SweeperGrid();
		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(sweeperGrid);
		flagsLabel = new Label("");
		borderPane.setBottom(flagsLabel);
		MenuBar menubar = new MenuBar();
		Menu newGame = new Menu("New Game");
		MenuItem custom = (new MenuItem("Custom..."));
		custom.setOnAction(x -> Popups.NEW_GAME.show());
		newGame.getItems().add(custom);
		
		menubar.getMenus().add(newGame);
		borderPane.setTop(menubar);
		stack.getChildren().add(borderPane);
	}
	
	/**
	 * @return the {@link Pane} that is the root of the scenegraph of this {@link Application}.
	 */
	public Pane getPane() {
		return stack;
	}
	
	/**
	 * @return the {@link SweeperGrid} associated with this {@link Application}.
	 */
	public SweeperGrid getSweeperGrid() {
		return sweeperGrid;
	}
	
	/**
	 * @return the {@link MinesweeperBoard} associated with this {@link Application}.
	 */
	public MinesweeperBoard getBoard() {
		return getSweeperGrid().getBoard();
	}
	
	/**
	 * Changes the {@link MinesweeperBoard} associated with this {@link Application} to {@code newBoard}.
	 * @param newBoard the board to change to.
	 */
	public void changeBoard(MinesweeperBoard newBoard) {
		sweeperGrid.changeBoard(newBoard);
		SolverGUI.INSTANCE.alignWithMainBoard();
	}
	
	/**
	 * Changes the number displayed as the number of "Flags Remaining" in the Pane returned by {@link #getPane()}
	 * to {@code count}.
	 * @param count the number to change the flag count to
	 */
	public void setFlagCount(int count) {
		flagsLabel.setText("Flags Remaining: " + count);
	}
}
