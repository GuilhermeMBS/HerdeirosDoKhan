package view;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controller.Controller;
import model.ObservadoIF;
import model.ObservadorIF;
import model.TreasureType;
import model.TributePosition;
import model.TributeType;

class PlayerInventoryPanel extends JPanel implements ObservadorIF {
	private static final long serialVersionUID = 1L;
	
	private static final int COLUMNS = 3;
	private static final int GRID_CHIPS = 12;
	private static final int CHIP_HEIGHT = 36;
	
	private static final int GAP = 6;
	private static final int PAD = 6;
	
	private static final String ICON_FOLDER = "assets/";
	
	private static final String[] FOLDER_NAMES = {
			"tributeType/", "treasureType/", "actions/"
	};
	
	private static final Map<String, String[]> FOLDER_ICON_NAMES = new HashMap<String, String[]>();
	private final Map<String, BufferedImage> icons = new HashMap<String, BufferedImage>();

	private static final Map<TributeType, Color> TRIBUTE_COLORS = new HashMap<>();
	
	private static final Color INK = new Color(35, 30, 25);
	
	private final Controller controller;

	PlayerInventoryPanel() {
		controller = Controller.getController();
		
		setPreferredSize(new Dimension(340, 300));
		setBackground(new Color(245, 240, 230));
		setBorder(BorderFactory.createTitledBorder("Actions, Tributes & Resources"));
		setToolTipText("");
		
		FOLDER_ICON_NAMES.put(FOLDER_NAMES[0], new String[] {
				"COIN", "SWORD", "YURT"
		});
		FOLDER_ICON_NAMES.put(FOLDER_NAMES[1], new String[] {
				"ANIMAL", "FUR", "SILK", "IRON", "SPICE"
		});
		FOLDER_ICON_NAMES.put(FOLDER_NAMES[2], new String[] {
				"Hand", "Horse", "Khan"
		});
		
		TRIBUTE_COLORS.put(TributeType.COIN, new Color(180, 125, 20));
		TRIBUTE_COLORS.put(TributeType.SWORD, new Color(165, 35, 35));
		TRIBUTE_COLORS.put(TributeType.YURT, new Color(40, 110, 60));
		
		loadIcons();		
	}
	
	private void loadIcons() {
		for (String folder : FOLDER_NAMES) {
			String[] icons_names = FOLDER_ICON_NAMES.get(folder);
			for (String icon : icons_names) {
				String path = ICON_FOLDER + folder + icon + ".jpg";
				File file = new File(path);
				if (!file.isFile()) continue;
				try {
					BufferedImage img = ImageIO.read(file);
					this.icons.put(icon, img);
				} catch (IOException e) {
					// Something
				}
			}
		}
	}
	
	
	/*
	 * ==============================================
	 * ==================== Data ====================
	 * ==============================================
	 */
	
	private static final class Chip {
		final String key;
		final String tooltip;
		final Color color;
		final String qty;
		
		Chip(String key, String tooltip, Color color, String qty) {
			this.key = key;
			this.tooltip = tooltip;
			this.color = color;
			this.qty = qty;
		}
	}
	
	private String withVirtual(int[] amount) {
		String ret = "" + amount[TributePosition.TRIBUTE.ordinal()];
		if (amount[TributePosition.VIRTUAL.ordinal()] > 0) ret += "(+" + amount[TributePosition.VIRTUAL.ordinal()] + ")";
		return ret;
	}
	
	private List<Chip> buildChips() {
		List<Chip> chips = new ArrayList<>();
		
		chips.add(new Chip("Hand", "Hands left", new Color(130, 95, 60),
                String.valueOf(controller.getCurrentPlayerHandsLeft()))
		);
		chips.add(new Chip("Horse", "Movements Left", new Color(190, 130, 25),
                String.valueOf(controller.getCurrentPlayerMovementsLeft()))
		);
		chips.add(new Chip("YS", "Yurts in stock", new Color(100, 130, 80),
                String.valueOf(controller.getCurrentPlayerYurtsLeft())));
		
		for (TributeType type : TributeType.values()) {
			int [] values = controller.getCurrentPlayerTributeAmount(type);
			chips.add(new Chip(
					type.toString(),
					type.toString().substring(0,1).toUpperCase() + type.toString().substring(1).toLowerCase() + " (virtual in parentheses)",
					TRIBUTE_COLORS.get(type),
					withVirtual(values)
				)
			);
		}

        chips.add(new Chip(TreasureType.FUR.toString(), "Fur", new Color(120, 120, 140),
                String.valueOf(controller.getCurrentPlayerTreasure(TreasureType.FUR))));
        chips.add(new Chip(TreasureType.SILK.toString(), "Silk", new Color(80, 50, 130),
                String.valueOf(controller.getCurrentPlayerTreasure(TreasureType.SILK))));
        chips.add(new Chip(TreasureType.IRON.toString(), "Iron", new Color(110, 50, 50),
                String.valueOf(controller.getCurrentPlayerTreasure(TreasureType.IRON))));
        chips.add(new Chip(TreasureType.ANIMAL.toString(), "Animal", new Color(45, 85, 55),
                String.valueOf(controller.getCurrentPlayerTreasure(TreasureType.ANIMAL))));
        chips.add(new Chip(TreasureType.SPICE.toString(), "Spice", new Color(200, 100, 40),
                String.valueOf(controller.getCurrentPlayerTreasure(TreasureType.SPICE))));
        
        chips.add(new Chip("Khan", "Khan action", new Color(40, 60, 120),
                controller.isKhanPending() ? "READY" : "-"));
        return chips;
	}
	
