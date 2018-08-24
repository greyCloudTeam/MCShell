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
	public final int MODE_LEAVE=3;
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
			cfg.println(name,1,"閹存劕濮涙潻鐐村复閺堝秴濮熼崳顭掔磼");
			is=client.getInputStream();
			di=new DataInputStream(is);
			os=client.getOutputStream();
			dos=new DataOutputStream(os);
			
			cfg.println(name,1,"閸戝棗顦弫鐗堝祦閸栵拷....");
			
			sendPack hand=new sendPack(dos,0x00);
			hand.writeVarInt(version);
			hand.writeString(ip);
			hand.thisPack.writeShort(port);
			hand.writeVarInt(2);
			
			sendPack username=new sendPack(dos,0x00);
			username.writeString(cfg.username);
			
			hand.sendPack(false, -1);
			dos.flush();
			cfg.println(name,1,"瀹告彃褰傞柅浣烘闂勫棗瀵�....");
			
			username.sendPack(false, -1);
			dos.flush();
			cfg.println(name,1,"缁涘绶熼崫宥呯安....");
			while(!stop) {
				try {
					acceptPack ri=new acceptPack(di,compression);
					cfg.println(name,1, "閹恒儲鏁归崚鐗堟殶閹诡喖瀵橀敍宀勬毐鎼达拷:"+ri.data.length+"閿涘d:"+ri.id);
					if(mode==MODE_LOGIN) {
						if(ri.id==0x00) {
							throw new RuntimeException("娑撳秷鍏樻潻鐐村复鏉╂瑤閲滈張宥呭閸ｏ拷:\n"+ri.readString());
						}else if(ri.id==0x02) {
							uuid=ri.readString();
							this.username=ri.readString();
							mode=MODE_PLAY;
							
							cfg.println(name,1,"閻у妾伴幋鎰!\n"+
									"uuid:"+uuid+"\n"+
									"username:"+this.username);
						}else if(ri.id==0x01) {
							cfg.println(name,3,"閺堝秴濮熼崳銊ョ磻閸氼垯绨″锝囧妤犲矁鐦�");
							break;
						}
					}else if(mode==MODE_PLAY) {
						if(ri.id==0x00) {
							int id=ri.readInt();
							cfg.println(name, 1,"閺堝秴濮熺粩顖濐洣濮瑰倷绻氶幐浣界箾閹猴拷,闂呭繑婧�id:"+id);
							sendPack p=new sendPack(dos,0x00);
							p.thisPack.writeInt(id);
							p.sendPack(false, -1);
						}else if(ri.id==0x01) {//join game
							cfg.println(name, 2,"鏉╃偞甯撮崚鐗堢埗閹达拷");
							int eid=ri.readInt();//鐎圭偘缍媔d
							int gamemode=ri.readUnsignedByte();//濞撳憡鍨欏Ο鈥崇础閿涳拷0閻㈢喎鐡ㄩ敍锟�1閿涘苯鍨遍柅鐙呯礉2閸愭帡娅撻敍锟�3閺冧浇顫�
							this.gamemode=gamemode;
							String modeT="閺堫亞鐓�:"+gamemode;
							if(gamemode==0)
								modeT="閻㈢喎鐡�";
							if(gamemode==1)
								modeT="閸掓盯锟斤拷";
							if(gamemode==2)
								modeT="閸愭帡娅�";
							if(gamemode==3)
								modeT="閺冧浇顫�";
							byte world=ri.readByte();//娑撳秵绔诲Δ姘剧礉-1閾忔氨鈹栭敍锟�0娑撹绗橀悾宀嬬礉1缂佹挻娼�
							String worldT="閺堫亞鐓�:"+world;
							if(world==-1)
								worldT="閾忔氨鈹�";
							if(world==0)
								worldT="娑撹绗橀悾锟�";
							if(world==1)
								worldT="娑撳鏅�";
							int dif=ri.readUnsignedByte();//闂呮儳瀹抽敍锟�0閸滃苯閽╅敍锟�1缁狅拷閸楁洩绱�2閺咁噣锟芥熬绱�3閸ヤ即姣�
							String difT="閺堫亞鐓�:"+dif;
							if(dif==0)
								difT="閸滃苯閽�";
							if(dif==1)
								difT="缁狅拷閸楋拷";
							if(dif==2)
								difT="閺咁噣锟斤拷";
							if(dif==3)
								difT="閸ヤ即姣�";
							int max=ri.readUnsignedByte();//閺堬拷婢堆呭负鐎硅绱濋弴鍓х病鐞氼偄顓归幋椋庮伂閻€劍娼电紒妯哄煑閻溾晛顔嶉崚妤勩�冮敍灞肩稻閻滄澘婀悮顐㈡嫹閻ｃ儰绨�
							String type=ri.readString();//閸欘垯浜掗張濉猠fault, flat, largeBiomes, amplified, default_1_1閿涘苯顕惔鏃撶窗姒涙顓婚敍灞介挬閸э讣绱濇径褍鐎烽悽鐔哄⒖缂囥倗閮撮敍灞藉嚒閺�鎯с亣閿涘矂绮拋顦�1_1
							if(type.equals("default"))
								type="閺咁噣锟斤拷,姒涙顓�";
							if(type.equals("flat"))
								type="楠炲啿娼�";
							if(type.equals("largeBiomes"))
								type="婢堆冪�烽悽鐔哄⒖缂囥倗閮�";
							if(type.equals("default_1_1"))
								type="姒涙顓籣1_1";
							//boolean RDI=ri.readBoolean();//Reduced Debug Info閻ㄥ嫮鐣濋崘娆欑礉閸戝繐鐨拫鍐槸娣団剝浼呴敍灞筋嚠MCShell閺夈儴顕╅弰顖涚梾閺堝鏁ら惃鍕剁礉0x01鐞涖劎銇氭稉绨峳ue閿涳拷0x00鐞涖劎銇氭稉绡篴lse
							cfg.println(this.name,1,"閺堝秴濮熼崳銊ユ勾閸ュ彞淇婇幁鐥媙"+
											"鐎圭偘缍媔d:"+eid+"\n"+
											"濞撳憡鍨欏Ο鈥崇础(gamemode):"+modeT+"\n"+
											"瑜版挸澧犳担宥囩枂:"+worldT+"\n"+
											"闂呮儳瀹�:"+difT+"\n"+
											"閺堬拷婢堆呭负鐎癸拷:"+max+"\n"+
											"閸︽澘娴樼猾璇茬��:"+type);
											//"閸戝繐鐨拫鍐槸娣団剝浼�?:"+RDI);
						}else if(ri.id==0x02) {
							String msg=ri.readString();
							cfg.println(this.name, 1,"閼卞﹤銇夋穱鈩冧紖:\n"+msg);
						}else if(ri.id==0x03) {
							cfg.println(this.name,1,"閺堝秴濮熼崳銊︽闂傚瓨娲块弬锟�:\n"+
									"閸︽澘娴橀弮鏈靛敩:"+ri.readLong()+"\t瑜版挸銇夐惃鍕闂傦拷:"+ri.readLong());
						}else if(ri.id==0x04) {
							int eid=ri.readInt();
							short slot=ri.readShort();
							String slotS="閺堫亞鐓�";
							if(slot==0)
								slotS="閹镐焦婀�";
							if(slot==1)
								slotS="闂堟潙鐡�";
							if(slot==2)
								slotS="缂佹垼鍚�";
							if(slot==3)
								slotS="閼虫悂鎽�";
							if(slot==4)
								slotS="婢跺娲�";
							short id=ri.readShort();
							if(id!=-1) {
								byte num=ri.readByte();
								short s=ri.readShort();
								cfg.println(this.name, 1,"鐎圭偘缍嬬憗鍛槵閺囧瓨鏌�:\n"+
											"鐎圭偘缍媔d:"+eid+"\t閹绘帗蝎:"+slot+"\t閸фd:"+id+"\t閻椻晛鎼ч弫浼村櫤:"+num+"\t閹圭喎娼栫粙瀣:"+s);
								continue;
							}
							cfg.println(this.name, 1,"鐎圭偘缍嬬憗鍛槵閺囧瓨鏌�:\n"+
									"鐎圭偘缍媔d:"+eid+"\t閹绘帗蝎:"+slot+"\t閸фd:缁岋拷");
						}else if(ri.id==0x05) {
							cfg.println(this.name, 2,"娴ｅ秶鐤嗛弴瀛樻煀!娴ｇ姴缍嬮崜宥囨畱娴ｅ秶鐤�:\n"+
										"x:"+ri.readInt()+"\ty:"+ri.readInt()+"\tz:"+ri.readInt());
						}else if(ri.id==0x06){
							float heal=ri.readFloat();
							short food=ri.readShort();
							float bh=ri.readFloat();
							if(heal<=0){
								cfg.println(this.name, 2,"娴ｇ姴鍑＄紒蹇旑劥娴溾槄绱�");
								continue;
							}
							cfg.println(this.name, 2,"娴ｇ姷娈戠悰锟介柌锟�/妤椼儵銈块崐鍏兼纯閺傦拷!\n"+
										"鐞涳拷闁诧拷:"+heal+"\t妤椼儵銈块崐锟�:"+food+"\t妤椼儵銈块崐濂搞偙閸滃苯瀹�:"+bh);
						}else if(ri.id==0x07) {
							int world=ri.readInt();//娑撳秵绔诲Δ姘剧礉-1閾忔氨鈹栭敍锟�0娑撹绗橀悾宀嬬礉1缂佹挻娼�
							String worldT="閺堫亞鐓�:"+world;
							if(world==-1)
								worldT="閾忔氨鈹�";
							if(world==0)
								worldT="娑撹绗橀悾锟�";
							if(world==1)
								worldT="娑撳鏅�";
							int dif=ri.readUnsignedByte();//闂呮儳瀹抽敍锟�0閸滃苯閽╅敍锟�1缁狅拷閸楁洩绱�2閺咁噣锟芥熬绱�3閸ヤ即姣�
							String difT="閺堫亞鐓�:"+dif;
							if(dif==0)
								difT="閸滃苯閽�";
							if(dif==1)
								difT="缁狅拷閸楋拷";
							if(dif==2)
								difT="閺咁噣锟斤拷";
							if(dif==3)
								difT="閸ヤ即姣�";
							
							int gamemode=ri.readUnsignedByte();//濞撳憡鍨欏Ο鈥崇础閿涳拷0閻㈢喎鐡ㄩ敍锟�1閿涘苯鍨遍柅鐙呯礉2閸愭帡娅撻敍锟�3閺冧浇顫�
							this.gamemode=gamemode;
							String modeT="閺堫亞鐓�:"+gamemode;
							if(gamemode==0)
								modeT="閻㈢喎鐡�";
							if(gamemode==1)
								modeT="閸掓盯锟斤拷";
							if(gamemode==2)
								modeT="閸愭帡娅�";
							if(gamemode==3)
								modeT="閺冧浇顫�";
							
							String type=ri.readString();//閸欘垯浜掗張濉猠fault, flat, largeBiomes, amplified, default_1_1閿涘苯顕惔鏃撶窗姒涙顓婚敍灞介挬閸э讣绱濇径褍鐎烽悽鐔哄⒖缂囥倗閮撮敍灞藉嚒閺�鎯с亣閿涘矂绮拋顦�1_1
							if(type.equals("default"))
								type="閺咁噣锟斤拷,姒涙顓�";
							if(type.equals("flat"))
								type="楠炲啿娼�";
							if(type.equals("largeBiomes"))
								type="婢堆冪�烽悽鐔哄⒖缂囥倗閮�";
							if(type.equals("default_1_1"))
								type="姒涙顓籣1_1";
							
							cfg.println(this.name,2,"閺堝秴濮熺粩顖濐洣濮瑰倹娲块弨鍦樊鎼达拷:\n"+
										"缂佹潙瀹�:"+worldT+"\t闂呮儳瀹�:"+difT+"\t濞撳憡鍨欏Ο鈥崇础:"+modeT+"\t閸︽澘娴樼猾璇茬��:"+type);
						}else if(ri.id==0x08) { 
							double x=ri.readDouble();
							double y=ri.readDouble();
							double z=ri.readDouble();
							float ph=ri.readFloat();
							float p=ri.readFloat();
							boolean d=ri.readBoolean();
							cfg.println(this.name, 2,"娴ｇ姷娈戞担宥囩枂鐞氼偅娲块弨锟�:\n"+
												"X:"+x+"\tY:"+y+"\tZ:"+z+"\t閸嬪繗鍩�:"+ph+"\t娣囶垵顫�:"+p+"\t閸︺劌婀存稉锟�:"+d);
						}else if(ri.id==0x09) {
							byte i=ri.readByte();
							cfg.println(this.name,2,"娴ｇ娀锟藉瀚ㄩ惃鍕躬閻椻晛鎼ч弽蹇庤厬閻ㄥ嫮澧块崫浣哄偍瀵洝顫﹂弴瀛樻暭:"+i);
						}else if(ri.id==0x0A) {
							int eid=ri.readInt();
							int x=ri.readInt();
							int y=ri.readUnsignedByte();
							int z=ri.readInt();
							cfg.println(this.name,1,"閺堝甯虹�硅埖鍨ㄦ担鐘哄殰瀹稿崬鍩屾惔濠佺瑐閻ゎ潕娴滐拷:\n"+
												"eid:"+eid+"\tX:"+x+"\tY:"+y+"\tZ:"+z);
						}else if(ri.id==0x0B) {
							int eid=ri.readVarInt();
							int id=ri.readUnsignedByte();
							String idT="閺堫亞鐓�";
							if(id==0)
								idT="閹藉棜鍣�";
							if(id==1)
								idT="閸欐ぞ婵�";
							if(id==2)
								idT="缁傝绱戞惔锟�";
							if(id==3)
								idT="閸氬啩绗㈢憲锟�";
							if(id==4)
								idT="娑撳秵鏌囬幒澶庮攨";
							if(id==5)
								idT="娑擃厺绨￠懡顖涙寜";
							if(id==104)
								idT="闊弓绱�";
							if(id==105)
								idT="鐠ч攱娼�";
							cfg.println(this.name, 1,"鐎圭偘缍嬮崝銊ф暰闂囷拷鐟曚焦娲块弨锟�:\n"+
												"鐎圭偘缍媔d:"+eid+"\t閸斻劎鏁�:"+idT);
						}else if(ri.id==0x0C) {
							int eid=ri.readVarInt();
							String uuid=ri.readString();
							String name=ri.readString();
							int x=ri.readIntA();
							int y=ri.readIntA();
							int z=ri.readIntA();
							byte ph=ri.readByte();
							byte p=ri.readByte();
							short item=ri.readShort();
							cfg.println(this.name, 1,"閻溾晛顔嶆潻娑樺弳瑜版挸澧犻崣顖濐潌閼煎啫娲�:\n"+
												"鐎圭偘缍媔d:"+eid+"\t閻溾晛顔島uid:"+uuid+"\t閻溾晛顔嶉崥宥呯摟:"+name+"\n"+
												"X:"+x+"\tY:"+y+"\tZ:"+z+"\t閸嬪繗鍩�:"+ph+"\t娣囶垵顫�:"+p+"\t閹靛绗傞惃鍕⒖閸濓拷:"+item);
						}else if(ri.id==0x0D) {
							int eid=ri.readInt();
							int item=ri.readInt();
							cfg.println(this.name, 1,"閺堝姹夐幏鎯у絿閻椻晛鎼ч敍灞界杽娴ｆ悆d:"+eid+",閻椻晛鎼х�圭偘缍媔d:"+item);
						}else if(ri.id==0x11) {
							cfg.println(this.name,1,"閺堝秴濮熼崳銊ら獓閸戣櫣绮℃瀛縩"+
												"鐎圭偘缍媔d:"+ri.readVarInt()+"\t閸ф劖鐖�:\n"+
												"x:"+ri.readIntA()+"\ty:"+ri.readIntA()+"\tz:"+ri.readIntA()+"\n"+
												"閺佷即鍣�:"+ri.readShort());
						}else if(ri.id==0x12) {
							cfg.println(this.name,1,"閺堝秴濮熺粩顖濐啎缂冾喖鐤勬担鎾伙拷鐔峰:\n"+
												"鐎圭偘缍媔d:"+ri.readInt()+"\tX闁喎瀹�:"+ri.readShort()+"\tY闁喎瀹�:"+ri.readShort()+"\tZ闁喎瀹�:"+ri.readShort());
						}else if(ri.id==0x13) {
							cfg.println(this.name, 1,"鐎圭偘缍嬬悮顐ｆ噮濮ｄ緤绱濋弫浼村櫤"+ri.readByte());
						}else if(ri.id==0x15) {
							cfg.println(this.name, 1,"鐎圭偘缍嬮惄绋款嚠缁夎濮╅敍灞界杽娴ｆ悆d:"+ri.readInt()+"閿涘苯娼楅弽锟�:\n"+
												"X:"+ri.readByte()+"\tY:"+ri.readByte()+"\tZ"+ri.readByte());
						}else if(ri.id==0x16) {
							cfg.println(this.name,1,"鐎圭偘缍嬮弮瀣祮閿涘苯鐤勬担鎼僤:"+ri.readInt()+",X閺冨娴�:"+ri.readByte()+",Y閺冨娴�:"+ri.readByte());
						}else if(ri.id==0x17) {
							cfg.println(this.name, 1,"鐎圭偘缍嬮弮瀣祮+缁夎濮�,鐎圭偘缍媔d閿涳拷"+ri.readInt()+",閸ф劖鐖�:\n" + 
												"X:"+ri.readByte()+"\tY:"+ri.readByte()+"\tZ"+ri.readByte()+"\n"+
												"X閺冨娴�:"+ri.readByte()+"\tY閺冨娴�:"+ri.readByte());
						}else if(ri.id==0x18) {
							cfg.println(this.name,1,"鐎圭偘缍嬬粔璇插З鐡掑懓绻�4娑擃亜娼￠敍灞界杽娴ｆ悆d:"+ri.readInt()+",閸ф劖鐖�:\n"+
												"X:"+ri.readIntA()+"\tY:"+ri.readIntA()+"\tZ:"+ri.readIntA()+"\n"+
												"X閺冨娴�:"+ri.readByte()+"\tY閺冨娴�:"+ri.readByte());
						}else if(ri.id==0x19) {
							cfg.println(this.name,1,"鐎圭偘缍嬫径鎾劥閺冨娴嗛敍灞界杽娴ｆ悆d"+ri.readInt()+"閿涘閺冨娴�:"+ri.readByte());
						}else if(ri.id==0x1A) {
							int eid=ri.readInt();
							byte type=ri.readByte();
							String t="閺堫亞鐓�";
							if(type==0)
								t="娑撳海鏁撻悧鈺傛箒閸忓磭娈戞稉婊嗐偪閿涳拷";
							if(type==1)
								t="閸欐ぞ婵�";
							if(type==2)
								t="娑撳海甯虹�硅泛鐤勬担鎾存箒閸忓磭娈戞稉婊嗐偪閿涳拷";
							if(type==3)
								t="濮濊楠�";
							if(type==6)
								t="妞诡垱婀囬悪闂磋厬..";
							if(type==7)
								t="閻欒壈顫︽す顖涙箛娴滐拷";
							if(type==8)
								t="閻欒壈鍤滃杈ㄥ濮橈拷";
							if(type==9)
								t="閿涘牐鍤滈幋鎴礆閺堝秴濮熼崳銊﹀复閸欐娈戞顕�顥�";
							if(type==10)
								t="缂佺數绶ゅ锝呮躬閸氬啳宕�";
							if(type==11)
								t="闁句礁鍊嬮崕鈥叉唉閸戣桨绔撮弨顖滃缚閻燂拷";
							if(type==12)
								t="閺夋垶鐨禍褏鏁撻悥鍗炵妇";
							if(type==13)
								t="娑擄拷娑擃亝娼欏鎴犳晸濮樻柨鑻熺�电粯鐪伴幎銉ヮ槻";
							if(type==14)
								t="閺夋垶鐨鍫濇彥娑旓拷";
							if(type==15)
								t="婵傚啿甯嗛弬鑺ョ《";
							if(type==16)
								t="閸嶉潧妗堥崜褏鍎撻幗鍥ㄦ閸欐ɑ鍨氶弶鎴炵毌";
							if(type==17)
								t="閻戠喕濮抽悥鍡欏仮";
							if(type==18)
								t="閻栧彉绗傛禍铏硅閿涳拷";
							cfg.println(this.name, 1,"鐎圭偘缍嬮悩鑸碉拷浣筋潶閺�鐟板綁閿涘苯鐤勬担鎼僤:"+eid+",閺�鐟板綁閸氬海濮搁幀锟�:"+t);
						}else if(ri.id==0x1F) {
							cfg.println(this.name,1,"娴ｇ姷娈戠粵澶岄獓閹存牜绮℃灞炬暭閸欐﹫绱濈紒蹇涚崣閺夛拷:"+ri.readFloat()+"/1,缁涘楠�:"+ri.readShort()+",閹崵绮℃锟�:"+ri.readShort());
						}else if(ri.id==0x27) {
							cfg.println(this.name, 2,"闂勫嫯绻庨崣鎴犳晸閻栧棛鍋�:\n"+
												"X:"+ri.readFloat()+"\tY:"+ri.readFloat()+"\tZ:"+ri.readFloat()+"\t閸楀﹤绶�:"+ri.readFloat());
						}else if(ri.id==0x28) {
							int m=ri.readInt();
							String text="閸忔湹绮�";
							if(m==1000||m==1001)
								text="閻愮懓鍤�";
							if(m==1002)
								text="bow";
							if(m==1003)
								text="閹垫挸绱戦幋鏍у彠娑撳﹪妫�";
							if(m==1004)
								text="閸㈣泛妯佹竟锟�";
							if(m==1005)
								text="閹绢厽鏂侀崗澶屾磸";
							if(m==1007)
								text="閹爼鐡婇崘鑼剁箖閺夈儰绨�";
							if(m==1008)
								text="閹爼鐡婇崣鎴濆毉閻忣偆鎮�";
							if(m==1009)
								text="閹爼鐡婇惃鍕紑閻炲啯鍙冮幈銏犲綁鐏忥拷";
							if(m==1010)
								text="閸嶉潧妗堥張銊ャ仈閿涳拷";
							if(m==1011)
								text="閸嶉潧妗堥柌鎴濈潣閿涳拷";
							if(m==1012)
								text="閸嶉潧妗堥張銊ャ仈閹垫挾鐗敍锟�";
							if(m==1013)
								text="閻㈢喐鍨氶崙瀣祩";
							if(m==1014)
								text="閸戝娴傞弨璇插毊";
							if(m==1015)
								text="閾︽瑨娼拃鎴掔瑓閿涳拷";
							if(m==1016)
								text="閸嶉潧妗堥幇鐔哥厠";
							if(m==1017)
								text="閸嶉潧妗堥幁銏狀槻閿涳拷";
							if(m==1018)
								text="enderdragon end";
							if(m==1020)
								text="闁句胶鐗曢幑鐔锋綎";
							if(m==1021)
								text="闁句胶鐗曟担璺ㄦ暏";
							if(m==1022)
								text="闁句胶鐗曢梽宥堟儰";
							if(m==2000)
								text="娴溠呮晸10娑擃亞鐭戠�涳拷";
							if(m==2002)
								text="閼筋垱鎸夋鐐寸盃";
							cfg.println(this.name, 1,"閸涖劌娲块張澶婏紣闂婏拷:\n"+
												"X:"+ri.readInt()+"\tY:"+ri.readByte()+"\tZ:"+ri.readInt()+"\t閸欐垹鏁撻惃鍕紣闂婏拷:"+text);
						}else if(ri.id==0x2B) {
							int be=ri.readUnsignedByte();
							String text="閸忔湹绮�";
							if(be==0)
								text="鎼村﹥妫ら弫鍫吹";
							if(be==1)
								text="缂佹挻娼稉瀣处";
							if(be==2)
								text="瀵拷婵绗呴梿锟�";
							if(be==3)
								text="閺�鐟板綁濞撳憡鍨欏Ο鈥崇础";
								float value=ri.readFloat();
								if(value==0)
									text+="閿涘瞼鏁撶�涳拷";
								if(value==1)
									text+="閿涘苯鍨遍柅锟�";
								if(value==3)
									text+="閿涘苯鍟嬮梽锟�";
							if(be==4)
								text="鏉堟挸鍙嗙�涳箑鍨庨敍锟�";
							if(be==6)
								text="鐏忓嫪鑵戦崚鎵负鐎硅绱�";
							cfg.println(this.name, 1,"閺�鐟板綁濞撳憡鍨欓悩鑸碉拷渚婄吹"+text);
						}else if(ri.id==0x2D) {
							cfg.println(this.name,1,"缁愭褰涢幍鎾崇磻閿涘瞼鐛ラ崣顤痙:"+ri.readUnsignedByte()+",缁愭褰涚猾璇茬��:"+ri.readUnsignedByte()+",缁愭褰涢弽鍥暯:"+ri.readString()+",閹绘帗蝎閺佷即鍣�:"+ri.readUnsignedByte());
						}else if(ri.id==0x2E) {
							cfg.println(this.name, 1,"缁愭褰涢崗鎶芥４閿涘瞼鐛ラ崣顤痙:"+ri.readUnsignedByte());
						}else if(ri.id==0x2F) {
							byte id=ri.readByte();
							short slot=ri.readShort();
							short bid=ri.readShort();
							if(bid==-1) {
								cfg.println(this.name,1,"缁愭褰涢幓鎺撔悧鈺佹惂閺囧瓨鏌婇敍宀�鐛ラ崣顤痙:"+id+",閹绘帗蝎:"+slot+",閹绘帗蝎娑擃厾娈戦悧鈺佹惂閸欐ü璐熺粚锟�");
							}else {
								byte num=ri.readByte();
								short s=ri.readShort();
								cfg.println(this.name,1,"缁愭褰涢幓鎺撔悧鈺佹惂閺囧瓨鏌婇敍宀�鐛ラ崣顤痙:"+id+",閹绘帗蝎:"+slot+",閹绘帗蝎娑擃厾娈戦悧鈺佹惂id:"+bid+",閺佷即鍣�:"+num+",閹圭喎娼栫粙瀣:"+s);
							}
						}else if(ri.id==0x30) {
							cfg.println(this.name, 1,"缁愭褰涢幓鎺撔悧鈺佹惂閺囧瓨鏌婇敍宀�鐛ラ崣顤痙:"+ri.readUnsignedByte());
						}else if(ri.id==0x31) {
							cfg.println(this.name, 1,"缁愭褰涙稉顓犳畱鐏炵偞锟窖勬纯閺傦拷,缁愭褰沬d:"+ri.readUnsignedByte()+",鐏炵偞锟斤拷:"+ri.readShort()+",閸婏拷:"+ri.readShort());
						}else if(ri.id==0x32) {
							int id=ri.readUnsignedByte();
							short code=ri.readShort();
							boolean y=ri.readBoolean();
							String text="";
							if(y)
								text="閺堝秴濮熼崳銊﹀复閸欐顕Ч鍌︾礉缁愭褰沬d:";
							else
								text="閺堝秴濮熼崳銊﹀珕缂佹繆顕Ч鍌︾礉缁愭褰沬d:";
							cfg.println(this.name, 1,text+id+",閸斻劋缍旂紓鏍у娇:"+code);
						}else if(ri.id==0x33) {
							cfg.println(this.name, 1,"閸欐垹骞囬崗顒�鎲￠悧锟�,X:"+ri.readInt()+",Y:"+ri.readShort()+",Z:"+ri.readInt()+",娑撳娼伴弰顖氬彆閸涘﹦澧濋惃鍕敶鐎癸拷:\n"+
												ri.readString()+"\n"+
												ri.readString()+"\n"+
												ri.readString()+"\n"+
												ri.readString());
						}else if(ri.id==0x36) {
							cfg.println(this.name, 1,"閸忣剙鎲￠悧宀�绱潏鎴濇珤閹垫挸绱�,X:"+ri.readInt()+",Y:"+ri.readInt()+",Z:"+ri.readInt());
						}else if(ri.id==0x37) {
							int cout=ri.readVarInt();
							String text="娑撳娼伴弰顖滅埠鐠佲�蹭繆閹拷:";
							for(int i=0;i<cout;i++) {
								text+="\n閻溾晛顔嶉崥锟�:"+ri.readString()+",閸掑棙鏆�:"+ri.readVarInt()+"\n-----------------------------------------------------";
							}
							cfg.println(this.name, 1,text);
						}else if(ri.id==0x38) {
							cfg.println(this.name, 1,"閻溾晛顔嶉崥宥呯摟:"+ri.readString()+",閸︺劎鍤�:"+ri.readBoolean()+",瀵ゆ儼绻�:"+ri.readShort()+"ms");
						}else if(ri.id==0x39) {
							cfg.println(this.name, 1,"閻溾晛顔嶉懗钘夊:"+ri.readByte()+",妞嬬偠顢戦柅鐔峰:"+ri.readFloat()+",鐞涘矁铔嬮柅鐔峰:"+ri.readFloat());
						}else if(ri.id==0x3A) {
							cfg.println(this.name, 1,"閸掓儼銆冪�瑰本鍨氶敍锟�"+ri.readString());
						}else if(ri.id==0x3B) {
							String name=ri.readString();
							String value=ri.readString();
							byte flag=ri.readByte();
							String text="";
							if(flag==0)
								text="閸掓稑缂�";
							if(flag==1)
								text="閸掔娀娅�";
							if(flag==2)
								text="閺囧瓨鏌�";
							cfg.println(this.name, 1,"鐠佹澘鍨庨悧锟�"+text+",閻溾晛顔嶉崥锟�:"+name+",閸婏拷:"+value);
						}else if(ri.id==0x3C) {
							String name=ri.readString();
							byte o=ri.readByte();
							if(o==0)
								cfg.println(this.name, 1,"閺囧瓨鏌婇崚鍡樻殶閿涘瞼甯虹�硅泛鎮�:"+name+"閸掑棙鏆熼崥宥囆�:"+ri.readString()+",閸婏拷"+ri.readInt());
							else
								cfg.println(this.name, 1,"閸掔娀娅庨崚鍡樻殶,閻溾晛顔嶉崥锟�:"+name);
						}else if(ri.id==0x3D) {
							byte o=ri.readByte();
							String text="閺堫亞鐓�";
							if(o==0)
								text="閸掓銆�";
							if(o==1)
								text="鏉堣鐖�";
							if(o==2)
								text="閸氬秴鐡ф稉瀣桨";
							cfg.println(this.name,1,"閺勫墽銇氱拋鏉垮瀻閺夊尅绱濇担宥囩枂:"+text+",閸掑棙鏆熼崥宥囆�:"+ri.readString());
						}else if(ri.id==0x3E) {
							String name=ri.readString();
							byte mode=ri.readByte();
							if(mode==0) {
								String sname=ri.readString();
								String q=ri.readString();
								String h=ri.readString();
								byte ys=ri.readByte();
								String ysText="閺屻儳婀呴崣瀣偨闂呮劕鑸�?";
								if(ys==0)
									ysText="閸忔娊妫�";
								if(ys==1)
									ysText="閹垫挸绱�";
								short num=ri.readShort();
								String list="";
								for(int i=0;i<num;i++) {
									list+=ri.readString()+"\t";
								}
								cfg.println(this.name,1,"閸ャ垽妲﹂崚娑樼紦閿涘苯娲熼梼鐔锋倳缁夛拷:"+name+",閸ャ垽妲﹂弰鍓с仛閸氬秶袨:"+sname+",閸ャ垽妲﹂崜宥囩磻:"+q+",閸ャ垽妲﹂崥搴ｇ磻:"+h+",閸欏鏉芥稊瀣紑:"+ysText+",閻溾晛顔嶉弫浼村櫤:"+num+"\n"+list);
								continue;
							}
							if(mode==2) {
								String sname=ri.readString();
								String q=ri.readString();
								String h=ri.readString();
								byte ys=ri.readByte();
								String ysText="閺屻儳婀呴崣瀣偨闂呮劕鑸�?";
								if(ys==0)
									ysText="閸忔娊妫�";
								if(ys==1)
									ysText="閹垫挸绱�";
								cfg.println(this.name,1,"閸ャ垽妲︽穱鈩冧紖閺囧瓨鏌婇敍灞芥礋闂冪喎鎮曠粔锟�:"+name+",閸ャ垽妲﹂弰鍓с仛閸氬秶袨:"+sname+",閸ャ垽妲﹂崜宥囩磻:"+q+",閸ャ垽妲﹂崥搴ｇ磻:"+h+",閸欏鏉芥稊瀣紑:"+ysText);
								continue;
							}
							if(mode==3||mode==4) {
								short num=ri.readShort();
								String list="";
								for(int i=0;i<num;i++) {
									list+=ri.readString()+"\t";
								}
								cfg.println(this.name,1,"閸ャ垽妲﹂悳鈺侇啀閸旂姴鍙嗛幋鏍у灩闂勶拷:"+name+",閻溾晛顔嶉弫浼村櫤:"+num+"\n"+list);
								continue;
							}
						}else if(ri.id==0x3F) {
							String qd=ri.readString();
							ri.readVarInt();//閼差垰鐣剧拠璇插煂0
							byte[] data=ri.getAllData();
							cfg.println(this.name, 1,"閺�璺哄煂閹绘帊娆㈡穱鈩冧紖閿涘本褰冩禒锟�:"+qd+",閺佺増宓�:"+Arrays.toString(data)+"\n鏉烆剚宕查幋鎰瀮閺堬拷:"+new String(data));
						}else if(ri.id==0x40) {
							cfg.println(this.name, 2,"閺堝秴濮熺粩顖欏瘜閸斻劍鏌囧锟芥潻鐐村复閿涘苯甯崶锟�:\n"+ri.readString()+"\n閻㈠彉绨弬顓炵磻鏉╃偞甯撮敍灞肩稑鐏忓棙妫ゅ▔鏇炲絺闁焦瀵氭禒銈忕礉鐠囬娲块幒銉ㄧ翻閸忣櫌"fuck\"閺夈儵鏀㈠В浣规拱鐟欏棗娴�");
							mode=MODE_LEAVE;
							break;
						}
					}
				}catch(EOFException e1) {
					//cfg.println(name,3,"鐠囪褰囧☉鍫熶紖閺冭泛鍤柨锟�:"+e1);
					//break;
				}catch(RuntimeException e2) {
					cfg.println(name,3,"閻у妾伴弮璺哄毉闁匡拷:"+e2);
					e2.printStackTrace();
					break;
				}catch(Exception e3) {
					cfg.println(name,3,"鐠囪褰囧☉鍫熶紖閺冭泛鍤柨锟�:"+e3);
				}
			}
		}catch(Exception e){
			cfg.println(name,3,"閻у妾伴弮璺哄毉闁匡拷:"+e);
		}
	}
}
