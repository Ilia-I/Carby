import java.util.ArrayList;
import java.util.List;

public class Main {

	public static void main(String[] args) {
//		Dictionary dict = new Dictionary();
		List<String> list = new ArrayList<String>();
		list.add("Energy");
		list.add("1");
		list.add(".");
		list.add("5");
		list.add("kcal");
		
		String res = "";
		for(String s: list) {
			res += s;
			if(test(s)) {
				res += " ";			
			}
		}
		System.out.println(res);
	}

	private static boolean test(String s) {
		String pattern= "\\d|\\p{Punct}";
	    return !s.matches(pattern);
	}

}
