package piratemap.generate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import piratemap.generate.RoboMap.Tile;

public class TemplateRules
{
    final String[] rules = new String[] {
      "common", "",
      "tree", "at the $treedesc tree|at the tree shaped like $animal| at the tree with a $animal painted on",
      "treedesc", "burned | blighted | charred | split",
      "animal", " monkey | tiger | lion | parrot ",
      "sand", " $stone | $stone | $stone | $wreck ", // Poor mans probability
      "stone", " at the $color stone | at the stone that looks like a $animal | at the stone with $number scratch marks"
              + " | at the stone marked with a $mark",
      "color", "white | black | red | yellow ",
      "number", "two | three | four | five",
      "wreck", "at the old shack | at the broken shack | at the old post",
      "hill", "through the $valleydesc valley | through the $cavedesc cave",
      "valleydesc", "bright | dark | mossy | windy",
      "cavedesc", "bright | dark | mossy | windy",
      "mark", " sun | moon | star | flower | heart | $animal",
      
      "start", "at $wreck | at the sunken boat | at the seaweed jungle | at the old turtle nest | at the shady cove"
    };
    
    HashMap<String, String[]> map = new HashMap<>();
    
    public TemplateRules()
    {
        for (int i = 0; i+1 < rules.length; i+=2)
        {
            String ruleName = rules[i].trim();
            
            String[] rhs = rules[i+1].split("\\|");
            
            String[] rhs1 = new String[rhs.length];
            for (int j = 0; j < rhs.length; j++)
            {
                rhs1[j] = rhs[j].trim();
            }
            
            map.put(ruleName, rhs1);
        }
    }
    
    Random rand = new Random();
    Pattern p = Pattern.compile("\\$([a-zA-Z_0-9]+)");
    public String getStr(String rule)
    {
        String[] rhs = map.get(rule);
        if (rhs == null)
        {
            System.err.println("No rule for "+rule);
            return "[]";
        }
        
        String rhs1 = rhs[rand.nextInt(rhs.length)];
        
//        System.out.println("Processing "+rhs1);
        
        Matcher m = p.matcher(rhs1);
        while(m.find())
        {
            String var = m.group(1);
         
//            System.out.println("Found "+m.group(0));
            
            String replace = getStr(var);
            
            rhs1 = m.replaceFirst(replace);
            m = p.matcher(rhs1);
        }
        
        return rhs1;
    }
    
    public String getStrFor(Tile t)
    {
        switch (t)
        {
        case SAND:
            return getStr("sand");
        case HILL:
            return getStr("hill");
        case TREES:
            return getStr("tree");
        default:
            System.err.println("Dont know description for tile "+t);
            return "";
        }
    }
    
    public static void main(String[] args)
    {
        TemplateRules t = new TemplateRules();
        
        for (int i = 0; i < 100; i++)
            System.out.println(t.getStr("hill"));
    }
}
