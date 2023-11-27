package org.example;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;
import oshi.SystemInfo;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;

import javax.swing.*;
import java.io.IOException;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.exists;

public class App {
    public static void execute(String fileInput) {
        FFmpeg ffmpeg;
        FFprobe ffprobe;
        try {
            ffmpeg = new FFmpeg("C:/ffmpeg/ffmpeg.exe");
            ffprobe = new FFprobe("C:/ffmpeg/ffprobe.exe");
        } catch (IOException e) {
            System.out.println("Problem retrieving ffmpeg source");
            return;
        }

        Path outputPath = Path.of("C:/fileOutput/");
        if (!exists(outputPath)) {
            try {
                createDirectory(outputPath);
            } catch (IOException e) {
                System.out.println("Failed to create output dir.");
                return;
            }
        }

        FFmpegBuilder builder = new FFmpegBuilder();
        builder
                .setInput(fileInput)
                .overrideOutputFiles(true)
                .addOutput("C:/fileOutput/" + filterName(fileInput))
                .setFormat("mp4")
                .setAudioCodec("aac")
                .setVideoCodec(findGPUEncoder());

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

        FFmpegProbeResult probeResult;
        try {
            probeResult = ffprobe.probe(fileInput);
        } catch (IOException e) {
            System.out.println("Problem probing input file");
            return;
        }
        FFmpegJob job = executor.createJob(builder, new ProgressListener() {
            final double duration_ns = probeResult.getFormat().duration * TimeUnit.SECONDS.toNanos(1);
            @Override
            public void progress(Progress progress) {
                double percentage = progress.out_time_ns / duration_ns;
                System.out.printf("%.0f%n", percentage * 100);
                GUI.componentMap.get(filterName(fileInput)).setText(String.format("%.0f%n%%", percentage *100));
                SwingUtilities.updateComponentTreeUI(GUI.frame);
            }
        });

        new Thread(job).start();
    }

    private static String findGPUEncoder() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        List<GraphicsCard> gpu = hal.getGraphicsCards();
        for (GraphicsCard card : gpu) {
            if (card.getName().contains("AMD")) {
                return "h264_amf";
            } else if (card.getName().contains("Nvidia")) {
                return "h264_nvenc";
            }
        }
        return "h264";
    }

    private static String filterName(String absolutePath) {
        String[] parts = absolutePath.split("\\\\");
        return parts[parts.length-1];
    }
}
