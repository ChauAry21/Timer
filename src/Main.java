import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    private static Timer timer;
    private static String soundFilePath;
    private static TimerTask timerTask;

    public static void main(String[] args) {
        File preset = new File("src/preset_audio/preset.wav");

        JFrame frame = new JFrame("Timer App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1080, 720);

        JPanel panel = new JPanel(new BorderLayout());

        JLabel label = new JLabel("Click the button to start the timer.");
        label.setHorizontalAlignment(JLabel.CENTER);
        panel.add(label, BorderLayout.CENTER);

        JButton startButton = new JButton("Start Timer");
        panel.add(startButton, BorderLayout.SOUTH);

        JButton selectAudioButton = new JButton("Select Audio");
        panel.add(selectAudioButton, BorderLayout.NORTH);

        JButton stopTimerButton = new JButton("Stop Timer");
        panel.add(stopTimerButton, BorderLayout.EAST);

        selectAudioButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("WAV files (*.wav)", "wav"));
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    soundFilePath = selectedFile.getAbsolutePath();
                    JOptionPane.showMessageDialog(frame, "Selected Audio: " + soundFilePath);
                }
            }
        });

        startButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (soundFilePath == null || soundFilePath.isEmpty()) {
                    soundFilePath = preset.getPath();
                    return;
                }

                String timerLengthInput = JOptionPane.showInputDialog(frame, "Enter the timer length (hh:mm:ss):");

                if (timerLengthInput == null) {
                    // User canceled the input
                    return;
                }

                long timerLengthMillis = parseTimerLength(timerLengthInput);

                if (timerLengthMillis <= 0) {
                    JOptionPane.showMessageDialog(frame, "Invalid timer length format. Please enter in hh:mm:ss format.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                startButton.setVisible(false);
                stopTimerButton.setVisible(true);

                if (timerTask != null) {
                    timerTask.cancel();
                }

                final long[] countdown = {timerLengthMillis / 1000};
                label.setText("Time remaining: " + formatTimer(countdown[0]));

                timerTask = new TimerTask() {

                    public void run() {
                        if (countdown[0] > 0) {
                            countdown[0]--;
                            label.setText("Time remaining: " + formatTimer(countdown[0]));
                        } else {
                            playSound(soundFilePath);
                            label.setText("Time's up!");
                            countdown[0] = timerLengthMillis / 1000;
                        }
                    }
                };

                timer = new Timer();
                timer.scheduleAtFixedRate(timerTask, 1000, 1000); // Update every 1 second
            }
        });

        stopTimerButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                startButton.setVisible(true);
                stopTimerButton.setVisible(false);

                if (timerTask != null) {
                    timerTask.cancel();
                    label.setText("Timer stopped.");
                }
            }
        });

        stopTimerButton.setVisible(false);

        frame.add(panel);
        frame.setVisible(true);
    }

    private static long parseTimerLength(String timerLengthInput) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            Date timerLengthDate = sdf.parse(timerLengthInput);
            return timerLengthDate.getTime();
        } catch (ParseException e) {
            return -1;
        }
    }

    private static String formatTimer(long seconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        return sdf.format(new Date(seconds * 1000));
    }

    public static void playSound(String soundFilePath) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(soundFilePath).getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();

            // Close the audioInputStream and release resources
            audioInputStream.close();
        } catch (UnsupportedAudioFileException | LineUnavailableException e) {
            System.err.println("Unsupported audio file format: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
