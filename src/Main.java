// import java.util.Scanner;

import javax.swing.SwingUtilities;

import view.InitialSetupFrame;

public class Main {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new InitialSetupFrame().setVisible(true);
			}
		});
	}
}
