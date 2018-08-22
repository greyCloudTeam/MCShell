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
			cfg.println(name,1,"成功连接服务器！");
			is=client.getInputStream();
			di=new DataInputStream(is);
			os=client.getOutputStream();
			dos=new DataOutputStream(os);
			
			cfg.println(name,1,"准备数据包....");
			
			sendPack hand=new sendPack(dos,0x00);
			hand.writeVarInt(version);
			hand.writeString(ip);
			hand.thisPack.writeShort(port);
			hand.writeVarInt(2);
			
			sendPack username=new sendPack(dos,0x00);
			username.writeString(cfg.username);
			
			hand.sendPack(false, -1);
			dos.flush();
			cfg.println(name,1,"已发送登陆包....");
			
			username.sendPack(false, -1);
			dos.flush();
			cfg.println(name,1,"等待响应....");
			while(!stop) {
				try {
					acceptPack ri=new acceptPack(di,compression);
					cfg.println(name,1, "接收到数据包\n长度:"+ri.data.length+"\tid:"+ri.id);
					if(mode==MODE_LOGIN) {
						if(ri.id==0x00) {
							throw new RuntimeException("不能连接这个服务器:\n"+ri.readString());
						}else if(ri.id==0x03) {
							cfg.println(name,2,"服务器要求启用压缩...");
							int value=ri.readVarInt();
							cfg.println(name,2,"压缩前最大数据包大小:"+value+"byte");
							maxPackSize=value;
							compression=true;
						}else if(ri.id==0x02) {
							uuid=ri.readString();
							this.username=ri.readString();
							mode=MODE_PLAY;
							
							cfg.println(name,1,"登陆成功!\n"+
									"uuid:"+uuid+"\n"+
									"username:"+this.username);
						}else if(ri.id==0x01) {
							cfg.println(name,3,"服务器开启了正版验证");
							break;
						}
					}else if(mode==MODE_PLAY) {
						if(ri.id==0x01) {//产出经验
							cfg.println(name, 1,"服务器产出经验\n");
							cfg.println(name,1,"实体id:"+ri.readVarInt()+"\t坐标:\n");
							/*
										"x:"+ri.readDouble()+"\ty:"+ri.readDouble()+"\tz:"+ri.readDouble()+"\n"+
										"数量:"+ri.readShort());
							*/
						}else if(ri.id==0x23) {//join game
							cfg.println(name, 2,"连接到游戏");
							int eid=ri.readInt();//实体id
							int gamemode=ri.readUnsignedByte();//游戏模式，0生存，1，创造，2冒险，3旁观
							this.gamemode=gamemode;
							String modeT="未知:"+gamemode;
							if(gamemode==0)
								modeT="生存";
							if(gamemode==1)
								modeT="创造";
							if(gamemode==2)
								modeT="冒险";
							if(gamemode==3)
								modeT="旁观";
							int world=ri.readInt();//不清楚，-1虚空，0主世界，1结束
							String worldT="未知:"+world;
							if(world==-1)
								worldT="虚空";
							if(world==0)
								worldT="主世界";
							if(world==1)
								worldT="结束?";
							int dif=ri.readUnsignedByte();//难度，0和平，1简单，2普通，3困难
							String difT="未知:"+dif;
							if(dif==0)
								difT="和平";
							if(dif==1)
								difT="简单";
							if(dif==2)
								difT="普通";
							if(dif==3)
								difT="困难";
							int max=ri.readUnsignedByte();//最大玩家，曾经被客户端用来绘制玩家列表，但现在被忽略了
							String type=ri.readString();//可以有default, flat, largeBiomes, amplified, default_1_1，对应：默认，平坦，大型生物群系，已放大，默认_1_1
							if(type.equals("default"))
								type="普通,默认";
							if(type.equals("flat"))
								type="平坦";
							if(type.equals("largeBiomes"))
								type="大型生物群系";
							if(type.equals("default_1_1"))
								type="默认_1_1";
							boolean RDI=ri.readBoolean();//Reduced Debug Info的简写，减少调试信息，对MCShell来说是没有用的，0x01表示为true，0x00表示为false
							cfg.println(name,1,"服务器地图信息\n"+
											"实体id:"+eid+"\n"+
											"游戏模式(gamemode):"+modeT+"\n"+
											"当前位置:"+worldT+"\n"+
											"难度:"+difT+"\n"+
											"最大玩家:"+max+"\n"+
											"地图类型:"+type+"\n"+
											"减少调试信息?:"+RDI);
						}else if(ri.id==0x18) {
							String name=ri.readString();
							byte[] data=ri.getAllData();
							cfg.println(this.name, 2,"接收到服务端的插件信息\n"+
										"插件名字:"+name+"\t数据:"+Arrays.toString(data)+"\n"+
										"转换为文本:"+new String(data));
							
						}else if(ri.id==0x0d) {
							int dif=ri.readUnsignedByte();
							String text="未知:"+dif;
							if(dif==0)
								text="和平";
							if(dif==1)
								text="简单";
							if(dif==2)
								text="普通";
							if(dif==3)
								text="困难";
							cfg.println(this.name,2,"服务端要求更改难度:"+text);
						}else if(ri.id==0x2c) {
							byte nl=ri.readByte();
							cfg.println(name, 2, "你拥有这些能力:"+nl+"\n"+
										"飞行速度:"+ri.readFloat()+"\t视野:"+ri.readFloat());
						}else if(ri.id==0x3a) {
							byte num=ri.readByte();
							cfg.println(name, 1,"物品栏选项索引已更新:"+num);
						}else if(ri.id==0x1b) {
							cfg.println(name,1, "实体状态被改变\n"+
										"实体id:"+ri.readInt()+"\t更改后状态:"+ri.readByte());
						}else if(ri.id==0x0f) {
							String msg=ri.readString();
							byte type=ri.readByte();
							String text="未知";
							if(type==0)
								text="玩家的信息";
							if(type==1)
								text="系统消息";
							if(type==2)
								text="游戏信息";
							cfg.println(name, 1,text+"\n"+msg);
						}else if(ri.id==0x31) {//解锁合成
							int action=ri.readVarInt();
							String actionText="未知";
							if(action==0)
								actionText="设置";
							if(action==1)
								actionText="添加";
							if(action==2)
								actionText="删除";
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
								cfg.println(name, 1,"解锁合成(下面的信息由于不知道如何翻译，所以都是英文)\n"+
											"action:"+actionText+"\tCrafting Book Open"+bool1+"\tFiltering Craftable:"+bool2+"\n"+
											"Recipe IDs1:\n"+Arrays.toString(array1)+"\n"+
											"Recipe IDs2:\n"+Arrays.toString(array2));
								continue;
							}
							cfg.println(name, 1,"解锁合成(下面的信息由于不知道如何翻译，所以都是英文)\t"+
									"action:"+actionText+"\tCrafting Book Open"+bool1+"\tFiltering Craftable:"+bool2+"\n"+
									"Recipe IDs1:\n"+Arrays.toString(array1)+"\n"+
									"Recipe IDs2:\n不存在");
						}else if(ri.id==0x2e) {
							int action=ri.readVarInt();
							int playNum=ri.readVarInt();
							String status="未知";
							if(action==0)
								status="新增玩家";
							if(action==1)
								status="更新游戏模式";
							if(action==2)
								status="更新延迟";
							if(action==3)
								status="更新名称";
							if(action==4)
								status="删除玩家";
							String msg="玩家列表更新:"+status;
							for(int i=0;i<playNum;i++) {
								byte[] uuid=new byte[16];
								ri.readFully(uuid);
								msg+="\nuuid:"+Arrays.toString(uuid);
								if(action==0) {
									msg+="\n玩家名:"+ri.readString();
									int num=ri.readVarInt();
									for(int a=0;a<num;a++) {
										msg+="\n属性名:"+ri.readString();
										msg+="\t属性值:"+ri.readString();
										boolean sign=ri.readBoolean();
										msg+="\t是否签名:"+sign;
										if(sign) {
											msg+="\t签名:"+ri.readString();
										}
									}
									msg+="\n游戏模式:"+ri.readVarInt();
									msg+="\t延迟:"+ri.readVarInt()+"ms";
									if(ri.readBoolean())
										msg+="\t显示名称:\n"+ri.readString();
								}
								if(action==1) {
									msg+="\n游戏模式:"+ri.readVarInt();
								}
								if(action==2) {
									msg+="\n延迟:"+ri.readVarInt()+"ms";
								}
								if(action==3) {
									boolean bool=ri.readBoolean();
									msg+="\n是否存在显示名称:"+bool;
									if(bool)
										msg+="\t显示名称:\n"+ri.readString();
								}
								msg+="\n"+"--------------------------------------------------------";
							}
							cfg.println(name, 1,msg);
						}else if(ri.id==0x47) {
							cfg.println(name,1,"服务器时间更新:\n"+
												"地图年龄:"+ri.readLong()+"\t当天的时间:"+ri.readLong());
						}else if(ri.id==0x1f) {
							/*
							byte[] id=ri.getAllData();
							cfg.println(name, 1,"服务端要求保持连接:"+Arrays.toString(id));
							b= new ByteArrayOutputStream();
							handshake = new DataOutputStream(b);
							cfg.writeVarInt(handshake, 0x1f);
							handshake.write(id);
							acceptPack.sendPack(dos, b.toByteArray(), compression, maxPackSize);
							*/
						}
					}
				}catch(EOFException e1) {
					//cfg.println(name,3,"读取消息时出错:"+e1);
					//break;
				}catch(RuntimeException e2) {
					cfg.println(name,3,"登陆时出错:"+e2);
					e2.printStackTrace();
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