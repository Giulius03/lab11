package it.unibo.oop.reactivegui03;

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
 * Third experiment with reactive gui.
 */
@SuppressWarnings("PMD.AvoidPrintStackTrace")
public final class AnotherConcurrentGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private final JLabel display = new JLabel();
    private final JButton btnUp = new JButton("up");
    private final JButton btnDown = new JButton("down");
    private final JButton btnStop = new JButton("stop");

    public AnotherConcurrentGUI() {
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

        final CounterAgent counter = new CounterAgent();
        final TimerAgent timer = new TimerAgent(counter);
        /*
         * Handlers
         */
        this.btnStop.addActionListener(e -> {
            counter.stopCounting();
            this.btnStop.setEnabled(false);
            this.btnUp.setEnabled(false);
            this.btnDown.setEnabled(false);
        });
        this.btnUp.addActionListener(e -> counter.increment());
        this.btnDown.addActionListener(e -> counter.decrement());

        new Thread(counter).start();
        new Thread(timer).start();
    }



    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private class CounterAgent implements Runnable {
        private volatile boolean stop;
        private volatile boolean increasing = true;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    this.counter = increasing ? this.counter+1 : this.counter-1;
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    JOptionPane.showMessageDialog(AnotherConcurrentGUI.this.rootPane, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        /**
         * External command to stop counting.
         */
        public synchronized void stopCounting() {
            this.stop = true;
        }

        public void increment() {
            this.increasing = true;
        }
        
        public void decrement() {
            this.increasing = false;
        }
    }

    private class TimerAgent implements Runnable {
        private static final long MAX_TIME_MILLISECONDS = 10000;
        private CounterAgent counter;

        private TimerAgent(final CounterAgent c) {
            this.counter = c;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(MAX_TIME_MILLISECONDS);
            } catch (InterruptedException e) {
                    JOptionPane.showMessageDialog(AnotherConcurrentGUI.this.rootPane, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            }
            this.counter.stopCounting();
            SwingUtilities.invokeLater(() -> {
                AnotherConcurrentGUI.this.btnUp.setEnabled(false);
                AnotherConcurrentGUI.this.btnDown.setEnabled(false);
                AnotherConcurrentGUI.this.btnStop.setEnabled(false);
            });
        }
    }
}
