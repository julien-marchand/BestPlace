package gipad.tools;

public class Test {
	
	public static void main(String[] args) {
		long lon = 166758546*100;
		System.out.println((int)lon);
		System.out.println((long)(Math.pow(2, 31)+10));
		System.out.println((int)(Math.pow(2, 31)+100.0));
		System.out.println(Math.pow(2, 31));
	}
}
