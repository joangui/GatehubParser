import model.Transaction;
import parser.GatehubParser;

import java.io.*;
import java.util.List;
import java.util.Properties;


public class GatehubParserInterface {

    public static void main(String [] args)
    {
        Properties prop = new Properties();
        InputStream input = null;

        try {
            String filename = args[0];
            input = new FileInputStream(filename);
            // load a properties file
            prop.load(input);

            // get the property value and print it out
            GatehubParser ghp = new GatehubParser(prop);
            List<Transaction> transactionsHistory = ghp.run();

            ghp.serializeTransactionsHistory(transactionsHistory);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if( input!=null)
                try {
                input.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
        }
        System.out.println("Done.");
    }
}
