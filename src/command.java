import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class command extends Thread {
	Scanner s=new Scanner(System.in);
	@Override
	public void run() {
		//先执行command。mcs的命令
		//String[] d=main.command.split("\n");
		//try {
			//for(int i=0;i<d.length;i++) {
				//String[] co=d[i].split(" ");
				//if(co.length==0) {
					//continue;
				//}
				//event(co);
			//}
		//} catch (InterruptedException | IOException e) {
			// TODO 閼奉亜濮╅悽鐔稿灇閻拷 catch 閸э拷
			//e.printStackTrace();
		//}
		while(true) {
			while(cfg.commandStop) {}
			if(main.path.equals("View-Main")) {
				System.out.print(main.path+">");
			}else {
				if(cfg.allView.get(main.path).showC) {
					System.out.print(main.path+">");
				}
			}
			String[] c=s.nextLine().split(" ");
			if(c.length==0) {
				cfg.commandStop=false;
				continue;
			}
			cfg.commandStop=true;
			try {
				event(c);
			} catch (InterruptedException | IOException e) {
				// TODO 閼奉亜濮╅悽鐔稿灇閻拷 catch 閸э拷
				e.printStackTrace();
			}
		}
	}
	public static void run(String text) {
		/*
		String[] d=text.split("\n");
		try {
			for(int i=0;i<d.length;i++) {
				String[] co=d[i].split(" ");
				if(co.length==0) {
					continue;
				}
				event(co);
			}
		} catch (InterruptedException | IOException e) {
			// TODO 閼奉亜濮╅悽鐔稿灇閻拷 catch 閸э拷
			e.printStackTrace();
		}
		*/
	}
	public static void event(String[] c) throws InterruptedException, IOException {
		if(c[0].equals("help")) {
			cfg.println(1, "下面是MCShell的全部命令:\n"+
						"listPing +ip +port:获取一个服务器的信息\n"+
						"login +ip +port +versionNum(你需要通过 \"listPing\" 命令来获取，注意，不是游戏版本，是协议版本):登陆一个服务器，执行完毕后会创建一个视图\n"+
						"setUsername +username:设置MCShell登陆游戏时的用户名\n"+
						"in +token(通过 \"login\" 命令创建视图后输出的视图id):切换视图\n"+
						"hide:隐藏命令提示符\n"+
						"show:显示命令提示符\n"+
						"fuck:销毁当前视图\n"+
						"hidePack +lv(隐藏的数据包的等级，可选msg:不接收聊天信息，pos:不接收未知的信息，obj:不接收有关物体的信息，detail:不接收详细信息) +id:屏蔽指定的数据包，默认只接收聊天信息\n"+
						"showPack +lv(显示的数据包的等级，可选msg:接收聊天信息，pos:接收位置的信息，obj:接收有关物体的信息，detail:接收详细信息) +id:显示指定的数据包，默认只接收聊天信息\n"+
						"chat +msg:向服务器发送消息");
		}else if(c[0].equals("")){
			
		}else if(c[0].equals("show")) {
			if(main.path.equals("View-Main")) {
				cfg.println(3, "不能在Main视图中使用此命令");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			cfg.allView.get(main.path).showC=true;
		}else if(c[0].equals("hide")) {
			if(main.path.equals("View-Main")) {
				cfg.println(3, "不能在Main视图中使用此命令");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			cfg.allView.get(main.path).showC=false;
		}else if(c[0].equals("fuck")) {
			if(main.path.equals("View-Main")) {
				cfg.println(3, "不能在Main视图中使用此命令");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			View temp=cfg.allView.get(main.path);
			temp.client.close();
			temp.stop=true;
			cfg.allView.remove(main.path);
			System.out.println("\n\n\n\n\n\n");
			main.path="View-Main";
			Thread.sleep(100);
			cfg.commandStop=false;
			return;
		}else if(c[0].equals("listPing")){
			if(!main.path.equals("View-Main")) {
				cfg.println(3, "不能在非Main视图中使用此命令");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			if(c.length!=3) {
				cfg.println(3, "传递的参数数量不正确");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			cfg.println(1, "请等待....");
			cfg.pingList(c[1],c[2]);
			return;
		}else if(c[0].equals("setUsername")) {
			if(!main.path.equals("View-Main")) {
				cfg.println(3, "不能在非Main视图中使用此命令");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			if(c.length!=2) {
				cfg.println(3, "传递的参数数量不正确");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			cfg.username=c[1];
		}else if(c[0].equals("login")) {
			if(!main.path.equals("View-Main")) {
				cfg.println(3, "不能在非Main视图中使用此命令");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			if(c.length!=4) {
				cfg.println(3, "传递的参数数量不正确");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			cfg.println(1, "请等待....");
			View n=new View();
			n.ip=c[1];
			n.port=Integer.parseInt(c[2]);
			n.version=Integer.parseInt(c[3]);
			n.name="View-"+(cfg.allViewNum+1);
			cfg.println(2,"成功创建视图，视图id:"+(cfg.allViewNum+1)+"请使用\"in\"命令来切换视图，进入视图后命令提示符可能是隐藏的，你可以输入 \"show\" 命令来显示命令提示符");
			cfg.allView.put(n.name, n);
			n.start();
			cfg.allViewNum++;
			return;
		}else if(c[0].equals("in")){
			if(c.length!=2) {
				cfg.println(3, "传递的参数数量不正确");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			if(c[1].equals("main")) {
				if(main.path.equals("View-Main")) {
					Thread.sleep(100);
					cfg.commandStop=false;
					return;
				}
				
				System.out.println("\n\n\n\n\n\n");
				View temp=cfg.allView.get(main.path);
				temp.hide=true;
				main.path="View-Main";
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			cfg.jumpView("View-"+c[1]);
			return;
		}else if(c[0].equals("hidePack")) {
			if(c.length!=3) {
				cfg.println(3, "传递的参数数量不正确");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			if(!cfg.allView.containsKey("View-"+c[2])) {
				cfg.println(3, "视图不存在");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			View v=cfg.allView.get("View-"+c[2]); 
			if(c[1].equals("msg")) {
				v.lv[0]=false;
				cfg.println(1, "成功屏蔽此等级的数据包");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			if(c[1].equals("pos")) {
				v.lv[1]=false;
				cfg.println(1, "成功屏蔽此等级的数据包");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			if(c[1].equals("obj")) {
				v.lv[2]=false;
				cfg.println(1, "成功屏蔽此等级的数据包");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			if(c[1].equals("detail")) {
				v.lv[3]=false;
				cfg.println(1, "成功屏蔽此等级的数据包");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			cfg.println(3,"不存在的等级");
		}else if(c[0].equals("showPack")) {
			if(c.length!=3) {
				cfg.println(3, "传递的参数数量不正确");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			if(!cfg.allView.containsKey("View-"+c[2])) {
				cfg.println(3, "视图不存在");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			View v=cfg.allView.get("View-"+c[2]); 
			if(c[1].equals("msg")) {
				v.lv[0]=true;
				cfg.println(1, "已显示此等级的数据包");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			if(c[1].equals("pos")) {
				v.lv[1]=true;
				cfg.println(1, "已显示此等级的数据包");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			if(c[1].equals("obj")) {
				v.lv[2]=true;
				cfg.println(1, "已显示此等级的数据包");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			if(c[1].equals("detail")) {
				v.lv[3]=true;
				cfg.println(1, "已显示此等级的数据包");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			cfg.println(3,"不存在的等级");
		}else if(c[0].equals("chat")) {
			if(c.length<2) {
				cfg.println(3, "传递的参数数量不正确");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			if(main.path.equals("View-Main")) {
				cfg.println(3, "不能在Main视图中使用此命令");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			String text="";
			for(int i=1;i<c.length;i++) {
				if(i==1) {
					text+=c[i];
				}else {
					text+=" "+c[i];
				}
			}
			DataOutputStream dos=cfg.allView.get(main.path).dos;
			sendPack sp=new sendPack(dos,0x01);
			sp.writeChat(text);
			sp.sendPack(cfg.allView.get(main.path).compression, cfg.allView.get(main.path).maxPackSize);
		}else if(c[0].equals("hit")) {
			if(c.length<3) {
				cfg.println(3, "传递的参数数量不正确");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			if(main.path.equals("View-Main")) {
				cfg.println(3, "不能在Main视图中使用此命令");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			byte flag=-1;
			if(c[2].equals("l")) {
				flag=0;
			}
			if(c[2].equals("r")) {
				flag=1;
			}else {
				cfg.println(3, "第三个参数不是\"l\"就是\"r\"，不能是其他的值");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			DataOutputStream dos=cfg.allView.get(main.path).dos;
			sendPack sp=new sendPack(dos,0x02);
			sp.thisPack.writeInt(Integer.parseInt(c[1]));
			sp.thisPack.writeByte(flag);
			sp.sendPack(cfg.allView.get(main.path).compression, cfg.allView.get(main.path).maxPackSize);
		}else if(c[0].equals("land")) {
			if(c.length!=2) {
				cfg.println(3, "传递的参数数量不正确");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			if(main.path.equals("View-Main")) {
				cfg.println(3, "不能在Main视图中使用此命令");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			DataOutputStream dos=cfg.allView.get(main.path).dos;
			sendPack sp=new sendPack(dos,0x03);
			if(c[1].equals("f"))
				sp.writeBoolean(false);
			if(c[1].equals("t")) {
				sp.writeBoolean(true);
			}
			sp.sendPack(cfg.allView.get(main.path).compression, cfg.allView.get(main.path).maxPackSize);
		}else{
			cfg.println(3, "未知的命令\""+c[0]+"\"");
		}
		Thread.sleep(100);
		cfg.commandStop=false;
	}
}
