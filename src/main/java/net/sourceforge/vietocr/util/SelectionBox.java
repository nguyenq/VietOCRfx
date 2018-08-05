package net.sourceforge.vietocr.util;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class SelectionBox {

    final DragContext dragContext = new DragContext();
    final Rectangle rect;
    Group group;
    double orgSceneX, orgSceneY;
    double orgTranslateX, orgTranslateY;

    public SelectionBox(Group group) {
        this.group = group;

        rect = new Rectangle(0, 0, 0, 0);
//        rect.setStroke(Color.BLUE);
//        rect.setStrokeWidth(1);
//        rect.setStrokeLineCap(StrokeLineCap.ROUND);
//        rect.setFill(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6));

        rect.setStyle(
                "-fx-stroke: forestgreen; "
                + "-fx-stroke-width: 2px; "
                + "-fx-stroke-dash-array: 12 2 4 2; "
                + "-fx-stroke-dash-offset: 6; "
                + "-fx-stroke-line-cap: butt; "
                + "-fx-fill: rgba(255, 228, 118, .5);"
        );

        rect.setCursor(Cursor.MOVE);
        rect.setOnMousePressed(shapeOnMousePressedEventHandler);
        rect.setOnMouseDragged(shapeOnMouseDraggedEventHandler);

        group.getChildren().add(rect);

        group.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
        group.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
    }
    
    public Rectangle getRect() {
        return rect;
    }

    EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            dragContext.mouseAnchorX = event.getX();
            dragContext.mouseAnchorY = event.getY();

            if (!rect.contains(event.getX(), event.getY())) {
                orgSceneX = 0;
                orgSceneY = 0;
                orgTranslateX = 0;
                orgTranslateY = 0;

                rect.setWidth(0);
                rect.setHeight(0);
                rect.setX(dragContext.mouseAnchorX);
                rect.setY(dragContext.mouseAnchorY);
                rect.setTranslateX(orgTranslateX);
                rect.setTranslateY(orgTranslateY);
            }

            event.consume();
        }
    };

    EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            double offsetX = event.getX() - dragContext.mouseAnchorX;
            double offsetY = event.getY() - dragContext.mouseAnchorY;

            if (offsetX > 0) {
                rect.setWidth(offsetX);
            } else {
                rect.setX(event.getX());
                rect.setWidth(dragContext.mouseAnchorX - rect.getX());
            }

            if (offsetY > 0) {
                rect.setHeight(offsetY);
            } else {
                rect.setY(event.getY());
                rect.setHeight(dragContext.mouseAnchorY - rect.getY());
            }

            event.consume();
        }
    };

    private final class DragContext {

        public double mouseAnchorX;
        public double mouseAnchorY;
    }

    EventHandler<MouseEvent> shapeOnMousePressedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent t) {
            orgSceneX = t.getSceneX();
            orgSceneY = t.getSceneY();
            orgTranslateX = ((Shape) (t.getSource())).getTranslateX();
            orgTranslateY = ((Shape) (t.getSource())).getTranslateY();
            t.consume();
        }
    };

    EventHandler<MouseEvent> shapeOnMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent t) {
            double offsetX = t.getSceneX() - orgSceneX;
            double offsetY = t.getSceneY() - orgSceneY;
            double newTranslateX = orgTranslateX + offsetX;
            double newTranslateY = orgTranslateY + offsetY;

            ((Shape) (t.getSource())).setTranslateX(newTranslateX);
            ((Shape) (t.getSource())).setTranslateY(newTranslateY);
            t.consume();
        }
    };
}
