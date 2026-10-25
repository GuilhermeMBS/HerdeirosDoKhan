package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import loader.GameStateLoader;

import model.GameData;

/**
 * Initial configuration window allowing players to start a new match, load a saved match,
 * choose the number of active players (2 to 5), select the Karakorum setup variant
 * (default or alternative), and assign an Heir character to each player slot.
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

    /**
     * Constructs the initial setup window.
     */
    public InitialSetupFrame() {
        super("Heirs of the Khan - Match Setup");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(520, 460));
        setLayout(new BorderLayout(10, 10));

        JPanel configPanel = new JPanel(new GridLayout(8, 2, 8, 8));
        configPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        configPanel.add(new JLabel("Number of Players (2 - 5):"));
        this.playerCountCombo = new JComboBox<>(new Integer[] {2, 3, 4, 5});
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

        JPanel buttonBar = new JPanel(new GridLayout(1, 2, 12, 0));
        buttonBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        JButton startNewMatchButton = new JButton("Start New Match");
        JButton continueSavedButton = new JButton("Continue Saved Match");
        buttonBar.add(startNewMatchButton);
        buttonBar.add(continueSavedButton);
        add(buttonBar, BorderLayout.SOUTH);

        bindSetupListeners(startNewMatchButton, continueSavedButton);
        updateActiveHeirSelectors();
        setLocationRelativeTo(null);
    }

    /**
     * Binds listeners using classic anonymous classes (no lambda expressions).
     */
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
                launchConfiguredMatch(false);
            }
        });

        continueSavedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launchConfiguredMatch(true);
            }
        });
    }

    /**
     * Enables or disables player heir dropdowns according to the selected player count.
     */
    private void updateActiveHeirSelectors() {
        Integer selectedCount = (Integer) this.playerCountCombo.getSelectedItem();
        int count = selectedCount != null ? selectedCount.intValue() : 2;
        for (int i = 0; i < this.heirSelectors.size(); i++) {
            this.heirSelectors.get(i).setEnabled(i < count);
        }
    }

    /**
     * Closes the setup screen and opens the main board window.
     */
    private void launchConfiguredMatch(boolean loadFromDisk) {
        GameStateLoader loader = new GameStateLoader();

        Integer selectedCount = (Integer) this.playerCountCombo.getSelectedItem();
        int count = selectedCount != null ? selectedCount.intValue() : 2;

        List<String> chosenHeirs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            chosenHeirs.add((String) this.heirSelectors.get(i).getSelectedItem());
        }

        boolean altMode = this.alternativeKarakorumRadio.isSelected();
        GameData facade = GameData.createData(altMode);
        facade.setupMatch(chosenHeirs, altMode);

        MainGameFrame mainFrame = new MainGameFrame(facade, loader);
        if (loadFromDisk) {
            loader.loadMatchState("savegame.properties", facade, mainFrame.getBoardPanel());
        }

        mainFrame.setVisible(true);
        dispose();
    }
}
