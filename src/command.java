import java.io.IOException;
import java.util.Scanner;

public class command extends Thread {
	Scanner s=new Scanner(System.in);
	@Override
	public void run() {
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
	public void event(String[] c) throws InterruptedException, IOException {
		if(c[0].equals("help")) {
			cfg.println(1, "下面是MCShell的全部命令:\n"+
						"listPing +ip +port:获取一个服务器的信息\n"+
						"login +ip +port +versionNum(你需要通过 \"listPing\" 命令来获取，注意，不是游戏版本，是协议版本):登陆一个服务器，执行完毕后会创建一个视图\n"+
						"setUsername +username:设置MCShell登陆游戏时的用户名\n"+
						"in +token(通过 \"login\" 命令创建视图后输出的视图id):切换视图\n"+
						"hide:隐藏命令提示符\n"+
						"show:显示命令提示符\n"+
						"fuck:销毁当前视图");
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
		}else{
			cfg.println(3, "未知的命令\""+c[0]+"\"");
		}
		Thread.sleep(100);
		cfg.commandStop=false;
	}
}
