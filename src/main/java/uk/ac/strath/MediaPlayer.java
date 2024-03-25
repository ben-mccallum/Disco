package uk.ac.strath;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import javax.swing.*;
import java.awt.*;
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
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));

        // Add media player component to JFrame
        getContentPane().add(mediaPlayerComponent, BorderLayout.CENTER);
        pack();

        // Play video from byte array
        playVideo(data);
    }

    private void playVideo(byte[] data) {
        // Create a ByteArrayInputStream from the byte array
        mynuts(data);

        // Create a media file from the ByteArrayInputStream
        String mediaPath = "\\temp.MP4";
        mediaPlayerComponent.getMediaPlayer().playMedia(mediaPath, ":no-video-title-show", ":file-caching=3000", ":live-caching=3000", ":network-caching=3000", ":sout-mux-caching=3000", ":codec=bytearray", ":demux=avformat", ":no-sout-rtp-sap", ":no-sout-standard-sap", ":sout-all", ":ttl=1");
    }

    public void mynuts(byte[] data){
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

}
