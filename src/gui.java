import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import java.awt.Font;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;


public class gui extends JFrame {

	
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
	public thread t=new thread();
	public boolean stop=false;
	public final int MODE_PLAY=2;
	public final int MODE_LOGIN=1;
	public final int MODE_LEAVE=3;
	public int mode=MODE_LOGIN;
	public String uuid="";
	public String username="";
	public Vector vData = new Vector();
	public Vector vName = new Vector();
	public DefaultTableModel model = new DefaultTableModel(vData, vName);
	//下面是ui
	public JTextPane output = new JTextPane();
	public JPanel contentPane;
	public JLabel lblNewLabel_1 = new JLabel("当前状态:准备就绪");
	public JTable table = new JTable();
	public Document docs = output.getDocument();
	public SimpleAttributeSet attrset = new SimpleAttributeSet();
	private JTable table_1;
	private JTextField input;
	public static gui guiApi=null;
	
	/**
	 * Launch the application.
	 * @throws BadLocationException 
	 */
	public static void main(String[] args) throws BadLocationException {
		//调试
		guiApi=new gui();
		/*
			a.ip="yxnat.softdev.top";
			a.port=12512;
			a.version=404;
			a.login();
		*/
		guiApi.setTitle("MCShell");
		guiApi.setVisible(true);
	}
	public void login() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//gui frame = new gui();
					
					
					t.start();
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
	public gui() throws BadLocationException {
		try {
		      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) { 
			e.printStackTrace();
		}
		setResizable(false);
		setFont(new Font("微软雅黑", Font.PLAIN, 15));
		
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
		output.setFont(new Font("新宋体", Font.PLAIN, 12));
		
		output.setEditable(false);
		//output.addActionListener(c);
		scrollPane.setViewportView(output);
		JButton button = new JButton("执行");
		
		input = new JTextField();
		input.setFont(new Font("新宋体", Font.PLAIN, 14));
		input.setText("[mcs]");
		input.setBounds(10, 343, 727, 23);
		panel.add(input);
		input.setColumns(10);
		
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					
					/**
					 * 命令处理
					 */
					StyleConstants.setFontSize(attrset,12);
					StyleConstants.setForeground(attrset, Color.blue);
					String command_text=input.getText();
		            if(command_text.length()>=5&&command_text.substring(0, 5).equals("[mcs]")) {
		            	docs.insertString(docs.getLength(), "[USER][MCSHELL]"+command_text.substring(5,command_text.length())+"\n", attrset);//对文本进行追加
			            output.setCaretPosition(output.getStyledDocument().getLength());
			            input.setText("[mcs]");
			            String command_this=command_text.substring(5,command_text.length());
			            String[] arr=command_this.split(" ");
			            if(arr[0].equals("help")) {
			            	log("[INFO]帮助信息：\n"+
			            			"\thelp>获取帮助信息\n"+
			            			"\tping +ip +port>获取服务器延迟\n"+
			            			"\tpingList +ip +port>获取服务器的motd信息\n"+
			            			"\tlogin +ip +port +version:服务器的版本号，通过pingList命令获取>登入服务器并创建视图\n"
			            			+ "\tclean [+ViewId:视图id，为空则默认为主视图]>清空一个视图的内容",Color.BLACK);
			            	return;
			            }else if(arr[0].equals("ping")) {
			            	if(arr.length!=3) {
			            		log("[ERROR]传递的参数不正确",Color.RED);
			            		return;
			            	}
			            	button.setEnabled(false);
			            	gui.ping(arr[1],arr[2]);
			            }
			            else {
			            	log("[ERROR]找不到此命令",Color.RED);
			            }
		            }else {
		            	docs.insertString(docs.getLength(), "[USER[SERVER]"+command_text+"\n", attrset);
		            	output.setCaretPosition(output.getStyledDocument().getLength());
			            input.setText("");
		            }
		            
		        } catch (BadLocationException e1) {
		            e1.printStackTrace();
		        }
				
				
				//textArea.paintImmediately(textArea.getBounds());
			}
		});
		
		
		input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent paramKeyEvent) {
                if(paramKeyEvent.getKeyChar()=='\n'){
                    button.doClick();
                }
                if(paramKeyEvent.getKeyCode() == KeyEvent.VK_A && paramKeyEvent.isControlDown()){
                	input.setText("[mcs]");
                }
                if(paramKeyEvent.getKeyCode() == KeyEvent.VK_Z && paramKeyEvent.isControlDown()){
                	input.setText("/");
                }
            }
        });
		
		
		
        
		button.setBounds(747, 343, 83, 25);
		panel.add(button);
		
		JLabel lblNewLabel = new JLabel("在这里输入的所有消息都会直接发送给服务器，以\"/\"开头为服务器指令，以\"[mcs]\"开头为MCShell指令");
		lblNewLabel.setBounds(10, 373, 820, 15);
		panel.add(lblNewLabel);
		
		
		
		
        JPanel panel_2 = new JPanel();
        tabbedPane.addTab("玩家信息",new ImageIcon(), panel_2, "当前玩家的列表，玩家的坐标，延迟等");
        panel_2.setLayout(null);
        
        
        table.setBounds(10, 10, 820, 374);
        panel_2.add(table);
        tabbedPane.setSelectedIndex(0);
		contentPane.add(tabbedPane);
		JPanel panel_1 = new JPanel();
		tabbedPane.addTab("网络",new ImageIcon(), panel_1, "MCShell接收到的数据包");
		panel_1.setLayout(null);
		
		JLabel label = new JLabel("仅显示服务器发送过来的数据包");
		label.setBounds(10, 6, 271, 15);
		panel_1.add(label);
		
		vName.add("id");
		vName.add("大小");
		vName.add("解释");
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 28, 820, 356);
		panel_1.add(scrollPane_1);
		
		table_1 = new JTable();
		table_1.setModel(model);
		scrollPane_1.setViewportView(table_1);
		lblNewLabel_1.setFont(new Font("宋体", Font.PLAIN, 13));
		
		
		lblNewLabel_1.setForeground(Color.RED);
		lblNewLabel_1.setBounds(7, 429, 835, 15);
		contentPane.add(lblNewLabel_1);
		t.c=this;
		
		StyleConstants.setFontSize(attrset,12);
		StyleConstants.setForeground(attrset, Color.black);
        docs.insertString(docs.getLength(), "[INFO]MCShell_gui ui加载完毕\n"
        		+ "\tMCShell版本:gui-beta1.0\n"
        		+ "\t作者:caiwen\n"
        		+ "\tEmail:3102733279@qq.com(出现bug请反馈)\n"
        		+ "[INFO]在鼠标焦点在下面的指令框时，按Enter键可直接执行命令，按Ctrl+A键可将指令框内容更改为“[mcs]”，按Ctrl+Z键可将指令框内容更改为“/”，执行“[mcs]help”获取帮助信息\n", attrset);//对文本进行追加
	}
	public void log(String msg,Color c,boolean br) throws BadLocationException {
		StyleConstants.setForeground(attrset, c);
		if(br) {
			docs.insertString(docs.getLength(), msg+"\n", attrset);//对文本进行追加
			output.setCaretPosition(output.getStyledDocument().getLength());
		}else {
			docs.insertString(docs.getLength(), msg, attrset);//对文本进行追加
			output.setCaretPosition(output.getStyledDocument().getLength());
		}
	}
	public void log(String msg,Color c) throws BadLocationException {
		StyleConstants.setForeground(attrset, c);
		docs.insertString(docs.getLength(), msg+"\n", attrset);//对文本进行追加
		output.setCaretPosition(output.getStyledDocument().getLength());
	}
	public void addPacket(String id,String size) {
		Vector vRow1 = new Vector();
		vRow1.add(id);
		vRow1.add(size);
		vRow1.add("");
		vData.add(vRow1);
		model = new DefaultTableModel(vData, vName);
		table_1.setModel(model);
		if(vData.size()==50) {
			vData.clear();
		}
	}
	public static void ping(String ip,String port) throws BadLocationException {
		Socket s;
		InputStream is=null;
		DataInputStream di=null;
		OutputStream os=null;
		DataOutputStream dos=null;
		try {
			int portT=Integer.parseInt(port);
			s=new Socket(ip,portT);
			is=s.getInputStream();
			di=new DataInputStream(is);
			os=s.getOutputStream();
			dos=new DataOutputStream(os);
			guiApi.log("[PING]ping....",Color.blue);
			
			sendPack ping=new sendPack(dos,0x01);
			ping.thisPack.writeLong(System.currentTimeMillis());
			
			ping.sendPack(false,-1);
			dos.flush();
			
			acceptPack ri=new acceptPack(di,false);
			//println(1, "接收到数据包，长度:"+ri.data.length+"，id:"+ri.id);
			if(ri.id!=0x01) {
				guiApi.log("[PING][ERROR]接收到的数据包不正确！",Color.red);
			}
			guiApi.log("[PING]服务器延迟,"+(System.currentTimeMillis()-ri.readLong())+"ms",Color.blue);
		}catch(RuntimeException e) {
			guiApi.log("[PING][ERROR]未知的错误："+e.getMessage(),Color.red);
		}catch(Exception e) {
			guiApi.log("[PING][ERROR]服务端可能禁止了ping",Color.red);
		}
		
		guiApi.log("完成！",Color.green);
	}
}

