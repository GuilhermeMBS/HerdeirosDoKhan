package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import java.util.HashMap;
import java.util.List;

import controller.Controller;

class OpenCounselorFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private final String COMMON_PATH = "assets/cartas/";
	private final int COUNSELOR_WIDTH = 100;
	private final int COUNSELOR_HEIGHT = 300;
	private HashMap<String, Image> counselorImages;
	
	JPanel CounselorPanel;
	
	
	OpenCounselorFrame() {
		super("Open Counselors");
		
		setSize(new Dimension(4*this.COUNSELOR_WIDTH + 100, this.COUNSELOR_HEIGHT + 100));
        setResizable(true);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);
        
        this.CounselorPanel = new JPanel(
        		new GridLayout(0, 4, 0, 0)
        );
        
		counselorImages = new HashMap<>();
		List<String> tmp = Controller.getController().getOpenCounselorsIds();
		for (String name : tmp) {
			String path = COMMON_PATH;
			if (name.startsWith("POWERUPS_COL") || name.startsWith("POWERUPS_SQUARE")) {
				path += "cartas3/cartas3-";
				if (name.startsWith("POWERUPS_COL")) path += "1.jpg";
				else path += "2.jpg";
			}
			else if (name.startsWith("CITIESDOMINATED") || name.startsWith("POWERUPS_LINE")) {
				path += "cartas2/cartas2-";
				if (name.startsWith("CITIESDOMINATED")) {
					if (name.endsWith("3")) path += "1.jpg";
					else path += "2.jpg";
				}
				else path += "3.jpg";
			}
			else {
				path += "cartas1/cartas1-";
				if (name.startsWith("YURT")) {
					if (name.endsWith("6")) path += "2.jpg";
					else path += "3.jpg";
				}
				else if (name.startsWith("YURTREGION")) {
					if (name.endsWith("2")) path += "5.jpg";
					else path += "6.jpg";
				}
				else if (name.startsWith("POINTS")) {
					if (name.endsWith("2")) path += "7.jpg";
					else path += "8.jpg";
				}
				else if (name.startsWith("SWORD")) path += "1.jpg";
				else if (name.startsWith("TREASURE")) path += "4.jpg";
				else path += "9.jpg";
			}
			
			this.counselorImages.put(name, this.loadOrCreatePlaceholder(path, this.COUNSELOR_HEIGHT, this.COUNSELOR_WIDTH, new Color(40, 40, 40)));
		}
		
		List<String> inGame = Controller.getController().getGameOpenCounselorsIds();
		for (String id : inGame) {
			Image img = this.counselorImages.get(id);
			
			if (img != null) {
				Image scalled = img.getScaledInstance(COUNSELOR_WIDTH, COUNSELOR_HEIGHT, Image.SCALE_SMOOTH);
				JLabel card = new JLabel(new ImageIcon(scalled));
				card.setHorizontalAlignment(SwingConstants.CENTER);
				
				this.CounselorPanel.add(card);
			}
		}
		
		add(this.CounselorPanel, BorderLayout.CENTER);
		
		revalidate();
		repaint();
		
		setVisible(true);
	}
	
	static BufferedImage readProjectImage(String relativePath) {
        String[] candidatePrefixes = new String[] {
            "",
            "src/",
            "bin/",
            "../",
            "../src/",
            "../../"
        };

        for (String prefix : candidatePrefixes) {
            File candidate = new File(prefix + relativePath);
            if (candidate.exists() && candidate.isFile()) {
                try {
                    return ImageIO.read(candidate);
                } catch (IOException ignored) {
                }
            }
        }

        try (InputStream in = BoardPanel.class.getClassLoader().getResourceAsStream(relativePath)) {
            if (in != null) {
                return ImageIO.read(in);
            }
        } catch (IOException ignored) {
        }

        File targetName = new File(relativePath);
        File found = findFileByName(new File(System.getProperty("user.dir")), targetName.getName(), 3);
        if (found != null) {
            try {
                return ImageIO.read(found);
            } catch (IOException ignored) {
            }
        }
        return null;
    }
	
	private static File findFileByName(File dir, String fileName, int maxDepth) {
        if (dir == null || !dir.isDirectory() || maxDepth < 0) {
            return null;
        }
        File[] children = dir.listFiles();
        if (children == null) {
            return null;
        }
        for (File child : children) {
            if (child.isFile() && child.getName().equalsIgnoreCase(fileName)) {
                return child;
            }
        }
        for (File child : children) {
            if (child.isDirectory() && !child.getName().startsWith(".")) {
                File match = findFileByName(child, fileName, maxDepth - 1);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }
	
	private Image loadOrCreatePlaceholder(String path, int width, int height, Color fallbackColor) {
        BufferedImage loaded = readProjectImage(path);
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
}
