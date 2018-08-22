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
						if(ri.id==0x01) {//��������
							cfg.println(name, 1,"��������������\n");
							cfg.println(name,1,"ʵ��id:"+ri.readVarInt()+"\t����:\n");
							/*
										"x:"+ri.readDouble()+"\ty:"+ri.readDouble()+"\tz:"+ri.readDouble()+"\n"+
										"����:"+ri.readShort());
							*/
						}else if(ri.id==0x23) {//join game
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
							cfg.println(name, 2, "��ӵ����Щ����:"+nl+"\n"+
										"�����ٶ�:"+ri.readFloat()+"\t��Ұ:"+ri.readFloat());
						}else if(ri.id==0x3a) {
							byte num=ri.readByte();
							cfg.println(name, 1,"��Ʒ��ѡ�������Ѹ���:"+num);
						}else if(ri.id==0x1b) {
							cfg.println(name,1, "ʵ��״̬���ı�\n"+
										"ʵ��id:"+ri.readInt()+"\t���ĺ�״̬:"+ri.readByte());
						}else if(ri.id==0x0f) {
							String msg=ri.readString();
							byte type=ri.readByte();
							String text="δ֪";
							if(type==0)
								text="��ҵ���Ϣ";
							if(type==1)
								text="ϵͳ��Ϣ";
							if(type==2)
								text="��Ϸ��Ϣ";
							cfg.println(name, 1,text+"\n"+msg);
						}else if(ri.id==0x31) {//�����ϳ�
							int action=ri.readVarInt();
							String actionText="δ֪";
							if(action==0)
								actionText="����";
							if(action==1)
								actionText="���";
							if(action==2)
								actionText="ɾ��";
							boolean bool1=ri.readBoolean();
							boolean bool2=ri.readBoolean();
							int[] array1=new int[ri.readVarInt()];
							for(int i=0;i<array1.length;i++) {
								array1[i]=ri.readVarInt();
							}
							if(gamemode==0) {
								int[] array2=new int[ri.readVarInt()];
								for(int i=0;i<array2.length;i++) {
									array2[i]=ri.readVarInt();
								}
								cfg.println(name, 1,"�����ϳ�(�������Ϣ���ڲ�֪����η��룬���Զ���Ӣ��)\n"+
											"action:"+actionText+"\tCrafting Book Open"+bool1+"\tFiltering Craftable:"+bool2+"\n"+
											"Recipe IDs1:\n"+Arrays.toString(array1)+"\n"+
											"Recipe IDs2:\n"+Arrays.toString(array2));
								continue;
							}
							cfg.println(name, 1,"�����ϳ�(�������Ϣ���ڲ�֪����η��룬���Զ���Ӣ��)\t"+
									"action:"+actionText+"\tCrafting Book Open"+bool1+"\tFiltering Craftable:"+bool2+"\n"+
									"Recipe IDs1:\n"+Arrays.toString(array1)+"\n"+
									"Recipe IDs2:\n������");
						}else if(ri.id==0x2e) {
							int action=ri.readVarInt();
							int playNum=ri.readVarInt();
							String status="δ֪";
							if(action==0)
								status="�������";
							if(action==1)
								status="������Ϸģʽ";
							if(action==2)
								status="�����ӳ�";
							if(action==3)
								status="��������";
							if(action==4)
								status="ɾ�����";
							String msg="����б����:"+status;
							for(int i=0;i<playNum;i++) {
								byte[] uuid=new byte[16];
								ri.readFully(uuid);
								msg+="\nuuid:"+Arrays.toString(uuid);
								if(action==0) {
									msg+="\n�����:"+ri.readString();
									int num=ri.readVarInt();
									for(int a=0;a<num;a++) {
										msg+="\n������:"+ri.readString();
										msg+="\t����ֵ:"+ri.readString();
										boolean sign=ri.readBoolean();
										msg+="\t�Ƿ�ǩ��:"+sign;
										if(sign) {
											msg+="\tǩ��:"+ri.readString();
										}
									}
									msg+="\n��Ϸģʽ:"+ri.readVarInt();
									msg+="\t�ӳ�:"+ri.readVarInt()+"ms";
									if(ri.readBoolean())
										msg+="\t��ʾ����:\n"+ri.readString();
								}
								if(action==1) {
									msg+="\n��Ϸģʽ:"+ri.readVarInt();
								}
								if(action==2) {
									msg+="\n�ӳ�:"+ri.readVarInt()+"ms";
								}
								if(action==3) {
									boolean bool=ri.readBoolean();
									msg+="\n�Ƿ������ʾ����:"+bool;
									if(bool)
										msg+="\t��ʾ����:\n"+ri.readString();
								}
								msg+="\n"+"--------------------------------------------------------";
							}
							cfg.println(name, 1,msg);
						}else if(ri.id==0x47) {
							cfg.println(name,1,"������ʱ�����:\n"+
												"��ͼ����:"+ri.readLong()+"\t�����ʱ��:"+ri.readLong());
						}else if(ri.id==0x1f) {
							/*
							byte[] id=ri.getAllData();
							cfg.println(name, 1,"�����Ҫ�󱣳�����:"+Arrays.toString(id));
							b= new ByteArrayOutputStream();
							handshake = new DataOutputStream(b);
							cfg.writeVarInt(handshake, 0x1f);
							handshake.write(id);
							acceptPack.sendPack(dos, b.toByteArray(), compression, maxPackSize);
							*/
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