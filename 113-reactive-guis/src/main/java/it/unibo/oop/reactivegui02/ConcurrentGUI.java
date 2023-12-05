package it.unibo.oop.reactivegui02;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Second example of reactive GUI.
 */
@SuppressWarnings("PMD.AvoidPrintStackTrace")
public final class ConcurrentGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private final JLabel display = new JLabel();
    private final JButton btnUp = new JButton("up");
    private final JButton btnDown = new JButton("down");
    private final JButton btnStop = new JButton("stop");

    public ConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(this.display);
        panel.add(this.btnUp);
        panel.add(this.btnDown);
        panel.add(this.btnStop);
        this.getContentPane().add(panel);
        this.setVisible(true);

        final Agent agent = new Agent();
        /*
         * Handlers
         */
        this.btnStop.addActionListener(e -> {
            agent.stopCounting();
            this.btnStop.setEnabled(false);
            this.btnUp.setEnabled(false);
            this.btnDown.setEnabled(false);
        });
        this.btnUp.addActionListener(e -> agent.increment());
        this.btnDown.addActionListener(e -> agent.decrement());

        new Thread(agent).start();
    }

    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private class Agent implements Runnable {
        private volatile boolean stop;
        private volatile boolean increasing = true;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> ConcurrentGUI.this.display.setText(nextText));
                    this.counter = increasing ? this.counter+1 : this.counter-1;
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    JOptionPane.showMessageDialog(ConcurrentGUI.this.rootPane, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }

        public void increment() {
            this.increasing = true;
        }
        
        public void decrement() {
            this.increasing = false;
        }
    }
}
