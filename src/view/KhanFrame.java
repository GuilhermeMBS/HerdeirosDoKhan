package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import controller.Controller;

/**
 * Non-modal window allowing the active player to select a destination
 * for the Genghis Khan piece without using lambda expressions or modal pop-ups.
 */
class KhanFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final Controller controller;
    private final ButtonGroup destinationButtonGroup;
    private final JPanel regionPanel;
    private final JLabel feedbackLabel;

    KhanFrame(List<String> provinces) {
        super("Select Khan Destination");

        this.controller = Controller.getController();

        setSize(new Dimension(500, 400));
        setResizable(true);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        this.destinationButtonGroup = new ButtonGroup();
        this.regionPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        this.regionPanel.setBorder(BorderFactory.createTitledBorder("Choose Destination"));

        for (String region : provinces) {
            addDestinationButton(region);
        }
        addDestinationButton("UPGRADES");

        this.feedbackLabel = new JLabel("Select a province or the Upgrades Area.");
        this.feedbackLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.feedbackLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        this.feedbackLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 2, 10));

        JPanel southContainer = new JPanel(new BorderLayout(4, 4));
        JPanel confirmPanel = new JPanel();
        JButton confirmButton = new JButton("Confirm");
        JButton cancelButton = new JButton("Cancel");

        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String dest = getSelectedDestination();
                if (dest == null) {
                    feedbackLabel.setForeground(new Color(180, 30, 30));
                    feedbackLabel.setText("Please select a valid destination first.");
                    return;
                }

                if (controller.createKhanAction(dest)) {
                    dispose();
                } else {
                    feedbackLabel.setForeground(new Color(180, 30, 30));
                    feedbackLabel.setText("Khan movement rejected. Check Khan action availability or destination.");
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        confirmPanel.add(cancelButton);
        confirmPanel.add(confirmButton);

        southContainer.add(this.feedbackLabel, BorderLayout.NORTH);
        southContainer.add(confirmPanel, BorderLayout.SOUTH);

        add(this.regionPanel, BorderLayout.CENTER);
        add(southContainer, BorderLayout.SOUTH);
    }

    private void addDestinationButton(String dest) {
        String label = "UPGRADES".equalsIgnoreCase(dest) ? "Upgrades Area" : dest;
        JRadioButton button = new JRadioButton(label);
        button.setActionCommand(dest);
        this.destinationButtonGroup.add(button);
        this.regionPanel.add(button);
    }

    private String getSelectedDestination() {
        if (this.destinationButtonGroup.getSelection() == null) {
            return null;
        }
        return this.destinationButtonGroup.getSelection().getActionCommand();
    }

    public void refreshDisplay() {
        this.repaint();
    }
}
