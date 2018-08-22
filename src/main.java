
public class main {
	public static String path="View-Main";
	public static void main(String[] args) {
		// TODO 自动生成的方法存根
		System.out.println("MCShell Beta-0.1 by GreyCloudTeam");
		System.out.println("------------------------WARNING------------------------");
		System.out.println("由于MCShell无法对每个版本进行支持，所以只支持下面几个版本");
		System.out.println("1.7.2,1.7.10,1.8,1.8.8(后三个版本将在以后的Beta版本进行适配");
		System.out.println("如果没有你想要的版本，请在github上留言，我们会尽快的适配");
		System.out.println("github.com/greyCloudTeam/MCShell");
		System.out.println("-------------------------------------------------------");
		System.out.println("输入 \"help\" 来获取帮助信息");
		command t=new command();
		t.start();
	}
}
