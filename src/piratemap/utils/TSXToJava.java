package piratemap.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TSXToJava
{

    public static void main(String[] args) throws IOException
    {
        File f = new File("tiles/terrain.tsx");
        
        FileReader fr = new FileReader(f);
        
        BufferedReader br = new BufferedReader(fr);
        
        Pattern p1 = Pattern.compile("<tile id=\"([0-9]+)\" terrain=\"([0-9]+),([0-9]+),([0-9]+),([0-9]+)\"");
        Pattern p2 = Pattern.compile("<terrain name=\"([a-zA-Z0-9]+)\" tile=\"([0-9]+)");
        String line;
        
        while ((line = br.readLine()) != null)
        {
            
            Matcher m = p1.matcher(line);
            if (m.find())
            {
                System.out.printf("%s, %s, %s, %s, %s,\n",
                        m.group(1), m.group(2), m.group(3), m.group(4), m.group(5));
            }
            
            m = p2.matcher(line);
            if (m.find())
            {
                System.out.printf("%s, // %s\n",
                        m.group(2), m.group(1));
            }
        }
    }
}
