package assignment4;

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.utils.picking.PickCanvas;
import org.jogamp.java3d.utils.picking.PickResult;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** 把鼠标点击映射为与 Z / X 键完全一致的逻辑（命中 LeftSwitch / RightSwitch） */
public class CanvasMousePicker {

    public static void install(Canvas3D canvas, BranchGroup sceneRoot, SwitchPanelController controller) {
        PickCanvas picker = new PickCanvas(canvas, sceneRoot);
        picker.setMode(PickCanvas.BOUNDS);      // BOUNDS 足够稳定；也可改成 GEOMETRY
        picker.setTolerance(4.0f);

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    picker.setShapeLocation(e.getX(), e.getY());
                    PickResult pr = picker.pickClosest();
                    if (pr == null) return;

                    Node n = pr.getNode(PickResult.SHAPE3D);
                    if (n instanceof Shape3D s) {
                        String name = s.getName();
                        if (SwitchPanelController.LEFT_NAME.equals(name)) {
                            controller.toggleLeft();
                        } else if (SwitchPanelController.RIGHT_NAME.equals(name)) {
                            controller.toggleRight();
                        }
                    }
                } catch (Exception ex) {
                    // 忽略偶发的拾取异常，避免影响运行
                }
            }
        });
    }
}