package lab2;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CodeLab2CircleLJ {

    // 是否显示小圆（菜单 Circle 控制）
    public static boolean SHOW_SMALL_CIRCLE = false; // 程序启动只显示大圆

    // 是否暂停旋转（菜单 Pause/Resume 控制）
    public static boolean DEFAULT_PAUSED = false;

    private JFrame frame;
    private GroupObjectsCircle viewPanel;

    public CodeLab2CircleLJ() {
        frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 3D 视图面板
        viewPanel = new GroupObjectsCircle();
        frame.add(viewPanel, BorderLayout.CENTER);

        // 初始标题
        updateTitle(frame, viewPanel.getCircleCount());

        // ===== 单一顶层菜单 =====
        MenuBar menuBar = new MenuBar();
        Menu menuMain = new Menu("Menu");

        // Exit
        MenuItem miExit = new MenuItem("Exit");
        miExit.addActionListener(e -> System.exit(0));
        menuMain.add(miExit);

        // Pause/Resume
        MenuItem miPauseResume = new MenuItem("Pause/Resume");
        miPauseResume.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean paused = !DEFAULT_PAUSED;
                DEFAULT_PAUSED = paused;
                viewPanel.setPaused(paused);
            }
        });
        menuMain.add(miPauseResume);

        // Shape（切换 1 圆 / 2 圆）
        MenuItem miShapeToggle = new MenuItem("Shape");
        miShapeToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean newShowSmall = !SHOW_SMALL_CIRCLE;
                SHOW_SMALL_CIRCLE = newShowSmall;
                viewPanel.setShowSmallCircle(newShowSmall);
                updateTitle(frame, viewPanel.getCircleCount());
            }
        });
        menuMain.add(miShapeToggle);

        // 组装菜单栏
        menuBar.add(menuMain);
        frame.setMenuBar(menuBar);

        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * 根据圆数量更新窗口标题。
     */
    public static void updateTitle(JFrame frame, int circleCount) {
        String title = (circleCount == 1) ? "1 Circle" : "2 Circles";
        frame.setTitle(title);
    }

    public static void main(String[] args) {
        new CodeLab2CircleLJ();
    }
}
