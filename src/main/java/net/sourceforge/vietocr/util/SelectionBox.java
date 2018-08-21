package net.sourceforge.vietocr.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;

public class SelectionBox {

    private static final int KEYBOARD_MOVEMENT_DELTA = 1;
    final Rectangle selectionBox;
    double mouseAnchorX;
    double mouseAnchorY;
    boolean dragdrawing;
    HashMap<Color, List<java.awt.Rectangle>> map;
    Group group;

    public SelectionBox(Group group) {
        this.group = group;
        selectionBox = createDraggableRectangle();

        selectionBox.setStyle(
                "-fx-stroke: forestgreen; "
                + "-fx-stroke-width: 1px; "
                + "-fx-stroke-dash-array: 12 2 4 2; "
                + "-fx-stroke-dash-offset: 6; "
                + "-fx-stroke-line-cap: butt; "
                + "-fx-fill: rgba(255, 228, 118, .5);"
        );

        group.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
        group.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
        group.addEventHandler(KeyEvent.KEY_PRESSED, onKeyPressedEventHandler);

        group.getChildren().add(selectionBox);
    }

    public Rectangle getRect() {
        return selectionBox;
    }

    /**
     * Deselects selection box.
     */
    public void deselect() {
        selectionBox.setWidth(0);
        selectionBox.setHeight(0);
    }

    /**
     * Gets segmented regions.
     *
     * @return map
     */
    public HashMap<Color, List<java.awt.Rectangle>> getSegmentedRegions() {
        return map;
    }

    /**
     * Sets segmented regions.
     *
     * @param map
     */
    public void setSegmentedRegions(HashMap<Color, List<java.awt.Rectangle>> map) {
        this.map = map;
        
        if (map == null) {
            // remove all SegmentedRegions from group
            group.getChildren().removeIf(n -> n instanceof SegmentedRegion);
            return;
        }

        List<SegmentedRegion> regions = new ArrayList<>();
        for (Color c : map.keySet()) {
            for (java.awt.Rectangle region : map.get(c)) {
                regions.add(new SegmentedRegion(region, c));
            }
        }
        group.getChildren().addAll(regions);
    }

    EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            mouseAnchorX = event.getX();
            mouseAnchorY = event.getY();

            selectionBox.getParent().requestFocus();

            if (selectionBox.getParent().getChildrenUnmodifiable().filtered(grip -> (grip instanceof Rectangle) && grip.contains(mouseAnchorX, mouseAnchorY)).isEmpty()) {
                selectionBox.setX(mouseAnchorX);
                selectionBox.setY(mouseAnchorY);
                selectionBox.setWidth(0);
                selectionBox.setHeight(0);
                dragdrawing = true;
            } else {
                // mouse pressed inside existing box
                dragdrawing = false;
            }

