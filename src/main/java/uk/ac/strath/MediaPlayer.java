package uk.ac.strath;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

public class MediaPlayer extends JFrame {

    private EmbeddedMediaPlayerComponent mediaPlayerComponent;

    public MediaPlayer(byte[] data) {
        boolean found = new NativeDiscovery().discover();
        System.out.println("VLC Library found: " + found);

        // Create a media player component
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();

        // Set up JFrame
        setTitle("Media Player");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));

        // Add window listener to handle window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mediaPlayerComponent.getMediaPlayer().stop();
                deleteTempFile();
            }
        });

        // Add media player component to JFrame
        getContentPane().add(mediaPlayerComponent, BorderLayout.CENTER);
        pack();

        // Pass byte array into the play video function
        playVideo(data);
    }

    private void playVideo(byte[] data) {
        // Create a temporary file
        downloader(data);

        // Display video using temp file
        String mediaPath = "\\temp.MP4";
        mediaPlayerComponent.getMediaPlayer().playMedia(mediaPath, ":no-video-title-show", ":file-caching=3000", ":live-caching=3000", ":network-caching=3000", ":sout-mux-caching=3000", ":codec=bytearray", ":demux=avformat", ":no-sout-rtp-sap", ":no-sout-standard-sap", ":sout-all", ":ttl=1");
    }

    public void downloader(byte[] data){
        String downloadFolderPath = System.getProperty("user.dir");
        String filePath = downloadFolderPath + File.separator + "temp.MP4";
        try (FileOutputStream stream = new FileOutputStream(filePath)) {
            stream.write(data);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteTempFile() {
        String downloadFolderPath = System.getProperty("user.dir");
        String filePath = downloadFolderPath + File.separator + "temp.MP4";
        File file = new File(filePath);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("Temporary file deleted successfully.");
            } else {
                System.err.println("Failed to delete temporary file.");
            }
        }
    }
}