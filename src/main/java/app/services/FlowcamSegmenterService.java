package main.java.app.services;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import main.java.app.models.processing.processors.MatUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;

public class FlowcamSegmenterService {

    public Service<Boolean> createService(File inputDirectory) {
        Service<Boolean> service = new Service<Boolean>() {
            @Override
            protected Task<Boolean> createTask() {
                Task<Boolean> task = new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {

                        String outputName = String.format("output_%s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
                        File outputDirectory = Paths.get(inputDirectory.getAbsolutePath(), outputName).toFile();
                        outputDirectory.mkdirs();

                        //Load files
                        Collection<File> fileCollection = FileUtils.listFiles(inputDirectory, new String[]{"bmp", "png", "tiff", "tif", "jpg", "jpeg"}, true);
                        if (fileCollection.size() > 0) {

                            int imageIdx = 0;
                            int subimageIdx = 0;

                            for (File file : fileCollection) {

                                if (this.isCancelled()) break;

                                imageIdx++;
                                subimageIdx = 0;

                                String extension = FilenameUtils.getExtension(file.getName());
                                String filename = FilenameUtils.removeExtension(file.getName());
                                String parent = file.getParent();
                                if (FilenameUtils.getBaseName(parent).startsWith("output")) continue;

                                Mat image = MatUtilities.imread(file.getAbsolutePath());
                                Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
                                Mat mask = new Mat();
                                Imgproc.threshold(image, mask, 1, 255, Imgproc.THRESH_BINARY);
                                ArrayList<MatOfPoint> contours = new ArrayList<>();
                                Imgproc.findContours(mask, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

                                for (MatOfPoint contour : contours) {

                                    subimageIdx++;

                                    updateMessage(String.format("Processing image %d/%d\n- sub image %d",
                                            imageIdx, fileCollection.size(), subimageIdx));
                                    updateProgress(imageIdx, fileCollection.size());

                                    Rect rect = Imgproc.boundingRect(contour);

                                    Mat subimage = (new Mat(image, rect)).clone();
                                    String newFilename = String.format("%s_%05d_%05d.%s",
                                            filename,
                                            imageIdx,
                                            subimageIdx,
                                            extension);

                                    File subimageFile = Paths.get(outputDirectory.getAbsolutePath(), newFilename).toFile();
                                    Imgcodecs.imwrite(subimageFile.getAbsolutePath(), subimage);
                                    subimage.release();
                                }
                                image.release();
                                mask.release();
                            }
                        }

                        return true;
                    }
                };
                return task;
            }
        };
        return service;
    }

    public class InputFileFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            if (pathname.isDirectory() && FilenameUtils.getBaseName(pathname.getAbsolutePath()).startsWith("output")) return false;
            else return true;
        }
    }
}
