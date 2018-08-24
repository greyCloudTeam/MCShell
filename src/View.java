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
	public int gamemode=0;
	
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
			
			sendPack hand=new sendPack(dos,0x00);
			hand.writeVarInt(version);
			hand.writeString(ip);
			hand.thisPack.writeShort(port);
			hand.writeVarInt(2);
			
			sendPack username=new sendPack(dos,0x00);
			username.writeString(cfg.username);
			
			hand.sendPack(false, -1);
			dos.flush();
			cfg.println(name,1,"�ѷ��͵�½��....");
			
			username.sendPack(false, -1);
			dos.flush();
			cfg.println(name,1,"�ȴ���Ӧ....");
			while(!stop) {
				try {
					acceptPack ri=new acceptPack(di,compression);
					cfg.println(name,1, "���յ����ݰ�\n����:"+ri.data.length+"\tid:"+ri.id);
					if(mode==MODE_LOGIN) {
						if(ri.id==0x00) {
							throw new RuntimeException("�����������������:\n"+ri.readString());
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
						if(ri.id==0x00) {
							int id=ri.readInt();
							cfg.println(name, 1,"�����Ҫ�󱣳�����,���id:"+id);
							sendPack p=new sendPack(dos,0x00);
							p.thisPack.writeInt(id);
							p.sendPack(false, -1);
						}else if(ri.id==0x01) {//join game
							cfg.println(name, 2,"���ӵ���Ϸ");
							int eid=ri.readInt();//ʵ��id
							int gamemode=ri.readUnsignedByte();//��Ϸģʽ��0���棬1�����죬2ð�գ�3�Թ�
							this.gamemode=gamemode;
							String modeT="δ֪:"+gamemode;
							if(gamemode==0)
								modeT="����";
							if(gamemode==1)
								modeT="����";
							if(gamemode==2)
								modeT="ð��";
							if(gamemode==3)
								modeT="�Թ�";
							byte world=ri.readByte();//�������-1��գ�0�����磬1����
							String worldT="δ֪:"+world;
							if(world==-1)
								worldT="���";
							if(world==0)
								worldT="������";
							if(world==1)
								worldT="�½�";
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
							//boolean RDI=ri.readBoolean();//Reduced Debug Info�ļ�д�����ٵ�����Ϣ����MCShell��˵��û���õģ�0x01��ʾΪtrue��0x00��ʾΪfalse
							cfg.println(name,1,"��������ͼ��Ϣ\n"+
											"ʵ��id:"+eid+"\n"+
											"��Ϸģʽ(gamemode):"+modeT+"\n"+
											"��ǰλ��:"+worldT+"\n"+
											"�Ѷ�:"+difT+"\n"+
											"������:"+max+"\n"+
											"��ͼ����:"+type);
											//"���ٵ�����Ϣ?:"+RDI);
						}else if(ri.id==0x02) {
							String msg=ri.readString();
							cfg.println(name, 1,"������Ϣ:\n"+msg);
						}else if(ri.id==0x03) {
							cfg.println(name,1,"������ʱ�����:\n"+
									"��ͼʱ��:"+ri.readLong()+"\t�����ʱ��:"+ri.readLong());
						}else if(ri.id==0x04) {
							int eid=ri.readInt();
							short slot=ri.readShort();
							String slotS="δ֪";
							if(slot==0)
								slotS="����";
							if(slot==1)
								slotS="ѥ��";
							if(slot==2)
								slotS="����";
							if(slot==3)
								slotS="����";
							if(slot==4)
								slotS="ͷ��";
							short id=ri.readShort();
							if(id!=-1) {
								byte num=ri.readByte();
								short s=ri.readShort();
								cfg.println(name, 1,"ʵ��װ������:\n"+
											"ʵ��id:"+eid+"\t���:"+slot+"\t��id:"+id+"\t��Ʒ����:"+num+"\t�𻵳̶�:"+s);
								continue;
							}
							cfg.println(name, 1,"ʵ��װ������:\n"+
									"ʵ��id:"+eid+"\t���:"+slot+"\t��id:��");
						}else if(ri.id==0x05) {
							cfg.println(name, 2,"λ�ø���!�㵱ǰ��λ��:\n"+
										"x:"+ri.readInt()+"\ty:"+ri.readInt()+"\tz:"+ri.readInt());
						}else if(ri.id==0x06){
							float heal=ri.readFloat();
							short food=ri.readShort();
							float bh=ri.readFloat();
							if(heal<=0){
								cfg.println(name, 2,"���Ѿ�������");
								continue;
							}
							cfg.println(name, 2,"���Ѫ��/����ֵ����!\n"+
										"Ѫ��:"+heal+"\t����ֵ:"+food+"\t����ֵ���Ͷ�:"+bh);
						}else if(ri.id==0x07) {
							int world=ri.readInt();//�������-1��գ�0�����磬1����
							String worldT="δ֪:"+world;
							if(world==-1)
								worldT="���";
							if(world==0)
								worldT="������";
							if(world==1)
								worldT="�½�";
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
							
							int gamemode=ri.readUnsignedByte();//��Ϸģʽ��0���棬1�����죬2ð�գ�3�Թ�
							this.gamemode=gamemode;
							String modeT="δ֪:"+gamemode;
							if(gamemode==0)
								modeT="����";
							if(gamemode==1)
								modeT="����";
							if(gamemode==2)
								modeT="ð��";
							if(gamemode==3)
								modeT="�Թ�";
							
							String type=ri.readString();//������default, flat, largeBiomes, amplified, default_1_1����Ӧ��Ĭ�ϣ�ƽ̹����������Ⱥϵ���ѷŴ�Ĭ��_1_1
							if(type.equals("default"))
								type="��ͨ,Ĭ��";
							if(type.equals("flat"))
								type="ƽ̹";
							if(type.equals("largeBiomes"))
								type="��������Ⱥϵ";
							if(type.equals("default_1_1"))
								type="Ĭ��_1_1";
							
							cfg.println(name,2,"�����Ҫ�����ά��:\n"+
										"ά��:"+worldT+"\t�Ѷ�:"+difT+"\t��Ϸģʽ:"+modeT+"\t��ͼ����:"+type);
						}else if(ri.id==0x08) { 
							double x=ri.readDouble();
							double y=ri.readDouble();
							double z=ri.readDouble();
							float ph=ri.readFloat();
							float p=ri.readFloat();
							boolean d=ri.readBoolean();
							cfg.println(name, 2,"���λ�ñ�����:\n"+
												"X:"+x+"\tY:"+y+"\tZ:"+z+"\tƫ��:"+ph+"\t����:"+p+"\t�ڵ���:"+d);
						}else if(ri.id==0x09) {
							byte i=ri.readByte();
							cfg.println(name,2,"��ѡ�������Ʒ���е���Ʒ����������:"+i);
						}else if(ri.id==0x0A) {
							int eid=ri.readInt();
							int x=ri.readInt();
							int y=ri.readUnsignedByte();
							int z=ri.readInt();
							cfg.println(name,1,"����һ����Լ�������˯����:\n"+
												"eid:"+eid+"\tX:"+x+"\tY:"+y+"\tZ:"+z);
						}else if(ri.id==0x0B) {
							int eid=ri.readVarInt();
							int id=ri.readUnsignedByte();
							String idT="δ֪";
							if(id==0)
								idT="�ڱ�";
							if(id==1)
								idT="����";
							if(id==2)
								idT="�뿪��";
							if(id==3)
								idT="�Զ���";
							if(id==4)
								idT="���ϵ�Ѫ";
							if(id==5)
								idT="����ҩˮ";
							if(id==104)
								idT="�׷�";
							if(id==105)
								idT="����";
							cfg.println(name, 1,"ʵ�嶯����Ҫ����:\n"+
												"ʵ��id:"+eid+"\t����:"+idT);
						}else if(ri.id==0x0C) {
							int eid=ri.readVarInt();
							String uuid=ri.readString();
							String name=ri.readString();
							int x=ri.readInt();
							int y=ri.readInt();
							int z=ri.readInt();
							byte ph=ri.readByte();
							byte p=ri.readByte();
							short item=ri.readShort();
							cfg.println(name, 1,"��ҽ��뵱ǰ�ɼ���Χ:\n"+
												"ʵ��id:"+eid+"\t���uuid:"+uuid+"\t�������:"+name+"\n"+
												"X:"+x+"\tY:"+y+"\tZ:"+z+"\tƫ��:"+ph+"\t����:"+p+"\t���ϵ���Ʒ:"+item);
						}else if(ri.id==0x0D) {
							int eid=ri.readInt();
							int item=ri.readInt();
							cfg.println(name, 1,"����ʰȡ��Ʒ��ʵ��id:"+eid+",��Ʒʵ��id:"+item);
						}else if(ri.id==0x11) {
							cfg.println(name,1,"��������������\n"+
												"ʵ��id:"+ri.readVarInt()+"\t����:\n"+
												"x:"+ri.readInt()+"\ty:"+ri.readInt()+"\tz:"+ri.readInt()+"\n"+
												"����:"+ri.readShort());
						}else if(ri.id==0x12) {
							cfg.println(name,1,"���������ʵ���ٶ�:\n"+
												"ʵ��id:"+ri.readInt()+"\tX�ٶ�:"+ri.readShort()+"\tY�ٶ�:"+ri.readShort()+"\tZ�ٶ�:"+ri.readShort());
						}else if(ri.id==0x13) {
							cfg.println(name, 1,"ʵ�屻�ݻ٣�����"+ri.readByte());
						}else if(ri.id==0x15) {
							cfg.println(name, 1,"ʵ������ƶ���ʵ��id:"+ri.readInt()+"������:\n"+
												"X:"+ri.readByte()+"\tY:"+ri.readByte()+"\tZ"+ri.readByte());
						}else if(ri.id==0x16) {
							cfg.println(name,1,"ʵ����ת��ʵ��id:"+ri.readInt()+",X��ת:"+ri.readByte()+",Y��ת:"+ri.readByte());
						}else if(ri.id==0x17) {
							cfg.println(name, 1,"ʵ����ת+�ƶ�,ʵ��id��"+ri.readInt()+",����:\n" + 
												"X:"+ri.readByte()+"\tY:"+ri.readByte()+"\tZ"+ri.readByte()+"\n"+
												"X��ת:"+ri.readByte()+"\tY��ת:"+ri.readByte());
						}else if(ri.id==0x18) {
							cfg.println(name,1,"ʵ���ƶ�����4���飬ʵ��id:"+ri.readInt()+",����:\n"+
												"X:"+ri.readInt()+"\tY:"+ri.readInt()+"\tZ:"+ri.readInt()+"\n"+
												"X��ת:"+ri.readByte()+"\tY��ת:"+ri.readByte());
						}else if(ri.id==0x19) {
							cfg.println(name,1,"ʵ��ͷ����ת��ʵ��id"+ri.readInt()+"��X��ת:"+ri.readByte());
						}else if(ri.id==0x1A) {
							int eid=ri.readInt();
							byte type=ri.readByte();
							String t="δ֪";
							if(type==0)
								t="�������йصĶ�����";
							if(type==1)
								t="����";
							if(type==2)
								t="�����ʵ���йصĶ�����";
							if(type==3)
								t="����";
							if(type==6)
								t="ѱ������..";
							if(type==7)
								t="�Ǳ�ѱ����";
							if(type==8)
								t="���Լ���ˮ";
							if(type==9)
								t="�����ң����������ܵ���ʳ";
							if(type==10)
								t="�������ڳԲ�";
							if(type==11)
								t="�����ܽ���һ֧õ��";
							if(type==12)
								t="�����������";
							if(type==13)
								t="һ������������Ѱ�󱨸�";
							if(type==14)
								t="����ܿ���";
							if(type==15)
								t="Ů��ʩ��";
							if(type==16)
								t="��ʬ����ҡ�α�ɴ���";
							if(type==17)
								t="�̻���ը";
							if(type==18)
								t="�������ࣿ";
							cfg.println(name, 1,"ʵ��״̬���ı䣬ʵ��id:"+eid+",�ı��״̬:"+t);
						}else if(ri.id==0x1F) {
							cfg.println(name,1,"��ĵȼ�����ı䣬������:"+ri.readFloat()+"/1,�ȼ�:"+ri.readShort()+",�ܾ���:"+ri.readShort());
						}
					}
				}catch(EOFException e1) {
					//cfg.println(name,3,"��ȡ��Ϣʱ����:"+e1);
					//break;
				}catch(RuntimeException e2) {
					cfg.println(name,3,"��½ʱ����:"+e2);
					e2.printStackTrace();
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