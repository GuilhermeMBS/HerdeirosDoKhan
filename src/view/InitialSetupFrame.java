package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import controller.Controller;
import loader.GameStateLoader;

/**
 * Initial configuration window allowing players to start a new match
 * or load a saved ASCII text match file via JFileChooser.
 */
public class InitialSetupFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final String[] AVAILABLE_HEIRS = new String[] {
        "Tolui", "Ogedei", "Chagatai", "Altani", "Jochi"
    };

    private final JComboBox<Integer> playerCountCombo;
    private final JRadioButton defaultKarakorumRadio;
    private final JRadioButton alternativeKarakorumRadio;
    private final List<JComboBox<String>> heirSelectors;
    private final JLabel statusLabel;

    public InitialSetupFrame() {
        super("Heirs of the Khan - Match Setup");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(520, 490));
        setLayout(new BorderLayout(10, 10));

        JPanel configPanel = new JPanel(new GridLayout(8, 2, 8, 8));
        configPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        configPanel.add(new JLabel("Number of Players (2 - 5):"));
        this.playerCountCombo = new JComboBox<>(new Integer[] { 2, 3, 4, 5 });
        configPanel.add(this.playerCountCombo);

        configPanel.add(new JLabel("Karakorum Trading Post Mode:"));
        JPanel radioPanel = new JPanel(new GridLayout(1, 2));
        this.defaultKarakorumRadio = new JRadioButton("Default", true);
        this.alternativeKarakorumRadio = new JRadioButton("Alternative", false);
        ButtonGroup group = new ButtonGroup();
        group.add(this.defaultKarakorumRadio);
        group.add(this.alternativeKarakorumRadio);
        radioPanel.add(this.defaultKarakorumRadio);
        radioPanel.add(this.alternativeKarakorumRadio);
        configPanel.add(radioPanel);

        this.heirSelectors = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            configPanel.add(new JLabel("Player " + (i + 1) + " Heir:"));
            JComboBox<String> selector = new JComboBox<>(AVAILABLE_HEIRS);
            selector.setSelectedIndex(i);
            this.heirSelectors.add(selector);
            configPanel.add(selector);
        }

        add(configPanel, BorderLayout.CENTER);

        JPanel southContainer = new JPanel(new BorderLayout(6, 6));
        southContainer.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        this.statusLabel = new JLabel("Configure a new match or select a saved .txt file to continue.");
        this.statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        southContainer.add(this.statusLabel, BorderLayout.NORTH);

        JPanel buttonBar = new JPanel(new GridLayout(1, 2, 12, 0));
        JButton startNewMatchButton = new JButton("Start New Match");
        JButton continueSavedButton = new JButton("Continue Saved Match (.txt)");
        buttonBar.add(startNewMatchButton);
        buttonBar.add(continueSavedButton);
        southContainer.add(buttonBar, BorderLayout.SOUTH);

        add(southContainer, BorderLayout.SOUTH);

        bindSetupListeners(startNewMatchButton, continueSavedButton);
        updateActiveHeirSelectors();
        setLocationRelativeTo(null);
    }

    private void bindSetupListeners(JButton startNewMatchButton, JButton continueSavedButton) {
        this.playerCountCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateActiveHeirSelectors();
            }
        });

        startNewMatchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launchNewMatch();
            }
        });

        continueSavedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launchSavedMatch();
            }
        });
    }

    private void updateActiveHeirSelectors() {
        Integer selectedCount = (Integer) this.playerCountCombo.getSelectedItem();
        int count = selectedCount != null ? selectedCount.intValue() : 2;
        for (int i = 0; i < this.heirSelectors.size(); i++) {
            this.heirSelectors.get(i).setEnabled(i < count);
        }
    }

    private void launchNewMatch() {
        GameStateLoader loader = new GameStateLoader();

        Integer selectedCount = (Integer) this.playerCountCombo.getSelectedItem();
        int count = selectedCount != null ? selectedCount.intValue() : 2;

        List<String> chosenHeirs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            chosenHeirs.add((String) this.heirSelectors.get(i).getSelectedItem());
        }

        boolean altMode = this.alternativeKarakorumRadio.isSelected();
        Controller controller = Controller.getController();
        controller.setupMatch(chosenHeirs, altMode);

        MainGameFrame mainFrame = new MainGameFrame(loader);
        mainFrame.setVisible(true);
        dispose();
    }

    private void launchSavedMatch() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Saved Match File (.txt)");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        int decision = fileChooser.showOpenDialog(this);
        if (decision != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();
        GameStateLoader loader = new GameStateLoader();

        // Load full match state BEFORE instantiating MainGameFrame so secret counselors
        // and turn states are already restored before observers initialize.
        boolean loaded = loader.loadMatchState(selectedFile);
        if (!loaded) {
            this.statusLabel.setForeground(new Color(180, 30, 30));
            this.statusLabel.setText("Failed to load saved file: " + selectedFile.getName());
            return;
        }

        MainGameFrame mainFrame = new MainGameFrame(loader);
        mainFrame.setVisible(true);
        dispose();
    }
}
