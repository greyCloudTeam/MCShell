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
			cfg.println(name,1,"�ɹ����ӷ�������");
			is=client.getInputStream();
			di=new DataInputStream(is);
			os=client.getOutputStream();
			dos=new DataOutputStream(os);
			
			cfg.println(name,1,"׼�����ݰ�....");
			ByteArrayOutputStream b ;
			DataOutputStream handshake;
			b= new ByteArrayOutputStream();
			handshake = new DataOutputStream(b);
			handshake.write(0x00);
			cfg.writeVarInt(handshake,version);//�汾��δ֪
			cfg.writeVarInt(handshake,ip.length()); //ip��ַ����
			handshake.writeBytes(ip); //ip
			handshake.writeShort(port); //port
			cfg.writeVarInt(handshake, 2); //state (1 for handshake)
			byte[] login=b.toByteArray();
			
			b= new ByteArrayOutputStream();
			handshake = new DataOutputStream(b);
			handshake.write(0x00);
			cfg.writeVarInt(handshake,cfg.username.length());//�汾��δ֪
			handshake.writeBytes(cfg.username); //ip
			byte[] username=b.toByteArray();
			
			cfg.writeVarInt(dos, login.length); //prepend size
			dos.write(login); //write handshake packet
			dos.flush();
			cfg.println(name,1,"�ѷ��͵�½��....");
			
			cfg.writeVarInt(dos, username.length); //prepend size
			dos.write(username); //write handshake packet
			dos.flush();
			cfg.println(name,1,"�ȴ���Ӧ....");
			
			while(!stop) {
				try {
					int id=0;
					if(compression) {
						int length=cfg.readVarInt(di);
						cfg.println(name,1,"�յ����� "+length+" byte");
						length=cfg.readVarInt(di);
						cfg.println(name,1,"��ѹ�����ݳ���:"+length+"byte");
						if(length==0) {
							id=cfg.readVarInt(di);
							cfg.println(name,1,"�յ����ݰ�,id:"+id);
						}
					}else {
						int length=cfg.readVarInt(di);
						cfg.println(name,1,"�յ����� "+length+" byte");
						id=cfg.readVarInt(di);
						cfg.println(name,1,"�յ����ݰ�,id:"+id);
					}
					if(mode==MODE_LOGIN) {
						if(id==0x00) {
							int motd=cfg.readVarInt(di);
							byte[] temp1=new byte[motd];
							di.readFully(temp1);
							String motdT=new String(temp1);
							throw new RuntimeException("�����������������:\n"+motdT);
						}else if(id==0x03) {
							cfg.println(name,2,"������Ҫ������ѹ��...");
							int value=cfg.readVarInt(di);
							cfg.println(name,2,"ѹ��ǰ������ݰ���С:"+value+"byte");
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
							
							cfg.println(name,1,"��½�ɹ�!\n"+
									"uuid:"+uuid+"\n"+
									"username:"+this.username);
						}else if(id==0x01) {
							cfg.println(name,3,"������������������֤");
							break;
						}
					}else if(mode==MODE_PLAY) {
						if(id==0x23) {//join game
							cfg.println(name, 2,"���ӵ���Ϸ");
							int eid=di.readInt();//ʵ��id
							int gamemode=di.readUnsignedByte();//��Ϸģʽ��0���棬1�����죬2ð�գ�3�Թ�
							int world=di.readInt();//�������-1��գ�0�����磬1����
							int dif=di.readUnsignedByte();//�Ѷȣ�0��ƽ��1�򵥣�2��ͨ��3����
							int max=di.readUnsignedByte();//�����ң��������ͻ���������������б������ڱ�������
							int typeLength=cfg.readVarInt(di);
							byte[] typeByte=new byte[typeLength];
							di.readFully(typeByte);
							String type=new String(typeByte);//������default, flat, largeBiomes, amplified, default_1_1����Ӧ��Ĭ�ϣ�ƽ̹����������Ⱥϵ���ѷŴ�Ĭ��_1_1
							byte RDI=di.readByte();//Reduced Debug Info�ļ�д�����ٵ�����Ϣ����MCShell��˵��û���õģ�0x01��ʾΪtrue��0x00��ʾΪfalse
							cfg.println(name,1,"��������ͼ��Ϣ\n"+
											"ʵ��id:"+eid+"\n"+
											"��Ϸģʽ(gamemode):"+gamemode+"\n"+
											"��ǰλ��:"+world+"\n"+
											"�Ѷ�:"+dif+"\n"+
											"������:"+max+"\n"+
											"��ͼ����:"+type+"\n"+
											"���ٵ�����Ϣ?:"+RDI);
						}
					}
				}catch(EOFException e1) {
					//cfg.println(name,3,"��ȡ��Ϣʱ����:"+e1);
					//break;
				}catch(RuntimeException e2) {
					cfg.println(name,3,"��ȡ��Ϣʱ����:"+e2);
					break;
				}catch(Exception e3) {
					cfg.println(name,3,"��ȡ��Ϣʱ����:"+e3);
				}
			}
		}catch(Exception e){
			cfg.println(name,3,"��½ʱ����:"+e);
		}
	}
}