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
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
		}
	}
	public void event(String[] c) throws InterruptedException, IOException {
		if(c[0].equals("help")) {
			cfg.println(1, "������Ϣ���������MCShell��ȫ������:\n"+
						"listPing +ip +port:��ȡĿ�����������Ϣ\n"+
						"login +ip +port +versionNum(�����ʹ�� \"listPing\" ����ȡЭ��汾�ţ�ע�⣬�ⲻ�ǰ汾����Э��汾��):��½һ���������������½�ɹ����᷵��һ����ʶ��Ĭ��ʹ��MCShell�û���\n"+
						"setUsername +username:����Ĭ�ϵ�½���û���\n"+
						"in +token(ͨ�� \"login\" ����õı�ʶ,������main,������ͼ):�л���ͼ\n"+
						"hide:���ص�ǰ��ͼ��������ʾ��\n"+
						"show:��ʾ��ǰ��ͼ��������ʾ��\n"+
						"fuck:���ٵ�ǰ��ͼ�����˳�����ʼ����(��ǿ���ж��������������)");
		}else if(c[0].equals("")){
			
		}else if(c[0].equals("show")) {
			if(main.path.equals("View-Main")) {
				cfg.println(3, "������main��ͼ��ʹ���������");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			cfg.allView.get(main.path).showC=true;
		}else if(c[0].equals("hide")) {
			if(main.path.equals("View-Main")) {
				cfg.println(3, "������main��ͼ��ʹ���������");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			cfg.allView.get(main.path).showC=false;
		}else if(c[0].equals("fuck")) {
			if(main.path.equals("View-Main")) {
				cfg.println(3, "������main��ͼ��ʹ���������");
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
				cfg.println(3, "�����ڷ�main��ͼ��ʹ���������");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			if(c.length!=3) {
				cfg.println(3, "���ݵĲ�������ȷ");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			cfg.println(1, "��ȴ�....");
			cfg.pingList(c[1],c[2]);
			return;
		}else if(c[0].equals("login")) {
			if(!main.path.equals("View-Main")) {
				cfg.println(3, "�����ڷ�main��ͼ��ʹ���������");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			if(c.length!=4) {
				cfg.println(3, "���ݵĲ�������ȷ");
				Thread.sleep(100);
				cfg.commandStop=false;
				return;
			}
			cfg.println(1, "��ȴ�....");
			View n=new View();
			n.ip=c[1];
			n.port=Integer.parseInt(c[2]);
			n.version=Integer.parseInt(c[3]);
			n.name="View-"+(cfg.allViewNum+1);
			cfg.println(2,"�ɹ�������ͼ��id:"+(cfg.allViewNum+1)+"����ʹ��\"in\"�������л���ͼ����ͼĬ��Ϊ����������ʾ����������show��ȡ������");
			cfg.allView.put(n.name, n);
			n.start();
			cfg.allViewNum++;
			return;
		}else if(c[0].equals("in")){
			if(c.length!=2) {
				cfg.println(3, "���ݵĲ�������ȷ");
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
			cfg.println(3, "δ֪������ \""+c[0]+"\"");
		}
		Thread.sleep(100);
		cfg.commandStop=false;
	}
}
