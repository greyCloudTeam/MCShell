import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

//import mdlaf.MaterialLookAndFeel;

//import javafx.application.Application;
//login_gui yxnat.softdev.top 12512 404
public class gui{
	public String ip="";
	public int port=-1;
	public Socket client;
	public InputStream is=null;
	public DataInputStream di=null;
	public OutputStream os=null;
	public DataOutputStream dos=null;
	public int version=-1;
	public boolean compression=false;
	public int maxPackSize=-1;
	public void login() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
	    	public void run() {
	        	createAndShowGUI();
	        }
	    });
	}
	private void createAndShowGUI() {
        // 创建及设置窗口
        JFrame frame = new JFrame("MCShell-"+ip+":"+port);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(900,600));
        frame.setResizable(false);
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("聊天/指令",new ImageIcon(), create_tab1(), "服务器广播的消息，MCShell的日志等");
        tabbedPane.addTab("玩家信息",new ImageIcon(), new JPanel(), "当前玩家的列表，玩家的坐标，延迟等");
        tabbedPane.addTab("玩家信息",new ImageIcon(), new JPanel(), "当前玩家的列表，玩家的坐标，延迟等");
        // 添加 "Hello World" 标签
        tabbedPane.setSelectedIndex(0);

        frame.setContentPane(tabbedPane);
        frame.pack();
        frame.setVisible(true);
        //JLabel label = new JLabel("Hello World");
        //frame.getContentPane().add(label);

        // 显示窗口
        
        //frame.setVisible(true);
    }
	private JComponent create_tab1() {
        // 创建面板, 使用一个 1 行 1 列的网格布局（为了让标签的宽高自动撑满面板）
        JPanel panel = new JPanel(new GridLayout(2, 1));

        // 创建标签
        //JLabel label = new JLabel(text);
        //label.setFont(new Font(null, Font.PLAIN, 50));
        //label.setHorizontalAlignment(SwingConstants.CENTER);
        JTextArea textArea = new JTextArea();
        textArea.setSize(900,550);
        textArea.setEditable(false);
        // 添加标签到面板
        panel.add(textArea);

        return panel;
    }
}
