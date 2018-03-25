import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {

	public static void main(String[] args) {
//		Dictionary dict = new Dictionary();
		List<String> listOfContents = new ArrayList<>();
        listOfContents.add("Energy");
        listOfContents.add("Fat");
        listOfContents.add("mono-unsaturates");
        listOfContents.add("polyunsaturates");
        listOfContents.add("saturates");
        listOfContents.add("of which mono-unsaturates");
        listOfContents.add("of which polyunsaturates");
        listOfContents.add("of which saturates");
        listOfContents.add("Carbohydrate");
        listOfContents.add("sugars");
        listOfContents.add("polyols");
        listOfContents.add("starch");
        listOfContents.add("of which sugars");
        listOfContents.add("of which polyols");
        listOfContents.add("of which starch");
        listOfContents.add("Fibre");
        listOfContents.add("Protein");
        listOfContents.add("Salt");
		
        List<String> input = new ArrayList<>();
        input.add("ugar 0.5g 15g 5%");
        input.add("Fat 23g 5.8g 8%");
		input.add("of which saturates 1.8g 0.5g 3%"); 
		input.add("Storage Information");
		input.add("Typical per 100g per serving RI"); 
		input.add("Protel 7.7g 1.9g 4%");
		input.add("Fibr 6.3g 1.6g");
		input.add("For rnor e information visit www.lldl.co.uk or www.lidl.ie"); 
		input.add("Energy 2035k/486kcal 515k/123kcal ");
		input.add("Ingred ");
		input.add("of which sugars 21g 5.3g 6%"); 
		input.add("values 25g ");
		input.add("Carbohydrate 59g 15g 6%"); 
		input.add("Nutrition Information ");
		input.add("lt 1.1g 0.28 5% ");
		input.add("Nutrition ");
		input.add("mon0-unsaturates 16g 4.0g"); 
		input.add("This pack contains 8servings"); 
		input.add("sReference intake of an average adult (8400kJ/2000kcal)"); 
		input.add("polyunsaturates 5.4g 1.4g ");
        
        
        for(String in:input){
        	
        	String[] tokens = in.split(" ");
        	System.out.println("\n-----------\nbefore:"+in);
        	String comp = "";
        	String rest = "";
        	boolean col1 = true;
            for (String token : tokens) {
                if(col1 && !isVal(token)) {
                	comp+=token+" ";
                } else {
                	rest+=token+" ";
                	col1=false;
                }
            }
            comp = comp.trim();
            rest = rest.trim();
            System.out.println("after:"+comp);
        	
	        List<Double> dist = new ArrayList<>();
			for(String s: listOfContents) {
				dist.add(similarity(s,comp));
//				System.out.println("difference between: "+s+" and "+comp+" = "+similarity(s,comp));
			}
			System.out.println(comp+" closest:"+listOfContents.get(dist.indexOf(Collections.max(dist)))+" s:"+Collections.max(dist));
			if(Collections.max(dist)>=0.5) {
				in = listOfContents.get(dist.indexOf(Collections.max(dist))) +" "+ rest;
			}
			System.out.println(in);
        }
        
	}
	
	private static double similarity(String expected, String compared) {
		  String longer = expected, shorter = compared;
		  if (expected.length() < compared.length()) { // longer should always have greater length
		    longer = compared; shorter = expected;
		  }
		  int longerLength = longer.length();
		  if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
		  LevenshteinDistance ld = new LevenshteinDistance(expected.length());//(int)(expected.length()/2)+1);
		  System.out.println("lev:"+ld.apply(longer, shorter)+" "+ld.apply(shorter,longer));
		  int distance = ld.apply(longer, shorter);
		  if(distance<0) return 0;
		  return (longerLength - distance) / (double) longerLength;
	}

	private static boolean isVal(String s) {
		String pattern= "\\d+(\\.\\d)?(\\p{ASCII})*";
	    return s.matches(pattern);
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
