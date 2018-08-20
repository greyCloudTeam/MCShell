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
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
	}
	public void event(String[] c) throws InterruptedException, IOException {
		if(c[0].equals("help")) {
			cfg.println(1, "帮助信息，下面的是MCShell的全部命令:\n"+
						"listPing +ip +port:获取目标服务器的信息\n"+
						"login +ip +port +versionNum(你可以使用 \"listPing\" 来获取协议版本号，注意，这不是版本，是协议版本号):登陆一个服务器，如果登陆成功，会返回一个标识，默认使用MCShell用户名\n"+
						"setUsername +username:设置默认登陆的用户名\n"+
						"in +token(通过 \"login\" 来获得的标识,或者是main,即主视图):切换视图\n"+
						"hide:隐藏当前视图的命令提示符\n"+
						"show:显示当前视图的命令提示符\n"+
						"fuck:销毁当前视图，并退出到开始界面(会强制中断与服务器的连接)");
		}else if(c[0].equals("")){
			
		}else if(c[0].equals("show")) {
			if(main.path.equals("View-Main")) {
				cfg.println(3, "不能在main视图中使用这个命令");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			cfg.allView.get(main.path).showC=true;
		}else if(c[0].equals("hide")) {
			if(main.path.equals("View-Main")) {
				cfg.println(3, "不能在main视图中使用这个命令");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			cfg.allView.get(main.path).showC=false;
		}else if(c[0].equals("fuck")) {
			if(main.path.equals("View-Main")) {
				cfg.println(3, "不能在main视图中使用这个命令");
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
				cfg.println(3, "不能在非main视图中使用这个命令");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			if(c.length!=3) {
				cfg.println(3, "传递的参数不正确");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			cfg.println(1, "请等待....");
			cfg.pingList(c[1],c[2]);
			return;
		}else if(c[0].equals("login")) {
			if(!main.path.equals("View-Main")) {
				cfg.println(3, "不能在非main视图中使用这个命令");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			if(c.length!=4) {
				cfg.println(3, "传递的参数不正确");
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
			cfg.println(2,"成功创建视图，id:"+(cfg.allViewNum+1)+"，请使用\"in\"命令来切换视图，视图默认为隐藏命令提示符，请输入show来取消隐藏");
			cfg.allView.put(n.name, n);
			n.start();
			cfg.allViewNum++;
			return;
		}else if(c[0].equals("in")){
			if(c.length!=2) {
				cfg.println(3, "传递的参数不正确");
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
			cfg.println(3, "未知的命令 \""+c[0]+"\"");
		}
		Thread.sleep(100);
		cfg.commandStop=false;
	}
}
