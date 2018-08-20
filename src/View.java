import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Iterator;

import javax.crypto.Cipher;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class View extends Thread{
	public String name="Unkown-View";
	public boolean compression=false;
	public int maxPackSize=-1;
	public final int MODE_PLAY=2;
	public final int MODE_LOGIN=1;
	public int mode=MODE_LOGIN;
	public String username="MCShell";
	public String uuid="";
	public String ip="";
	public int port=0;
	public Socket client;
	public InputStream is=null;
	public DataInputStream di=null;
	public OutputStream os=null;
	public DataOutputStream dos=null;
	public int version=-1;
	public boolean stop=false;
	public boolean hide=true;
	public String leaveMsg="";
	public boolean showC=false;
	
	@Override
	public void run() {
		cfg.commandStop=false;
		try {
			client=new Socket(ip,port);
			cfg.println(name,1,"成功连接服务器！");
			is=client.getInputStream();
			di=new DataInputStream(is);
			os=client.getOutputStream();
			dos=new DataOutputStream(os);
			
			cfg.println(name,1,"准备数据包....");
			ByteArrayOutputStream b ;
			DataOutputStream handshake;
			b= new ByteArrayOutputStream();
			handshake = new DataOutputStream(b);
			handshake.write(0x00);
			cfg.writeVarInt(handshake,version);//版本号未知
			cfg.writeVarInt(handshake,ip.length()); //ip地址长度
			handshake.writeBytes(ip); //ip
			handshake.writeShort(port); //port
			cfg.writeVarInt(handshake, 2); //state (1 for handshake)
			byte[] login=b.toByteArray();
			
			b= new ByteArrayOutputStream();
			handshake = new DataOutputStream(b);
			handshake.write(0x00);
			cfg.writeVarInt(handshake,cfg.username.length());//版本号未知
			handshake.writeBytes(cfg.username); //ip
			byte[] username=b.toByteArray();
			
			cfg.writeVarInt(dos, login.length); //prepend size
			dos.write(login); //write handshake packet
			dos.flush();
			cfg.println(name,1,"已发送登陆包....");
			
			cfg.writeVarInt(dos, username.length); //prepend size
			dos.write(username); //write handshake packet
			dos.flush();
			cfg.println(name,1,"等待响应....");
			
			while(!stop) {
				try {
					int id=0;
					if(compression) {
						int length=cfg.readVarInt(di);
						cfg.println(name,1,"收到数据 "+length+" byte");
						length=cfg.readVarInt(di);
						cfg.println(name,1,"解压后数据长度:"+length+"byte");
						if(length==0) {
							id=cfg.readVarInt(di);
							cfg.println(name,1,"收到数据包,id:"+id);
						}
					}else {
						int length=cfg.readVarInt(di);
						cfg.println(name,1,"收到数据 "+length+" byte");
						id=cfg.readVarInt(di);
						cfg.println(name,1,"收到数据包,id:"+id);
					}
					if(mode==MODE_LOGIN) {
						if(id==0x00) {
							int motd=cfg.readVarInt(di);
							byte[] temp1=new byte[motd];
							di.readFully(temp1);
							String motdT=new String(temp1);
							throw new RuntimeException("不能连接这个服务器:\n"+motdT);
						}else if(id==0x03) {
							cfg.println(name,2,"服务器要求启用压缩...");
							int value=cfg.readVarInt(di);
							cfg.println(name,2,"压缩前最大数据包大小:"+value+"byte");
							maxPackSize=value;
							compression=true;
						}else if(id==0x02) {
							
							int uuidLength=cfg.readVarInt(di);
							byte[] uuidByte=new byte[uuidLength];
							di.readFully(uuidByte);
							uuid=new String(uuidByte);
							
							int usernameLength=cfg.readVarInt(di);
							byte[] usernameByte=new byte[usernameLength];
							di.readFully(usernameByte);
							this.username=new String(usernameByte);
							mode=MODE_PLAY;
							
							cfg.println(name,1,"登陆成功!\n"+
									"uuid:"+uuid+"\n"+
									"username:"+this.username);
						}else if(id==0x01) {
							cfg.println(name,3,"服务器开启了正版验证");
							break;
						}
					}else if(mode==MODE_PLAY) {
						if(id==0x23) {//join game
							cfg.println(name, 2,"连接到游戏");
							int eid=di.readInt();//实体id
							int gamemode=di.readUnsignedByte();//游戏模式，0生存，1，创造，2冒险，3旁观
							int world=di.readInt();//不清楚，-1虚空，0主世界，1结束
							int dif=di.readUnsignedByte();//难度，0和平，1简单，2普通，3困难
							int max=di.readUnsignedByte();//最大玩家，曾经被客户端用来绘制玩家列表，但现在被忽略了
							int typeLength=cfg.readVarInt(di);
							byte[] typeByte=new byte[typeLength];
							di.readFully(typeByte);
							String type=new String(typeByte);//可以有default, flat, largeBiomes, amplified, default_1_1，对应：默认，平坦，大型生物群系，已放大，默认_1_1
							byte RDI=di.readByte();//Reduced Debug Info的简写，减少调试信息，对MCShell来说是没有用的，0x01表示为true，0x00表示为false
							cfg.println(name,1,"服务器地图信息\n"+
											"实体id:"+eid+"\n"+
											"游戏模式(gamemode):"+gamemode+"\n"+
											"当前位置:"+world+"\n"+
											"难度:"+dif+"\n"+
											"最大玩家:"+max+"\n"+
											"地图类型:"+type+"\n"+
											"减少调试信息?:"+RDI);
						}
					}
				}catch(EOFException e1) {
					//cfg.println(name,3,"读取消息时出错:"+e1);
					//break;
				}catch(RuntimeException e2) {
					cfg.println(name,3,"读取消息时出错:"+e2);
					break;
				}catch(Exception e3) {
					cfg.println(name,3,"读取消息时出错:"+e3);
				}
			}
		}catch(Exception e){
			cfg.println(name,3,"登陆时出错:"+e);
		}
	}
}