            event.consume();
        }
    };

    EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            if (dragdrawing) {
                double offsetX = event.getX() - mouseAnchorX;
                double offsetY = event.getY() - mouseAnchorY;

                if (offsetX > 0) {
                    selectionBox.setWidth(offsetX);
                } else {
                    selectionBox.setX(event.getX());
                    selectionBox.setWidth(mouseAnchorX - selectionBox.getX());
                }

                if (offsetY > 0) {
                    selectionBox.setHeight(offsetY);
                } else {
                    selectionBox.setY(event.getY());
                    selectionBox.setHeight(mouseAnchorY - selectionBox.getY());
                }
            }
            event.consume();
        }
    };

    EventHandler<KeyEvent> onKeyPressedEventHandler = new EventHandler<KeyEvent>() {

        @Override
        public void handle(KeyEvent event) {
            if (event.isControlDown()) {
                // Moves selection box with arrow keys.
                switch (event.getCode()) {
                    case UP:
                        selectionBox.setY(selectionBox.getY() - KEYBOARD_MOVEMENT_DELTA);
                        break;
                    case RIGHT:
                        selectionBox.setX(selectionBox.getX() + KEYBOARD_MOVEMENT_DELTA);
                        break;
                    case DOWN:
                        selectionBox.setY(selectionBox.getY() + KEYBOARD_MOVEMENT_DELTA);
                        break;
                    case LEFT:
                        selectionBox.setX(selectionBox.getX() - KEYBOARD_MOVEMENT_DELTA);
                        break;
                }
            }
        }
    };

    private Rectangle createDraggableRectangle() {
        final double handleSize = 10;

        Rectangle rect = new Rectangle();
        rect.setCursor(Cursor.MOVE);

        // top left resize handle:
        Rectangle resizeHandleNW = new Anchor(handleSize, handleSize, Color.GOLD);
        resizeHandleNW.setCursor(Cursor.NW_RESIZE);
        // bind to top left corner of Rectangle:
        resizeHandleNW.xProperty().bind(rect.xProperty().add(-handleSize / 2));
        resizeHandleNW.yProperty().bind(rect.yProperty().add(-handleSize / 2));
        resizeHandleNW.visibleProperty().bind(rect.widthProperty().multiply(rect.heightProperty()).isEqualTo(0).not());

        // bottom right resize handle:
        Rectangle resizeHandleSE = new Anchor(handleSize, handleSize, Color.GOLD);
        resizeHandleSE.setCursor(Cursor.SE_RESIZE);
        // bind to bottom right corner of Rectangle:
        resizeHandleSE.xProperty().bind(rect.xProperty().add(rect.widthProperty()).add(-handleSize / 2));
        resizeHandleSE.yProperty().bind(rect.yProperty().add(rect.heightProperty()).add(-handleSize / 2));
        resizeHandleSE.visibleProperty().bind(rect.widthProperty().multiply(rect.heightProperty()).isEqualTo(0).not());
        // West grip
        Rectangle resizeHandleW = new Anchor(handleSize, handleSize, Color.GOLD);
        resizeHandleW.xProperty().bind(rect.xProperty().add(-handleSize / 2));
        resizeHandleW.yProperty().bind(rect.yProperty().add(rect.heightProperty().divide(2)).add(-handleSize / 2));
        resizeHandleW.setCursor(Cursor.W_RESIZE);
        resizeHandleW.visibleProperty().bind(rect.widthProperty().multiply(rect.heightProperty()).isEqualTo(0).not());
        // Esst grip
        Rectangle resizeHandleE = new Anchor(handleSize, handleSize, Color.GOLD);
        resizeHandleE.xProperty().bind(rect.xProperty().add(rect.widthProperty()).add(-handleSize / 2));
        resizeHandleE.yProperty().bind(rect.yProperty().add(rect.heightProperty().divide(2)).add(-handleSize / 2));
        resizeHandleE.setCursor(Cursor.W_RESIZE);
        resizeHandleE.visibleProperty().bind(rect.widthProperty().multiply(rect.heightProperty()).isEqualTo(0).not());
        // SE grip
        Rectangle resizeHandleSW = new Anchor(handleSize, handleSize, Color.GOLD);
        resizeHandleSW.xProperty().bind(rect.xProperty().add(-handleSize / 2));
        resizeHandleSW.yProperty().bind(rect.yProperty().add(rect.heightProperty()).add(-handleSize / 2));
        resizeHandleSW.setCursor(Cursor.SW_RESIZE);
        resizeHandleSW.visibleProperty().bind(rect.widthProperty().multiply(rect.heightProperty()).isEqualTo(0).not());
        // South grip
        Rectangle resizeHandleS = new Anchor(handleSize, handleSize, Color.GOLD);
        resizeHandleS.xProperty().bind(rect.xProperty().add(rect.widthProperty().divide(2)).add(-handleSize / 2));
        resizeHandleS.yProperty().bind(rect.yProperty().add(rect.heightProperty()).add(-handleSize / 2));
        resizeHandleS.setCursor(Cursor.N_RESIZE);
        resizeHandleS.visibleProperty().bind(rect.widthProperty().multiply(rect.heightProperty()).isEqualTo(0).not());
        // North grip
        Rectangle resizeHandleN = new Anchor(handleSize, handleSize, Color.GOLD);
        resizeHandleN.xProperty().bind(rect.xProperty().add(rect.widthProperty().divide(2)).add(-handleSize / 2));
        resizeHandleN.yProperty().bind(rect.yProperty().add(-handleSize / 2));
        resizeHandleN.setCursor(Cursor.N_RESIZE);
        resizeHandleN.visibleProperty().bind(rect.widthProperty().multiply(rect.heightProperty()).isEqualTo(0).not());
        // Northeast grip
        Rectangle resizeHandleNE = new Anchor(handleSize, handleSize, Color.GOLD);
        resizeHandleNE.xProperty().bind(rect.xProperty().add(rect.widthProperty()).add(-handleSize / 2));
        resizeHandleNE.yProperty().bind(rect.yProperty().add(-handleSize / 2));
        resizeHandleNE.setCursor(Cursor.NE_RESIZE);
        resizeHandleNE.visibleProperty().bind(rect.widthProperty().multiply(rect.heightProperty()).isEqualTo(0).not());

        List<Rectangle> anchors = Arrays.asList(resizeHandleNW, resizeHandleSE, resizeHandleW, resizeHandleSW, resizeHandleN, resizeHandleE, resizeHandleNE, resizeHandleS);

        // force resizeHandle to live in same parent as rectangle:
        rect.parentProperty().addListener((obs, oldParent, newParent) -> {
            for (Shape c : anchors) {
                Pane currentParent = (Pane) c.getParent();
                if (currentParent != null) {
                    currentParent.getChildren().remove(c);
                }
                ((Group) newParent).getChildren().add(c);
            }
        });

        Wrapper<Point2D> mouseLocation = new Wrapper<>();
        anchors.forEach(c -> setUpDragging(c, mouseLocation));
        setUpDragging(rect, mouseLocation);

        resizeHandleNW.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double deltaY = event.getSceneY() - mouseLocation.value.getY();
                double newX = rect.getX() + deltaX;
                if (newX >= handleSize
                        && newX <= rect.getX() + rect.getWidth() - handleSize) {
                    rect.setX(newX);
                    rect.setWidth(rect.getWidth() - deltaX);
                }
                double newY = rect.getY() + deltaY;
                if (newY >= handleSize
                        && newY <= rect.getY() + rect.getHeight() - handleSize) {
                    rect.setY(newY);
                    rect.setHeight(rect.getHeight() - deltaY);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });

        resizeHandleNE.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double deltaY = event.getSceneY() - mouseLocation.value.getY();
                double newMaxX = rect.getX() + rect.getWidth() + deltaX;
                if (newMaxX >= rect.getX()
                        && newMaxX <= rect.getParent().getBoundsInLocal().getWidth() - handleSize) {
                    rect.setWidth(rect.getWidth() + deltaX);
                }
                double newY = rect.getY() + deltaY;
                if (newY >= handleSize
                        && newY <= rect.getY() + rect.getHeight() - handleSize) {
                    rect.setY(newY);
                    rect.setHeight(rect.getHeight() - deltaY);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });

        resizeHandleN.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaY = event.getSceneY() - mouseLocation.value.getY();
                double newY = rect.getY() + deltaY;
                if (newY >= handleSize
                        && newY <= rect.getY() + rect.getHeight() - handleSize) {
                    rect.setY(newY);
                    rect.setHeight(rect.getHeight() - deltaY);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });

        resizeHandleSE.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double deltaY = event.getSceneY() - mouseLocation.value.getY();
                double newMaxX = rect.getX() + rect.getWidth() + deltaX;
                if (newMaxX >= rect.getX()
                        && newMaxX <= rect.getParent().getBoundsInLocal().getWidth() - handleSize) {
                    rect.setWidth(rect.getWidth() + deltaX);
                }
                double newMaxY = rect.getY() + rect.getHeight() + deltaY;
                if (newMaxY >= rect.getY()
                        && newMaxY <= rect.getParent().getBoundsInLocal().getHeight() - handleSize) {
                    rect.setHeight(rect.getHeight() + deltaY);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });

        resizeHandleE.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double newMaxX = rect.getX() + rect.getWidth() + deltaX;
                if (newMaxX >= rect.getX()
                        && newMaxX <= rect.getParent().getBoundsInLocal().getWidth() - handleSize) {
                    rect.setWidth(rect.getWidth() + deltaX);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });

        resizeHandleW.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double newX = rect.getX() + deltaX;
                if (newX >= handleSize
                        && newX <= rect.getX() + rect.getWidth() - handleSize) {
                    rect.setX(newX);
                    rect.setWidth(rect.getWidth() - deltaX);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });

        resizeHandleSW.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double deltaY = event.getSceneY() - mouseLocation.value.getY();
                double newX = rect.getX() + deltaX;
                if (newX >= handleSize
                        && newX <= rect.getX() + rect.getWidth() - handleSize) {
                    rect.setX(newX);
                    rect.setWidth(rect.getWidth() - deltaX);
                }
                double newMaxY = rect.getY() + rect.getHeight() + deltaY;
                if (newMaxY >= rect.getY()
                        && newMaxY <= rect.getParent().getBoundsInLocal().getHeight() - handleSize) {
                    rect.setHeight(rect.getHeight() + deltaY);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });

        resizeHandleS.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaY = event.getSceneY() - mouseLocation.value.getY();
                double newMaxY = rect.getY() + rect.getHeight() + deltaY;
                if (newMaxY >= rect.getY()
                        && newMaxY <= rect.getParent().getBoundsInLocal().getHeight() - handleSize) {
                    rect.setHeight(rect.getHeight() + deltaY);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });

        rect.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double deltaY = event.getSceneY() - mouseLocation.value.getY();
                double newX = rect.getX() + deltaX;
                double newMaxX = newX + rect.getWidth();
                if (newX >= handleSize
                        && newMaxX <= rect.getParent().getBoundsInLocal().getWidth() - handleSize) {
                    rect.setX(newX);
                }
                double newY = rect.getY() + deltaY;
                double newMaxY = newY + rect.getHeight();
                if (newY >= handleSize
                        && newMaxY <= rect.getParent().getBoundsInLocal().getHeight() - handleSize) {
                    rect.setY(newY);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });

        return rect;
    }

    private void setUpDragging(Node shape, Wrapper<Point2D> mouseLocation) {
        shape.setOnDragDetected(event -> {
            mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
        });

        shape.setOnMouseReleased(event -> {
            mouseLocation.value = null;
        });
    }

    static class Wrapper<T> {

        T value;
    }

    /**
     * A draggable anchor displayed around a point.
     */
    class Anchor extends Rectangle {

        Anchor(double width, double height, Color color) {
            super(width, height);
            setFill(color.deriveColor(1, 1, 1, 0.5));
            setStroke(color);
            setStrokeWidth(1);
            setStrokeType(StrokeType.OUTSIDE);
        }
    }

    class SegmentedRegion extends Rectangle {

        SegmentedRegion(java.awt.Rectangle rect, Color color) {
            super(rect.x, rect.y, rect.width, rect.height);
            setFill(Color.TRANSPARENT);
            setStroke(color);
            setStrokeWidth(1);
        }
    }
}
