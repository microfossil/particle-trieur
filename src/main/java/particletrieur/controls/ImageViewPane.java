package particletrieur.controls;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import org.apache.regexp.RE;


/**
 *
 * @author akouznet - https://bugs.openjdk.java.net/browse/JDK-8091216
 */
public class ImageViewPane extends Region {

    private ObjectProperty<ImageView> imageViewProperty = new SimpleObjectProperty<ImageView>();
    public ObjectProperty<ImageView> imageViewProperty() {
        return imageViewProperty;
    }
    public ImageView getImageView() {
        return imageViewProperty.get();
    }
    public void setImageView(ImageView imageView) {
        this.imageViewProperty.set(imageView);
    }

    private BooleanProperty lockScale = new SimpleBooleanProperty(false);
    public boolean isLockScale() {
        return lockScale.get();
    }
    public BooleanProperty lockScaleProperty() {
        return lockScale;
    }
    public void setLockScale(boolean lockScale) {
        this.lockScale.set(lockScale);
    }

    private double currentScale = 1;
    private double mouseX;
    private double mouseY;
    private Rectangle2D viewPort = null;

    public ImageViewPane() {
        this(new ImageView());
    }

    @Override
    protected void layoutChildren() {
        ImageView imageView = imageViewProperty.get();

        if (imageView != null) {
                imageView.setFitWidth(getWidth());
                imageView.setFitHeight(getHeight());
                layoutInArea(imageView, 0, 0, getWidth(), getHeight(), 0, HPos.CENTER, VPos.CENTER);
        }
        super.layoutChildren();
    }

    public ImageViewPane(ImageView imageView) {
        imageViewProperty.addListener(new ChangeListener<ImageView>() {
            @Override
            public void changed(ObservableValue<? extends ImageView> arg0, ImageView oldIV, ImageView newIV) {
                if (oldIV != null) {
                    getChildren().remove(oldIV);
                    viewPort = null;
                }
                if (newIV != null) {
                    getChildren().add(newIV);
                    viewPort = null;
                }
            }
        });
        this.imageViewProperty.set(imageView);
    }

    public void addZoomEvents() {
        ImageView imageView = imageViewProperty.get();
        mouseX = 0;
        mouseY = 0;
        imageView.setOnScroll(event -> {
            this.zoom(event.getX(), event.getY(), -event.getDeltaY());
        });
        imageView.setOnMousePressed(event -> {
            mouseX = event.getX();
            mouseY = event.getY();
        });
        imageView.setOnMouseDragged( event -> {
            this.drag(mouseX - event.getX(), mouseY - event.getY());
            mouseX = event.getX();
            mouseY = event.getY();
        });
        imageView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                this.reset();
            }
        });
    }

    private Rectangle2D getDefaultViewPort() {
        ImageView imageView = imageViewProperty.get();
        imageView.setViewport(null);
        double iw = imageView.getImage().getWidth();
        double ih = imageView.getImage().getHeight();
        Bounds bounds = imageView.getBoundsInLocal();
        double imw = bounds.getWidth();
        double imh = bounds.getHeight();
        double pw = getWidth();
        double ph = getHeight();
        double exw = (pw / imw - 1) / 2;
        double exh = (ph / imh - 1) / 2;
        Rectangle2D rect = new Rectangle2D(-iw * exw, -ih * exh, iw * (1 + 2 * exw), ih * (1 + 2 * exh));
        return rect;
    }

    public void zoom(double x, double y, double delta) {
        double scale = (1.0 + delta / 200);
        zoomScale(x, y, scale);
    }

    private void zoomScale(double x, double y, double scale) {
        currentScale *= scale;
        ImageView imageView = imageViewProperty.get();
        if (viewPort == null) {
            viewPort = getDefaultViewPort();
        }
        Bounds bounds = imageView.getBoundsInLocal();
        double imw = bounds.getWidth();
        double imh = bounds.getHeight();
        double px = x * viewPort.getWidth() / imw + viewPort.getMinX();
        double py = y * viewPort.getHeight() / imh + viewPort.getMinY();
        double minX = (viewPort.getMinX() - px) * scale + px;
        double maxX = (viewPort.getMaxX() - px) * scale + px;
        double minY = (viewPort.getMinY() - py) * scale + py;
        double maxY = (viewPort.getMaxY() - py) * scale + py;

        Rectangle2D newViewPort = new Rectangle2D(minX, minY, maxX - minX, maxY - minY);

        if (newViewPort.getWidth() > 4 * imw) {
            //Zoomed out enough
            return;
        }
        else if (newViewPort.getWidth() < 16) {
            //Zoomed in enough
            return;
        }
        viewPort = newViewPort;
        imageView.setViewport(viewPort);
    }

    public void reset() {
        viewPort = getDefaultViewPort();
        ImageView imageView = imageViewProperty.get();
        imageView.setViewport(viewPort);
    }

    public void drag(double x, double y) {
        if (viewPort == null) {
            viewPort = getDefaultViewPort();
        }
        ImageView imageView = imageViewProperty.get();
        Bounds bounds = imageView.getBoundsInLocal();
        double imw = bounds.getWidth();
        double imh = bounds.getHeight();
        double dx = x * viewPort.getWidth() / imw;
        double dy = y * viewPort.getHeight() / imh;
        viewPort = new Rectangle2D(viewPort.getMinX() + dx, viewPort.getMinY() + dy, viewPort.getWidth(), viewPort.getHeight());
        imageView.setViewport(viewPort);
    }

    public void setImage(Image im) {
        imageViewProperty().get().setImage(im);
        if (!isLockScale()) reset();
    }
}
