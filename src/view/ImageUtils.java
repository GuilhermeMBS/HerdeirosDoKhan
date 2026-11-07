package view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

class ImageUtils {
	private ImageUtils() {}
	
	public static BufferedImage readProjectImage(String relativePath) {
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

    public static File findFileByName(File dir, String fileName, int maxDepth) {
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
}
