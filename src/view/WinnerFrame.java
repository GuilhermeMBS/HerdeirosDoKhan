package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import controller.Controller;
import model.PlayerColor;

/**
 * End of match screen. Shows the ranking of the players (from the highest to the
 * lowest score), points out the winner and asks if the players want a new match:
 * "New Match" reopens the heir selection window, "Exit" closes the application.
 * Closing this window also closes the application.
 *
 * @author Vinicius
 */
class WinnerFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private static final HashMap<PlayerColor, Color> COLORS = new HashMap<>();
    static {
        COLORS.put(PlayerColor.BLUE, new Color(30, 90, 200));
        COLORS.put(PlayerColor.RED, new Color(200, 40, 40));
        COLORS.put(PlayerColor.GREEN, new Color(30, 140, 60));
        COLORS.put(PlayerColor.YELLOW, new Color(230, 190, 30));
        COLORS.put(PlayerColor.BLACK, new Color(40, 40, 40));
    }

    private final Controller controller = Controller.getController();

    WinnerFrame() {
        super("Heirs of the Khan - End of Match");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(new RankingPanel(sortedByScore()), BorderLayout.CENTER);

        JPanel questionBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        questionBar.add(new JLabel("Do you want to start a new match?"));
        JButton newMatchButton = new JButton("New Match");
        JButton exitButton = new JButton("Exit");
        newMatchButton.addActionListener(new NewMatchListener());
        exitButton.addActionListener(new ExitListener());
        questionBar.add(newMatchButton);
        questionBar.add(exitButton);
        add(questionBar, BorderLayout.SOUTH);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    /**
     * @return the player indexes ordered from the highest to the lowest final score
     */
    private List<Integer> sortedByScore() {
        List<Integer> remaining = new ArrayList<>();
        for (int i = 0; i < controller.getTotalPlayers(); i++) {
            remaining.add(i);
        }
        List<Integer> sorted = new ArrayList<>();
        // Selection sort: each round takes the player with the highest score left
        while (!remaining.isEmpty()) {
            int best = remaining.get(0);
            for (int player : remaining) {
                if (controller.getFinalScore(player) > controller.getFinalScore(best)) {
                    best = player;
                }
            }
            sorted.add(best);
            remaining.remove(Integer.valueOf(best));
        }
        return sorted;
    }

    /** Closes every window of the finished match and reopens the heir selection. */
    private class NewMatchListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (Window window : Window.getWindows()) {
                window.dispose();
            }
            new InitialSetupFrame().setVisible(true);
        }
    }

    /** Closes the application. */
    private class ExitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    /**
     * Draws the ranking with Graphics2D: title, the winner(s) and one line per player
     * with its color, heir and final score.
     */
    private class RankingPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private static final int WIDTH = 520;
        private static final int TOP = 120;
        private static final int ROW = 46;

        private final List<Integer> ranking;

        RankingPanel(List<Integer> ranking) {
            this.ranking = ranking;
            setPreferredSize(new Dimension(WIDTH, TOP + ROW * ranking.size() + 20));
            setBackground(new Color(240, 230, 205));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(90, 50, 20));
            g2.setFont(new Font(Font.SERIF, Font.BOLD, 30));
            drawCentered(g2, "MATCH OVER", 45);

            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
            drawCentered(g2, winnerText(), 85);

            for (int pos = 0; pos < ranking.size(); pos++) {
                drawRow(g2, pos, ranking.get(pos), TOP + pos * ROW);
            }
        }

        /** One line of the ranking: position, color circle, heir name and score. */
        private void drawRow(Graphics2D g2, int pos, int player, int y) {
            boolean winner = controller.isWinner(player);
            if (winner) {
                g2.setColor(new Color(255, 215, 0, 120));
                g2.fillRoundRect(20, y, WIDTH - 40, ROW - 6, 12, 12);
            }

            g2.setColor(COLORS.getOrDefault(controller.getPlayerColor(player), Color.GRAY));
            g2.fillOval(70, y + 7, 26, 26);

            g2.setColor(Color.BLACK);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
            g2.drawString((pos + 1) + ".", 35, y + 27);
            g2.drawString(controller.getPlayerHeirName(player), 110, y + 27);
            g2.drawString(controller.getFinalScore(player) + " pts", WIDTH - 170, y + 27);
            if (winner) {
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
                g2.drawString("WINNER", WIDTH - 90, y + 26);
            }
        }

        /** "Winner: X" or, when there is a tie, "Tie: X and Y". */
        private String winnerText() {
            List<String> winners = new ArrayList<>();
            for (int player : ranking) {
                if (controller.isWinner(player)) {
                    winners.add(controller.getPlayerHeirName(player));
                }
            }
            String points = " (" + controller.getWinningScore() + " pts)";
            if (winners.size() == 1) {
                return "Winner: " + winners.get(0) + points;
            }
            return "Tie: " + String.join(" and ", winners) + points;
        }

        private void drawCentered(Graphics2D g2, String text, int y) {
            int x = (WIDTH - g2.getFontMetrics().stringWidth(text)) / 2;
            g2.drawString(text, x, y);
        }
    }
}