	/*
	 * ==============================================
	 * =================== Layout ===================
	 * ==============================================
	 */
	
	private Rectangle chipBounds(int ind) {
		Insets in = getInsets();
		int x0 = in.left + PAD;
		int y0 = in.top + PAD;
		
		int totalW = getWidth() - in.left - in.right - 2*PAD;
		int rows = GRID_CHIPS / COLUMNS;
		
		if (ind >= GRID_CHIPS) {
			return new Rectangle(x0, y0 + rows * (CHIP_HEIGHT + GAP), totalW, CHIP_HEIGHT);
		}
		
		int chipW = (totalW - (COLUMNS - 1) * GAP) / COLUMNS;
		int col = ind % COLUMNS;
		int row = ind / COLUMNS;
		
		return new Rectangle(x0 + col * (chipW + GAP), y0 + row * (CHIP_HEIGHT + GAP), chipW, CHIP_HEIGHT);
	}
	
	@Override
	public String getToolTipText(MouseEvent event) {
		List<Chip> chips = buildChips();
		for (int i = 0; i < chips.size(); i++) {
			if (chipBounds(i).contains(event.getPoint())) return chips.get(i).tooltip;
		}
		return null;
	}
	
	/*
	 * =============================================
	 *  ================= Painting =================
	 * =============================================
	 */
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 =(Graphics2D) g.create();
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		List<Chip> chips = buildChips();
		for (int i = 0; i < chips.size(); i++) {
			drawChip(g2, chips.get(i), chipBounds(i));
		}
		
//		Rectangle last = chipBounds(chips.size() - 1);
//		int xLeft = last.x;
//		int y = last.y + last.height + 14;
		
//		g2.setStroke(new BasicStroke(1f));
//		g2.setColor(new Color(160, 145, 125));
//		g2.drawLine(xLeft, y -6, last.x + last.width, y - 6);
		
//		g2.setColor(new Color(30, 30, 30));
//		g2.setFont(new Font("SansSerif", Font.BOLD, 11));
//		g2.drawString("Match Scoreboard: ", xLeft, y + 8);
//		
//		y += 22;
//		
//		g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
//		List<String> standings = controller.getScoreboardSummary();
//		for (String line : standings) {
//			g2.drawString(line,  xLeft, y);
//			y += 14;
//		}
		
		g2.dispose();
	}
	
	private void drawChip(Graphics2D g2, Chip chip, Rectangle rec) {
		Shape body = new RoundRectangle2D.Double(rec.x, rec.y, rec.width - 1, rec.height - 1, rec.height, rec.height);
		g2.setColor(new Color(255, 252, 245));
		g2.fill(body);
		
		g2.setColor(INK);
		g2.setStroke(new BasicStroke(2f));
		g2.draw(body);
		
		int d = rec.height - 8;
		int ix = rec.x + 4;
		int iy = rec.y + 4;
		drawIcon(g2, chip, ix, iy, d);
		
		int textX0 = ix + d + 4;
		int textW = rec.x + rec.width - 10 - textX0;
		int size = 12;
		Font font;
		FontMetrics fm;
		
		do {
			font = new Font("SansSerif", Font.BOLD, size);
			fm = g2.getFontMetrics(font);
			size--;
		} while (fm.stringWidth(chip.qty) > textW && size >= 8);
		
		g2.setFont(font);
		g2.setColor(INK);
		
		int tx = textX0 + (textW - fm.stringWidth(chip.qty)) / 2;
		int ty = rec.y + (rec.height - fm.getHeight()) / 2 + fm.getAscent();
		g2.drawString(chip.qty, tx, ty);
	}
	
	private void drawIcon(Graphics2D g2, Chip c, int x, int y, int d) {
		Ellipse2D circle = new Ellipse2D.Double(x, y, d, d);
		BufferedImage img = icons.get(c.key);
		
		if (img != null) {
			Shape oldClip = g2.getClip();
			g2.setClip(circle);
			g2.drawImage(img,  x, y, d, d, null);
			g2.setClip(oldClip);
		} else {
			g2.setColor(c.color);
			g2.fill(circle);
			
			g2.setFont(new Font("SansSerif", Font.BOLD, 9));
			FontMetrics fm = g2.getFontMetrics();
			g2.setColor(Color.WHITE);
			
			g2.drawString(c.key, x + (d - fm.stringWidth(c.key)) / 2, y + (d - fm.getHeight()) / 2 + fm.getAscent());
		}
		
		g2.setColor(INK);
		g2.setStroke(new BasicStroke(1.5f));
		g2.draw(circle);
	}

	/*
	 * ==============================================
	 * ================== Observer ==================
	 * ==============================================
	 */
	
	@Override
	public void notify(ObservadoIF o) {
		this.repaint();
	}
}
