package io.github.shogeo.phyjine.renderer;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

class Renderer {
    final Canvas canvas;
    final int width;
    final int height;
    private final Frame frame;
    private final BufferStrategy bufferStrategy;
    private final BufferedImage screenImage;
    private final int[] pixels;

    Renderer(int width, int height) {
        Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

        this.width = Math.min(width, (int) maxBounds.getWidth());
        this.height = Math.min(height, (int) maxBounds.getHeight());

        this.frame = new Frame("PhyJine");
        this.canvas = new Canvas();
        this.canvas.setSize(this.width, this.height);
        this.canvas.setBackground(Color.BLACK);

        this.frame.add(canvas);
        this.frame.pack();
        this.frame.setLocationRelativeTo(null);
        this.frame.setResizable(false);

        this.canvas.createBufferStrategy(2);
        this.bufferStrategy = canvas.getBufferStrategy();

        this.screenImage = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
        this.pixels = ((DataBufferInt) screenImage.getRaster().getDataBuffer()).getData();

        this.frame.setVisible(true);

        this.frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                frame.dispose();
            }
        });
    }

    void set(int x, int y, int color) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            pixels[y * width + x] = color;
        }
    }

    void update() {
        Graphics g = bufferStrategy.getDrawGraphics();
        g.drawImage(screenImage, 0, 0, null);
        g.dispose();
        bufferStrategy.show();
    }

    void clear() {
        Arrays.fill(pixels, 0x000000);
    }

    void circle(int x, int y, int radius, int color) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (((i - x) * (i - x) + (j - y) * (j - y)) <= radius * radius) {
                    set(i, j, color);
                }
            }
        }
    }
}