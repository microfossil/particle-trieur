package particletrieur.controls;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

public class ImageCell extends Region {
    private final ImageView imageView;

    public ImageCell(Image image) {
        imageView = new ImageView(image);
        getChildren().add(imageView);
    }

    public ImageCell() {
        this(null);
    }

    public final void setImage(Image value) {
        imageView.setImage(value);
    }

    public final Image getImage() {
        return imageView.getImage();
    }

    public final ObjectProperty<Image> imageProperty() {
        return imageView.imageProperty();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        Insets insets = getInsets();
        double x = insets.getLeft();
        double y = insets.getTop();
        double width = getWidth() - x - insets.getRight();
        double height = getHeight() - y - insets.getBottom();

        Image image = getImage();
        double imageWidth = 0;
        double imageHeight = 0;
        if (image != null) {
            imageWidth = image.getWidth();
            imageHeight = image.getHeight();
        }

        // scale ImageView to available size
        double factor = Math.min(width / imageWidth, height / imageHeight);
        if (Double.isFinite(factor) && factor > 0) {
            imageView.setFitHeight(factor * imageHeight);
            imageView.setFitWidth(factor * imageWidth);
            imageView.setVisible(true);
        } else {
            imageView.setVisible(false);
        }

        // center ImageView in available area
        layoutInArea(imageView, x, y, width, height, 0, HPos.CENTER, VPos.CENTER);
    }

}
