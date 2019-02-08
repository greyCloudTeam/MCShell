import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import java.awt.Font;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.JButton;
import javax.swing.JComponent;

import java.awt.Panel;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JTable;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;

public class ggui extends JFrame {

	private JPanel contentPane;
	private JTable table;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ggui frame = new ggui();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @throws BadLocationException 
	 */
	public ggui() throws BadLocationException {
		setResizable(false);
		setFont(new Font("微软雅黑", Font.PLAIN, 15));
		setTitle("MCShell");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 851, 477);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(0, 0, 845, 423);
		JPanel panel = new JPanel();
		tabbedPane.addTab("聊天/指令",new ImageIcon(), panel, "服务器广播的消息，MCShell的日志等");
		panel.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(10, 10, 820, 327);
		panel.add(scrollPane);
		
		JTextPane textPane = new JTextPane();
		textPane.setEditable(false);
		Document docs = textPane.getDocument();
		SimpleAttributeSet attrset = new SimpleAttributeSet();
		scrollPane.setViewportView(textPane);
		JTextPane txtpnmcs = new JTextPane();
		JButton button = new JButton("执行");
		
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					StyleConstants.setFontSize(attrset,12);
					StyleConstants.setForeground(attrset, Color.blue);
		            docs.insertString(docs.getLength(), "[USER]"+txtpnmcs.getText()+"\n", attrset);//对文本进行追加
		        } catch (BadLocationException e1) {
		            e1.printStackTrace();
		        }
				
				//textArea.paintImmediately(textArea.getBounds());
			}
		});
		
		
		StyleConstants.setFontSize(attrset,12);
		StyleConstants.setForeground(attrset, Color.black);
        docs.insertString(docs.getLength(), "[INFO]MCShell_gui ui加载完毕，正在准备登陆\n", attrset);//对文本进行追加
        
		button.setBounds(747, 347, 83, 21);
		panel.add(button);
		
		
		txtpnmcs.setText("[mcs]");
		txtpnmcs.setForeground(Color.GREEN);
		txtpnmcs.setBackground(Color.BLACK);
		txtpnmcs.setBounds(10, 347, 727, 21);
		panel.add(txtpnmcs);
		
		JLabel lblNewLabel = new JLabel("在这里输入的所有消息都会直接发送给服务器，以\"/\"开头为服务器指令，以\"[mcs]\"开头为MCShell指令");
		lblNewLabel.setBounds(10, 373, 820, 15);
		panel.add(lblNewLabel);
        JPanel panel_2 = new JPanel();
        tabbedPane.addTab("玩家信息",new ImageIcon(), panel_2, "当前玩家的列表，玩家的坐标，延迟等");
        panel_2.setLayout(null);
        
        table = new JTable();
        table.setBounds(10, 10, 820, 374);
        panel_2.add(table);
        tabbedPane.setSelectedIndex(0);
		contentPane.add(tabbedPane);
		JPanel panel_1 = new JPanel();
		tabbedPane.addTab("玩家信息",new ImageIcon(), panel_1, "当前玩家的列表，玩家的坐标，延迟等");
		panel_1.setLayout(null);
		
		JLabel lblNewLabel_1 = new JLabel("当前状态:已连接到服务器        服务器延迟:0ms        在线玩家:0");
		lblNewLabel_1.setForeground(Color.RED);
		lblNewLabel_1.setBounds(10, 428, 835, 15);
		contentPane.add(lblNewLabel_1);
	}
}
