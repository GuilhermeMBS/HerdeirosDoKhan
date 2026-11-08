package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controller.Controller;

class PrivateCounselorSelectorFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private static final int COUNSELOR_WIDTH = 155;
    private static final int COUNSELOR_HEIGHT = 270;
    private static final int PADDING = 18;
    private static final int GAP = 40;

    private static final String PATH = "assets/privateCounselors/";
    private final Map<String, Image> privateCounselorImages = new HashMap<>();
    
    private final List<String> options;
    private final List<Rectangle> cardBounds = new ArrayList<>();
    private final CardsCanvaPanel canvasPanel;
    private final JButton confirmButton;
    
    private int selectedIndex = -1;
    private String message = "Click a card, then press confirm. This choice is final";
    private Color msgColor = new Color(45, 35, 25);
	
	private Controller controller;
	
	PrivateCounselorSelectorFrame() {
		super("Select your private card");
		
		controller = Controller.getController();
		options = new ArrayList<>(controller.getPrivateCounselorCandidates());
		
		int frameWidth = (5 * COUNSELOR_WIDTH) + (7 * PADDING) + GAP;
        int frameHeight = COUNSELOR_HEIGHT + (2 * PADDING) + 95;

        setSize(new Dimension(frameWidth, frameHeight));
        setResizable(false);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
		setAlwaysOnTop(true);
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        
        loadPrivateCounselorImages();
        
        canvasPanel = new CardsCanvaPanel();
		add(canvasPanel, BorderLayout.CENTER);

		confirmButton = new JButton("Confirm choice");
		confirmButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				confirmChoice();
			}
		});
		
		JPanel southPanel = new JPanel();
		southPanel.add(confirmButton);
		add(southPanel, BorderLayout.SOUTH);
	}
	
	private void loadPrivateCounselorImages() {
		for (String name : options) {
			if (!this.privateCounselorImages.containsKey(name)) {
				this.privateCounselorImages.put(name, loadOrCreateCardImage(name));
			}
		}
	}
	
	private Image loadOrCreateCardImage(String name) {
		BufferedImage img = ImageUtils.readProjectImage(PATH + name + ".jpg");
		if (img == null) {
			img = new BufferedImage(COUNSELOR_WIDTH, COUNSELOR_HEIGHT, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = img.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(new Color(45, 35, 70));
			g2.fillRoundRect(2, 2, COUNSELOR_WIDTH - 4, COUNSELOR_HEIGHT - 4, 14, 14);
			g2.setColor(new Color(218, 165, 32));
			g2.setStroke(new BasicStroke(2.5f));
			g2.drawRoundRect(2, 2, COUNSELOR_WIDTH - 4, COUNSELOR_HEIGHT - 4, 14, 14);
			g2.setFont(new Font("SansSerif", Font.BOLD, 11));
			g2.drawString("SECRET COUNSELOR", 14, 28);
			g2.setColor(Color.WHITE);
			g2.setFont(new Font("SansSerif", Font.BOLD, 14));
			g2.drawString(name, 14, COUNSELOR_HEIGHT / 2);
			g2.dispose();
		}
		
		return img;
	}
	
	private void selectCard(int mouseX, int mouseY) {
		for (int i = 0; i < cardBounds.size(); i++) {
			if (cardBounds.get(i).contains(mouseX, mouseY)) {
				selectedIndex = i;
				message = "Selected: " + options.get(i) + ". Press confirm to keep it";
				msgColor = new Color(45, 35, 25);
				canvasPanel.repaint();
				return;
			}
		}
	}
	
	private void confirmChoice() {
		if (selectedIndex < 0 || selectedIndex >= options.size()) {
			message = "Please select one of the counselor first";
			msgColor = new Color(180, 30, 30);
			canvasPanel.repaint();
			return;
		}
		
		if (controller.choosePrivateCounselor(options.get(selectedIndex))) {
			dispose();
		} else {
			message = "Choice rejected: the counselor was already chosen";
			msgColor = new Color(180, 30, 30);
			canvasPanel.repaint();
		}
	}
	
	private final class CardsCanvaPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		private CardsCanvaPanel() {
			setBackground(new Color(230, 218, 195));
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					selectCard(e.getX(), e.getY());
				}
			});
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			
			cardBounds.clear();
			
			g2.setColor(new Color(55, 30, 85));
			g2.setFont(new Font("SansSerif", Font.BOLD, 14));
			g2.drawString(controller.getCurrentHeirName() + ", choose your Secret Counselor", PADDING, 26);
			
			int totalWidth = (2 * COUNSELOR_WIDTH) + GAP;
			int startX = (getWidth() - totalWidth) / 2;
			int startY = 46;
			
			for (int i = 0; i < options.size(); i++) {
				String name = options.get(i);
				int x = startX + i * (COUNSELOR_WIDTH + GAP);
				cardBounds.add(new Rectangle(x, startY, COUNSELOR_WIDTH, COUNSELOR_HEIGHT));
				
				Image img = privateCounselorImages.get(name);
				if (img != null) g2.drawImage(img, x, startY, COUNSELOR_WIDTH, COUNSELOR_HEIGHT, this);
				
				if (i == selectedIndex) {
					g2.setColor(new Color(218, 165, 32));
					g2.setStroke(new BasicStroke(5f));
					g2.drawRoundRect(x - 4, startY - 4, COUNSELOR_WIDTH + 8, COUNSELOR_HEIGHT + 8, 16, 16);
				}
				
				g2.setColor(new Color(45, 35, 25));
				g2.setFont(new Font("SansSerif", Font.BOLD, 13));
				g2.drawString(name, x, startY + COUNSELOR_HEIGHT + 22);
			}
			
			g2.setColor(msgColor);
			g2.setFont(new Font("SansSerif", Font.BOLD, 12));
			g2.drawString(message, PADDING, startY + COUNSELOR_HEIGHT + 75);

			g2.dispose();
		}
		
	}
}
