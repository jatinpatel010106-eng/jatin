package com.syllabus.app.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(String resourcePath) {
        setOpaque(false);
        try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
            if (stream != null) {
                backgroundImage = ImageIO.read(stream);
            }
        } catch (IOException ignored) {
            // If image is missing, panel renders plain color fallback.
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(240, 245, 255));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
