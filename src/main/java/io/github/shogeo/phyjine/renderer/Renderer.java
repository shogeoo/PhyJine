package io.github.shogeo.phyjine.renderer;

import io.github.shogeo.phyjine.core.PhysicsWorld;

import javax.swing.*;

public class Renderer implements Runnable {

    private final PhysicsWorld world;

    public Renderer(PhysicsWorld world) {
        this.world = world;
    }

    @Override
    public void run() {
        JFrame frame = new JFrame("PhyJine Renderer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Open in full screen

        RenderPanel panel = new RenderPanel(world);
        frame.add(panel);

        frame.setVisible(true);

        new Timer(16, e -> panel.repaint()).start();
    }
}