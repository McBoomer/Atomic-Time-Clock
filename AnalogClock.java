/*
 * Muadh Khan
 * June 28th, 2025
 * This is a clock application that displays the current time in an analog format.
 * It features smooth movement of the hour and minute hands, a ticking second hand,
 * and a dark mode toggle. The clock updates every second and can open a webpage
 * to check atomic time.
 */

package Clock;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalTime;
import java.time.ZoneId;

import javax.swing.*;

public class AnalogClock {

    static JFrame frame = new JFrame();
    static int hour, min, second, twelveHour, convert;
    static LocalTime currentTime;
    
    static int shadowWidth = 500;
    static int shadowHeight = 500;
    static int posX = shadowWidth / 2;
    static int posY = shadowHeight / 2;
    static int colorMode = 0;

    static String AMPM;

    static Color face = new Color(0, 0, 0);
    static Color fillColor = new Color(75, 83, 94);
    static Color outLineColor = new Color(26, 30, 36);
    static Color white = new Color(255, 255, 255);

    static Font basicFont = new Font("Futura", Font.PLAIN, 12);
    

    public static void main(String[] args) {
        loadClock();
    }

    public static void loadClock() {

        frame.setSize(600, 600);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setLayout(null);
        frame.setAlwaysOnTop(true);

        ImageIcon arrowIcon = null;

        java.net.URL imgURL = AnalogClock.class.getResource("ClockIcon.png");
        if (imgURL != null) {
            arrowIcon = new ImageIcon(imgURL);
            frame.setIconImage(arrowIcon.getImage());
        } else {
            JOptionPane.showMessageDialog(frame, "Icon image not found.");
        }
        // frame.setUndecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            currentTime = LocalTime.now();
            int centerX = frame.getContentPane().getWidth() / 2;
            int centerY = frame.getContentPane().getHeight() / 2;

            int hour = currentTime.getHour();
            int min = currentTime.getMinute();
            int sec = currentTime.getSecond();
            int nano = currentTime.getNano();

            // ⏱ Smooth hour & minute hand movement
            double fractionalMinute = min + sec / 60.0 + nano / 60_000_000_000.0;
            double fractionalHour = (hour % 12) + fractionalMinute / 60.0;

            // 🕓 Final angles
            double secondAngle = Math.toRadians((sec % 60) * 6);                // Ticking (no interpolation)
            double minuteAngle = Math.toRadians(fractionalMinute * 6);          // Smooth
            double hourAngle   = Math.toRadians(fractionalHour * 30);  

            int hourLength = 140;
            int endXh = centerX + (int) (Math.sin(hourAngle) * hourLength);
            int endYh = centerY - (int) (Math.cos(hourAngle) * hourLength);

            int minuteLength = 200;
            int endXm = centerX + (int) (Math.sin(minuteAngle) * minuteLength);
            int endYm = centerY - (int) (Math.cos(minuteAngle) * minuteLength);

            int secondsLength = 200;
            int endX = centerX + (int) (Math.sin(secondAngle) * secondsLength);
            int endY = centerY - (int) (Math.cos(secondAngle) * secondsLength);

            // Draw the second hand
            g2.setColor(white);
            g2.drawLine(centerX, centerY, endX, endY);

            float hourThickness = 6.0f;
            BasicStroke hourStroke = new BasicStroke(hourThickness);
            float countThickness = 1.0f;
            BasicStroke countStroke = new BasicStroke(countThickness);
            float minThickness = 3.0f;
            BasicStroke minStroke = new BasicStroke(minThickness);

            g2.setFont(basicFont);

            // Hour Hand
            g2.setColor(white);
            g2.setStroke(hourStroke);
            g2.drawLine(centerX, centerY, endXh, endYh);

            if (hour > 12) {
                convert = hour - 12;
                AMPM = "PM";
            } else {
                AMPM = "AM";
                convert = hour;
            }

            String time = String.format("%d:%02d:%02d", convert, min, second);
            frame.setTitle(time + " " + AMPM + "   |   Dark Mode [Q]   |   Check Atmoic Time [T]");
            // Get string width using FontMetrics
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(time);

            // Center horizontally within the panel
            int x = (frame.getContentPane().getWidth() - textWidth) / 2;
            int y = frame.getContentPane().getHeight() - 130;

            g2.setColor(white);
            g2.drawString("QUARTZ", frame.getContentPane().getWidth() / 2 - 24, frame.getContentPane().getHeight() - 150);
            g2.drawString(time, x, y);

            // Min Hand
            g2.setColor(white);
            g2.setStroke(minStroke);
            g2.drawLine(centerX, centerY, endXm, endYm);
            
            // Second Hand
            g2.setColor(white);
            g2.fillOval(centerX - 7, centerY - 7, 14, 14);
            g2.setColor(face);
            g2.fillOval(centerX - 4, centerY - 4, 8, 8);

            g2.setColor(white);
            g2.setStroke(countStroke);

            for (int i = 0; i < 12; i++) {
                double angle = Math.toRadians(i * 30); // 360° / 12 = 30° per tick

                int innerRadius = 210;  // where tick starts (closer to center)
                int outerRadius = 230;  // where tick ends (toward edge)

                int x1 = centerX + (int) (Math.sin(angle) * innerRadius);
                int y1 = centerY - (int) (Math.cos(angle) * innerRadius);
                int x2 = centerX + (int) (Math.sin(angle) * outerRadius);
                int y2 = centerY - (int) (Math.cos(angle) * outerRadius);

                g2.drawLine(x1, y1, x2, y2);
            }

            hour = currentTime.getHour();
            min = currentTime.getMinute();
            second = currentTime.getSecond();

            // Mask around oval
            g2.setColor(outLineColor);
            Area full = new Area(new Rectangle(0, 0, frame.getContentPane().getWidth(), frame.getContentPane().getHeight()));
            Ellipse2D oval = new Ellipse2D.Double(centerX - posX, centerY - posY, shadowWidth, shadowHeight);
            full.subtract(new Area(oval));
            g2.fill(full);
            g2.setColor(fillColor);
            g2.setStroke(minStroke);
            g2.drawOval(centerX - posX, centerY - posY, shadowWidth, shadowHeight);
            
            g2.dispose();
        }};
        

        Timer timer = new Timer(100, e -> {
            //seconds++;
            panel.repaint();
        });

        panel.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_Q) {
                    
                    colorMode = (colorMode == 0) ? 1 : 0;
                    setColorMode(colorMode);
                    if (colorMode == 0) {
                        panel.setBackground(Color.BLACK);
                    } else {
                        panel.setBackground(Color.WHITE);
                    }
                    panel.repaint();
                } else if (e.getKeyCode() == KeyEvent.VK_T) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://clock.zone/"));
                    } catch (IOException | URISyntaxException e1) {
                        e1.printStackTrace();
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_R) {
                    ZoneId laZone = ZoneId.of(("America/Los_Angeles"));
                    currentTime = LocalTime.now(laZone);
                    panel.repaint();
                }
            }

            public void keyReleased(KeyEvent e) {
            }
            
        });
        
        timer.setInitialDelay(1000);
        timer.start();

        panel.setBackground(face);
        panel.setBounds(0, 0, 600, 600);

        panel.setFocusable(true);
        panel.requestFocusInWindow();

        frame.add(panel);
        frame.setVisible(true);

    }

    public static void setColorMode(int mode) {
        switch (mode) {
            case 0: // Original colors
                face = new Color(0, 0, 0);
                fillColor = new Color(75, 83, 94);
                outLineColor = new Color(26, 30, 36);
                white = new Color(255, 255, 255);
                break;
            case 1: // Inverted colors
                face = new Color(255, 255, 255);
                fillColor = new Color(180, 172, 161);
                outLineColor = new Color(229, 225, 219);
                white = new Color(0, 0, 0);
                break;
            default:
                System.out.println("Invalid mode");
                break;
        }
    }
    
}
