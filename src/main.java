import java.io.FileInputStream;
import java.util.Properties;

public class main {
	public static String path="View-Main";
	//public static String command=File.readFile_string(File.getPath()+"\\command.mcs");
	//public static String login=File.readFile_string(File.getPath()+"\\login.mcs");
	public static void main(String[] args) {
		// TODO 閼奉亜濮╅悽鐔稿灇閻ㄥ嫭鏌熷▔鏇炵摠閺嶏拷
		System.out.println("MCShell Beta-0.1 by GreyCloudTeam");
		System.out.println("------------------------WARNING------------------------");
		System.out.println("由于mc每个版本的协议都不一样，暂时只支持下面几个版本");
		System.out.println("1.7.2,1.7.10,1.8,1.8.8(后面3个版本暂时没有支持，在以后的更新中会支持)");
		System.out.println("如果没有你想要的版本，可以在github上申请适配，我们会尽快的适配你提出的版本");
		System.out.println("github.com/greyCloudTeam/MCShell");
		System.out.println("-------------------------------------------------------");
		System.out.println("输入 \"help\" 来获取帮助信息");
		//System.out.println(acceptPack.toChat("{\"extra\":[{\"color\":\"white\",\"text\":\"[\"},{\"color\":\"green\",\"text\":\"垃圾废铁\"},{\"color\":\"white\",\"text\":\"] [\"},{\"bold\":true,\"color\":\"green\",\"text\":\"生存世界\"},{\"color\":\"white\",\"text\":\"]\\u003c\"},{\"color\":\"dark_green\",\"text\":\"qodobopo\"},\"\\u003e xty\"],\"text\":\"\"}"));
		command t=new command();
		t.start();
	}
}
