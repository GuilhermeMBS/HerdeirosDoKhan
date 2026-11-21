package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controller.Controller;
import model.GameData;
import model.ObservableIF;
import model.ObserverIF;

class BonusCardFrame extends JFrame implements ObserverIF {
    private static final long serialVersionUID = 1L;
    
    private final HashMap<String, Image> images;
    private final HashMap<String, Rectangle> cardBounds = new HashMap<>();
    private final int SIZE = 80;
    private static final int PADDING = 18;
    private static final int TOP = 40;
    
    private final CanvasPanel canvas = new CanvasPanel();
    private final JButton confirmButton = new JButton("Use selected card");
    private final JLabel statusLabel = new JLabel(" ");
    
    private String selectedCard;
    
    private final Controller controller;
    
    BonusCardFrame() {
    	super("Bonus cards available");
    	
    	controller = Controller.getController();
    	
    	images = new HashMap<>();
    	for (Map.Entry<String, String> entry : controller.getBonusCardImgPath().entrySet()) {
    		images.put(entry.getKey(), loadOrCreatePlaceholder(entry.getValue(), SIZE, SIZE, new Color(125, 125, 125)));
    	}
    	
    	setLayout(new BorderLayout());
    	add(canvas, BorderLayout.CENTER);
    	
    	JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
    	south.add(statusLabel);
    	south.add(confirmButton);
    	add(south, BorderLayout.SOUTH);
    	
    	confirmButton.setEnabled(false);
    	
    	confirmButton.addActionListener(new ActionListener() {
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			useSelectedCard();
    		}
    	});
    	
    	setSize(4 * (SIZE + PADDING) + PADDING + 20, 2 * (SIZE + PADDING + 20) + TOP + 90);
    	setLocationRelativeTo(null);
    	
    	controller.register(this);
    	addWindowListener(new WindowAdapter() {
    		@Override
    		public void windowClosing(WindowEvent e) {
    			GameData.getData().remove(BonusCardFrame.this);
    		}
    	});
    	refresh();
    }

	@Override
	public void notify(ObservableIF o) {
		refresh();
	}
	
	private Image loadOrCreatePlaceholder(String path, int width, int height, Color fallbackColor) {
        BufferedImage loaded = ImageUtils.readProjectImage(path);
        if (loaded != null) {
            return loaded;
        }
        BufferedImage fallback = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = fallback.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fallbackColor);
        if (width > 100) {
            g2.fillRect(0, 0, width, height);
        } else {
            g2.fillOval(2, 2, width - 4, height - 4);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(2, 2, width - 4, height - 4);
        }
        g2.dispose();
        return fallback;
    }

	private void refresh() {
		List<String> cards = controller.getCurrentPlayerBonusCards();
		if (selectedCard != null && !cards.contains(selectedCard)) selectedCard = null;
		
		setTitle("Bonus Cards - " + controller.getCurrentHeirName());
		
		confirmButton.setEnabled(selectedCard != null);
		
		canvas.repaint();
	}
	
	private void useSelectedCard() {
		if (selectedCard == null) return;
		
		String name = selectedCard;
		if (controller.useBonusCard(name)) {
			statusLabel.setText(name + " used");
			selectedCard = null;
		} else {
			statusLabel.setText("Could not use " + name + ".");
		}
		
		refresh();		
	}
	
	private final class CanvasPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		CanvasPanel() {
			setBackground(new Color(230, 218, 195));
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					for (Map.Entry<String, Rectangle> entry : cardBounds.entrySet()) {
						if (entry.getValue().contains(e.getPoint())) {
							selectedCard = entry.getKey();
							statusLabel.setText(" ");
							confirmButton.setEnabled(true);
							repaint();
							return;
						}
					}
				}
			});
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g.create();
			
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			cardBounds.clear();
			List<String> cards = controller.getCurrentPlayerBonusCards();
			
			g2d.setColor(new Color(45, 35, 25));
			g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
			g2d.drawString(cards.isEmpty() ? "No Bonus Cards available" : "Click a card, then confirm", PADDING, 24);
			
			int perRow = Math.max(1, (getWidth() - PADDING) / (SIZE + PADDING));
			for (int i = 0; i < cards.size(); i++) {
				String name = cards.get(i);
				int x = PADDING + (i % perRow) * (SIZE + PADDING);
				int y = TOP + (i / perRow) * (SIZE + PADDING + 20);
				
				Rectangle rec = new Rectangle(x, y, SIZE, SIZE);
				cardBounds.put(name, rec);
				
				Image img = images.get(name);
				if (img != null) g2d.drawImage(img, x, y, SIZE, SIZE, this);
				
				g2d.setColor(new Color(45, 35, 25));
				g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
				g2d.drawString(name, x, y + SIZE + 15);
				
				if (name.equalsIgnoreCase(selectedCard)) {
					g2d.setColor(new Color(255, 215, 0));
					g2d.setStroke(new BasicStroke(4f));
					g2d.drawRect(x - 2, y - 2, SIZE + 4, SIZE + 4);
				}
			}
			g2d.dispose();
		}
	}
}
