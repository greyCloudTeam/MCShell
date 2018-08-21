import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Arrays;
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
					pack ri=new pack(di,compression);
					cfg.println(name,1, "���յ����ݰ�\n����:"+ri.data.length+"\tid:"+ri.id);
					if(mode==MODE_LOGIN) {
						if(ri.id==0x00) {
							throw new RuntimeException("�����������������:\n"+ri.readString());
						}else if(ri.id==0x03) {
							cfg.println(name,2,"������Ҫ������ѹ��...");
							int value=ri.readVarInt();
							cfg.println(name,2,"ѹ��ǰ������ݰ���С:"+value+"byte");
							maxPackSize=value;
							compression=true;
						}else if(ri.id==0x02) {
							uuid=ri.readString();
							this.username=ri.readString();
							mode=MODE_PLAY;
							
							cfg.println(name,1,"��½�ɹ�!\n"+
									"uuid:"+uuid+"\n"+
									"username:"+this.username);
						}else if(ri.id==0x01) {
							cfg.println(name,3,"������������������֤");
							break;
						}
					}else if(mode==MODE_PLAY) {
						if(ri.id==0x23) {//join game
							cfg.println(name, 2,"���ӵ���Ϸ");
							int eid=ri.readInt();//ʵ��id
							int gamemode=ri.readUnsignedByte();//��Ϸģʽ��0���棬1�����죬2ð�գ�3�Թ�
							String modeT="δ֪:"+gamemode;
							if(gamemode==0)
								modeT="����";
							if(gamemode==1)
								modeT="����";
							if(gamemode==2)
								modeT="ð��";
							if(gamemode==3)
								modeT="�Թ�";
							int world=ri.readInt();//�������-1��գ�0�����磬1����
							String worldT="δ֪:"+world;
							if(world==-1)
								worldT="���";
							if(world==0)
								worldT="������";
							if(world==1)
								worldT="����?";
							int dif=ri.readUnsignedByte();//�Ѷȣ�0��ƽ��1�򵥣�2��ͨ��3����
							String difT="δ֪:"+dif;
							if(dif==0)
								difT="��ƽ";
							if(dif==1)
								difT="��";
							if(dif==2)
								difT="��ͨ";
							if(dif==3)
								difT="����";
							int max=ri.readUnsignedByte();//�����ң��������ͻ���������������б������ڱ�������
							String type=ri.readString();//������default, flat, largeBiomes, amplified, default_1_1����Ӧ��Ĭ�ϣ�ƽ̹����������Ⱥϵ���ѷŴ�Ĭ��_1_1
							if(type.equals("default"))
								type="��ͨ,Ĭ��";
							if(type.equals("flat"))
								type="ƽ̹";
							if(type.equals("largeBiomes"))
								type="��������Ⱥϵ";
							if(type.equals("default_1_1"))
								type="Ĭ��_1_1";
							boolean RDI=ri.readBoolean();//Reduced Debug Info�ļ�д�����ٵ�����Ϣ����MCShell��˵��û���õģ�0x01��ʾΪtrue��0x00��ʾΪfalse
							cfg.println(name,1,"��������ͼ��Ϣ\n"+
											"ʵ��id:"+eid+"\n"+
											"��Ϸģʽ(gamemode):"+modeT+"\n"+
											"��ǰλ��:"+worldT+"\n"+
											"�Ѷ�:"+difT+"\n"+
											"������:"+max+"\n"+
											"��ͼ����:"+type+"\n"+
											"���ٵ�����Ϣ?:"+RDI);
						}else if(ri.id==0x18) {
							String name=ri.readString();
							byte[] data=ri.getAllData();
							cfg.println(this.name, 2,"���յ�����˵Ĳ����Ϣ\n"+
										"�������:"+name+"\t����:"+Arrays.toString(data)+"\n"+
										"ת��Ϊ�ı�:"+new String(data));
							
						}else if(ri.id==0x0d) {
							int dif=ri.readUnsignedByte();
							String text="δ֪:"+dif;
							if(dif==0)
								text="��ƽ";
							if(dif==1)
								text="��";
							if(dif==2)
								text="��ͨ";
							if(dif==3)
								text="����";
							cfg.println(this.name,2,"�����Ҫ������Ѷ�:"+text);
						}else if(ri.id==0x2c) {
							byte nl=ri.readByte();
							cfg.println(name, 2, "��ӵ����Щ����:"+nl);
						}
					}
				}catch(EOFException e1) {
					//cfg.println(name,3,"��ȡ��Ϣʱ����:"+e1);
					//break;
				}catch(RuntimeException e2) {
					e2.printStackTrace();
					cfg.println(name,3,"��½ʱ����:"+e2);
					break;
				}catch(Exception e3) {
					cfg.println(name,3,"��ȡ��Ϣʱ����:"+e3);
					e3.printStackTrace();
				}
			}
		}catch(Exception e){
			cfg.println(name,3,"��½ʱ����:"+e);
		}
	}
}