class thread extends Thread{
	gui c=null;
	@Override
	public void run() {
		try {
			c.log("[THREAD]线程初始化成功，正在准备建立连接...",Color.darkGray);
			c.client=new Socket(c.ip,c.port);
			//cfg.println(name,1,"成功连接服务器");
			c.is=c.client.getInputStream();
			c.di=new DataInputStream(c.is);
			c.os=c.client.getOutputStream();
			c.dos=new DataOutputStream(c.os);
			
			//cfg.println(name,1,"准备数据包....");
			c.log("[THREAD]线程初始化成功，正在准备建立连接...",Color.darkGray);
			
			sendPack hand=new sendPack(c.dos,0x00);
			hand.writeVarInt(c.version);
			hand.writeString(c.ip);
			hand.thisPack.writeShort(c.port);
			hand.writeVarInt(2);
			
			sendPack username=new sendPack(c.dos,0x00);
			username.writeString(cfg.username);
			
			hand.sendPack(false, -1);
			c.dos.flush();
			//cfg.println(name,1,"已发送登陆包....");
			
			username.sendPack(false, -1);
			c.dos.flush();
			c.log("[THREAD]MCShell->"+c.ip+":"+c.port+"("+(hand.getSize()+username.getSize())+"bytes)",Color.darkGray);
			while(!c.stop) {
					acceptPack ri=new acceptPack(c.di,c.compression);
					c.addPacket(String.valueOf(ri.id), String.valueOf(ri.data.length));
					/*
					if(lv[3])
						cfg.println(name,1, "接收到数据包，长度:"+ri.data.length+",id:"+ri.id);
					*/
					if(c.mode==c.MODE_LOGIN) {
						if(ri.id==0x00) {
							c.log("[SERVER]你不能加入此服务器，原因:\n        "+ri.readString(),Color.red);
							//cfg.println(name,3,"不能连接到这个服务器:\n"+ri.readString());
							break;
						}else if(ri.id==0x02) {
							c.uuid=ri.readString();
							c.username=ri.readString();
							c.mode=c.MODE_PLAY;
							
							c.log("[SERVER-Success]:登陆成功\n"+
									"        uuid:"+c.uuid+"\n"+
									"        username:"+c.username
									,Color.green);
						}else if(ri.id==0x01) {
							c.log("[SERVER]此服务器开启了正版验证，无法登入",Color.red);
							break;
						}else if(ri.id==0x03) {
							c.compression=true;
							c.maxPackSize=ri.readVarInt();
							c.log("[SERVER]服务器启用压缩，大小:"+c.maxPackSize,Color.darkGray);
						}
						
					}else if(c.mode==c.MODE_PLAY) {
						if(ri.id==0x0e) {
							sendPack p=new sendPack(c.dos,0x0e);
							p.thisPack.writeLong(ri.readLong());
							p.sendPack(c.compression, c.maxPackSize);
						}
					}
			}
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			//c.log("[THREAD-ERROR]线程初始化成功，正在准备建立连接...",Color.darkGray);
			e.printStackTrace();
		}catch(Exception e1){
			try {
				c.log("[THREAD-Error]线程错误，无法与服务器通信："+e1.toString(),Color.red);
				e1.printStackTrace();
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}finally {
			try {
				c.dos.close();
				c.di.close();
				c.os.close();
				c.is.close();
				c.client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	
}
