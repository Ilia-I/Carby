import java.util.ArrayList;
import java.util.List;

public class Main {

	public static void main(String[] args) {
//		Dictionary dict = new Dictionary();
		List<String> list = new ArrayList<String>();
		list.add("Typical per 100g per serving");//1
		list.add("Typical per 100g per serving RI");//2
		list.add("Typical per 100g per serving 6R7");//3
		list.add("Typical per 100g per serving %RI");//4
		list.add("Typical per 100g per serving %6I");//5
		
		for(String s: list) {
			if(test(s)) {
				System.out.println(s);		
			}
		}
		
	}

	private static boolean test(String s) {
		String pattern= ""
				+ "(?s).*\\p{Space}.{0,1}RI.*"//2,4
				+ "|(?s).*\\p{Space}\\p{Punct}\\wI.*"//4,5
				+ "|(?s).*\\p{Punct}R\\w.*"//4
				;
	    return s.matches(pattern);
	}

}
