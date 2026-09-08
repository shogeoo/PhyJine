package io.github.shogeo.phyjine.renderer;

import io.github.shogeo.phyjine.core.PhysicsWorld;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class Renderer {

    private final PhysicsWorld world;

    public Renderer(PhysicsWorld world) {
        this.world = world;
    }

    public RenderPanel start() throws Exception {
        RenderPanel[] created = new RenderPanel[1];
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = new JFrame("PhyJine Renderer");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            RenderPanel panel = new RenderPanel(world);
            created[0] = panel;
            frame.add(panel);
            frame.setVisible(true);
        });

        long deadline = System.currentTimeMillis() + 10_000;
        while (created[0] == null || !created[0].isDisplayable() || created[0].getWidth() == 0) {
            if (System.currentTimeMillis() > deadline) {
                throw new IllegalStateException("Не удалось создать окно рендера");
            }
            Thread.sleep(1);
        }
        return created[0];
    }
}
