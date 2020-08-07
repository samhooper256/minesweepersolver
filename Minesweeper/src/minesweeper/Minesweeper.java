package minesweeper;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Minesweeper extends Application {
	//static final String RES_PREFIX = ""; //when in IDE
	static final String RES_PREFIX = "/"; //when exported
	
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(final Stage primaryStage) {
		Stage stage = primaryStage;
		stage.setTitle("Minesweeper");
		GamePanel.INSTANCE.changeBoard(MinesweeperBoard.newBoardEasyStart(20, 24, 0.20625f));
		Scene scene = new Scene(GamePanel.INSTANCE.getPane(), 600, 600);
		stage.setScene(scene);
		stage.show();
		SolverGUI.INSTANCE.getStage().show();
	}
}
