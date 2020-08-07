package minesweeper;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * <p>An enum describing, as enum constants, the popup dialogs used in this {@link javafx.application.Application}.</p>
 * 
 * <p>Each enum constant is associated with a {@link Stage}, which can be retrieved via the {@link #getStage()} method.
 * The stage is the popup window for that popup. The {@link #show()} method can be used to show the popup.</p>
 * @author Sam Hooper
 *
 */
enum Popups {
	
	/**
	 * The "New Game" popup, which allows the user to create a new board and, on their confirmation, sets
	 * that newly created board to the board associated with this {@link Application} by invoking
	 * {@link GamePanel#changeBoard(MinesweeperBoard)}.
	 */
	NEW_GAME("New Game"){
		
		/**
		 * The text field where the user will input the number of rows in the new board.
		 */
		private TextField rowField;
		/**
		 * The text field where the user will input the number of columns in the new board.
		 */
		private TextField colField;
		/**
		 * The text field where the user will input the mine percentage of the new board.
		 */
		private TextField minePercentField;
		/**
		 * The button the user can press to create a new board.
		 */
		private Button newGameButton;
		/**
		 * A Label holding as its text the error message displayed when the user has provided invalid input.
		 * Holds the empty String as its text when no error message needs to be displayed.
		 */
		private Label errorMessage;
		
		/**
		 * An indicator determining whether or not the new board will have an easy start, as defined by
		 * {@link MinesweeperBoard}. The board will have an easy start if {@code easyStart} is checked, and
		 * will not otherwise.
		 */
		private CheckBox easyStart;
		
		@Override
		protected void initialLayout() {
			getStage().initModality(Modality.APPLICATION_MODAL);
			VBox vBox = new VBox(5);
			vBox.setPadding(new Insets(10));
			vBox.setAlignment(Pos.CENTER);
			Label infoLabel = new Label("Create new board");
			
			HBox rowInput = new HBox(10);
			rowInput.setAlignment(Pos.CENTER);
			rowField = new TextField();
			rowInput.getChildren().addAll(new Label("Number of rows: "), rowField);
			
			HBox colInput = new HBox(10);
			colInput.setAlignment(Pos.CENTER);
			colField = new TextField();
			colInput.getChildren().addAll(new Label("Number of columns: "), colField);
			
			
			HBox minePercentInput = new HBox(10);
			minePercentInput.setAlignment(Pos.CENTER);
			minePercentField = new TextField();
			minePercentInput.getChildren().addAll(
					new Label("Percentage of tiles that will be mines: "), minePercentField);
			
			if(GamePanel.INSTANCE.getBoard() != null) {
				rowField.setText(String.valueOf(GamePanel.INSTANCE.getBoard().getRows()));
				colField.setText(String.valueOf(GamePanel.INSTANCE.getBoard().getColumns()));
				minePercentField.setText(String.format("%.3f", GamePanel.INSTANCE.getBoard().getMinePercent() * 100));
			}
			HBox easyStartInput = new HBox(10);
			easyStart = new CheckBox();
			easyStart.setSelected(true);
			easyStartInput.getChildren().addAll(easyStart, new Label("Guarantee first tile has zero adjacent mines"));
			
			newGameButton = new Button("New Game");
			newGameButton.setOnMouseClicked(mouseEvent -> attemptCreate());
			
			errorMessage = new Label();
			errorMessage.setVisible(true);
			vBox.getChildren().addAll(infoLabel, rowInput, colInput, minePercentInput, easyStartInput, newGameButton,
					errorMessage);
			Scene scene = new Scene(vBox);
			getStage().setScene(scene);
		}
		
		/**
		 * Attempts to create a new Minesweeper board and change the board associated with this {@link Application}
		 * to that newly created board. If the values input by the user do not satisfy the conditions of
		 * {@link #verifyInput()} or {@link #verifyValues(int, int, float)} (that is, either of those method returns
		 * {@code false}), this method will <b>not</b> create a new board but may still have the effect of cleaning up
		 * the user's existing (though invalid) input.
		 */
		private void attemptCreate() {
			errorMessage.setText("");
			applyToFields(x -> x.setBorder(null));
			if(!verifyInput()) {
				return;
			}
			else {
				applyToFieldTexts(String::strip);
			}
			int row = Integer.parseInt(rowField.getText());
			int col = Integer.parseInt(colField.getText());
			float minePercent = 0.01f * Float.parseFloat(minePercentField.getText());
			if(verifyValues(row, col, minePercent)) {
				GamePanel.INSTANCE.changeBoard(easyStart.isSelected() ? 
						MinesweeperBoard.newBoardEasyStart(row, col, minePercent) :
						MinesweeperBoard.newBoard(row, col, minePercent));
				getStage().hide();
			}
			
		}
		
		/**
		 * Determines whether or not the values for a new board's row count, column count, and mine percentage
		 * respectively would cause an {@link IllegalArgumentException} in {@link MinesweeperBoard}.
		 * @param row the proposed row count of the board
		 * @param col the propsed column count of the board
		 * @param minePercent the proposed mine percentage for the board
		 * @return {@code true} if and only if the proposed values would not cause an {@link IllegalArgumentException}
		 * if used to create a new {@link MinesweeperBoard}, {@code false} otherwise.
		 */
		private boolean verifyValues(int row, int col, float minePercent) {
			if(row < MinesweeperBoard.MIN_ROWS || row > MinesweeperBoard.MAX_ROWS) {
				errorMessage.setText(String.format("row must be between %d and %d",
						MinesweeperBoard.MIN_ROWS, MinesweeperBoard.MAX_ROWS));
				return false;
			}
			else if(col < MinesweeperBoard.MIN_COLS || col > MinesweeperBoard.MAX_COLS) {
				errorMessage.setText(String.format("col must be between %d and %d",
						MinesweeperBoard.MIN_COLS, MinesweeperBoard.MAX_COLS));
				return false;
			}
			else if(minePercent < 0.0f || minePercent > 1.0f) {
				errorMessage.setText("mine percentage must be between 0 and 100");
				return false;
			}
			return true;
		}
		
		/**
		 * Applies the given {@link Consumer} to {@link #rowField}, {@link #colField}, and {@link #minePercentField}.
		 * @param op the consumer
		 */
		private void applyToFields(Consumer<? super TextField> op) {
			op.accept(rowField);
			op.accept(colField);
			op.accept(minePercentField);
		}
		
		/**
		 * Applies the given {@link UnaryOperator} to the text in {@link #rowField}, {@link #colField},
		 * and {@link #minePercentField}, and sets the result as the new text of the corresponding fields.
		 * @param op the consumer
		 */
		private void applyToFieldTexts(UnaryOperator<String> op) {
			rowField.setText(op.apply(rowField.getText()));
			colField.setText(op.apply(colField.getText()));
			minePercentField.setText(op.apply(minePercentField.getText()));
		}
		
		/**
		 * Ensures that the user input valid numbers. Note that this method only verifies that the input is well
		 * formed, not that the input contains legal values for a Minesweeper board. For the users input to be valid,
		 * all of {@link #verifyRow()}, {@link #verifyCol()}, and {@link #verifyMinePercent()} must
		 * return {@code true}.
		 * @return {@code true} if any only if the user's input is well-formed, {@code false} otherwise.
		 */
		private boolean verifyInput() {
			return verifyRow() && verifyCol() && verifyMinePercent();
		}
		
		/**
		 * Indicates that an error has occurred if this method returns {@code false}.
		 * @return {@code true} if and only if the text in {@link #rowField} represents a nonnegative integer,
		 * {@code false} otherwise.
		 */
		private boolean verifyRow() {
			String text = rowField.getText().strip();
			if(DIGITS.matcher(text).matches()) {
				return true;
			}
			else {
				rowField.setBorder(new Border(new BorderStroke(
						Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
				return false;
			}
		}
		
		/**
		 * Indicates that an error has occurred if this method returns {@code false}.
		 * @return {@code true} if and only if the text in {@link #colField} represents a nonnegative integer,
		 * {@code false} otherwise.
		 */
		private boolean verifyCol() {
			String text = colField.getText().strip();
			if(DIGITS.matcher(text).matches()) {
				return true;
			}
			else {
				colField.setBorder(new Border(new BorderStroke(
						Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
				return false;
			}
		}
		
		/**
		 * Indicates that an error has occurred if this method returns {@code false}.
		 * @return {@code true} if and only if the text in {@link #minePercentField} represents a nonnegative
		 * real number, {@code false} otherwise.
		 */
		private boolean verifyMinePercent() {
			String text = minePercentField.getText().strip();
			if(FLOATING_POINT.matcher(text).matches()) {
				return true;
			}
			else {
				minePercentField.setBorder(new Border(new BorderStroke(
						Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
				return false;
			}
		}
	};
	
	/**
	 * A {@link Pattern} matching a series of one or more digits.
	 */
	private static final Pattern DIGITS = Pattern.compile("\\d+");
	
	/**
	 * A {@link Pattern} matching a nonnegative real number.
	 */
	private static final Pattern FLOATING_POINT = Pattern.compile("[0-9]+(\\.[0-9]+)?|\\.[0-9]+");
	
	/**
	 * The stage associated with this popup.
	 */
	private Stage stage;
	
	/**
	 * Creates a popup whose stage has the title {@code stageTitle}. This constructor invokes the
	 * {@link #initialLayout()} method.
	 * @param stageTitle the title of the stage
	 */
	Popups(final String stageTtile) {
		stage = new Stage();
		stage.setTitle(stageTtile);
		initialLayout();
	}
	
	/**
	 * Lays out the contents of the popup. This method is responsible for creating a scene and adding it
	 * to the scene. This method may invoke {@link Stage#initModality(Modality)} and
	 * {@link Stage#initStyle(javafx.stage.StageStyle)}.
	 */
	protected abstract void initialLayout();
	
	/**
	 * @return the stage associated with this popup
	 */
	public Stage getStage() {
		return stage;
	}
	
	/**
	 * Shows this popup. An invocation of this method is equivalent to:
	 * <blockquote><pre>
	 * getStage().show();
	 * </pre></blockquote>
	 */
	public void show() {
		stage.show();
	}
}
