package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import java.util.List;

import controller.Controller;

class KhanFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private final Controller controller;
    
    private final ButtonGroup destinationButton;
    private final JPanel regionPanel;
    
    KhanFrame(List<String> provinces) {
        super("Select Khan Destination");	
        
        controller = Controller.getController();
        
        setSize(new Dimension(500, 400));
        setResizable(true);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);
        
        this.destinationButton = new ButtonGroup();
        this.regionPanel = new JPanel(
        		new GridLayout(0, 2, 8, 8)
        );
        
        this.regionPanel.setBorder(
        		BorderFactory.createTitledBorder("Chose Destination")
        );
        
        for (String region : provinces) {
        	addDestinationButton(region);
        }
        
        addDestinationButton("UPGRADES");
        
        JPanel confirmPanel = new JPanel();
        JButton confirmButton = new JButton("Confirm");
        JButton cancelButton = new JButton("Cancel");
        
        confirmButton.addActionListener(e -> {
        	String destination = getSelectedDestination();
        	
        	if (destination == null) {
        		JOptionPane.showMessageDialog(
        				this,
        				"Please select a valid destination"
        		);
        		return;
        	}
        	
        	if (controller.createKhanAction(destination)) {
        		dispose();
        	} else {
        		JOptionPane.showMessageDialog(this,
        				"Khan movement rejected. Please choose another destination"
        		);
        	}
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        confirmPanel.add(cancelButton);
        confirmPanel.add(confirmButton);

        add(regionPanel, BorderLayout.CENTER);
        add(confirmPanel, BorderLayout.SOUTH);
    }
    
    private void addDestinationButton(String dest) {
    	String label = dest.equalsIgnoreCase("UPGRADES") ? "Upgrades Area" : dest;
    	
    	JRadioButton button = new JRadioButton(label);
    	
    	button.setActionCommand(dest);
    	
    	destinationButton.add(button);
    	regionPanel.add(button);
    }
    
    private String getSelectedDestination() {
    	if (destinationButton.getSelection() == null) return null;
    	return destinationButton.getSelection().getActionCommand();
    }
    
    public void refreshDisplay() {
    	this.repaint();
    }
}
