package wordcountStopWords.wordcountStopWords;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.omg.PortableInterceptor.USER_EXCEPTION;

public class App 
{
    public static void main( String[] args ) throws FileNotFoundException
    {
    	Scanner sc = new Scanner(new File("user_small.json"));
        //String str = "";
        String value = sc.nextLine();
        
        String UserID;
        JSONArray UserList;
        String line = value.toString();
        String[] tuple = line.split("\\n");
        
        try{
            for(int i=0;i<tuple.length; i++){
                JSONObject obj = new JSONObject(tuple[i]);
                UserID = obj.getString("user_id");
                //UserList = obj.getString("friends");
                UserList =  obj.getJSONArray("friends");
                System.out.println("user id is:"+UserID+"friend list is :" +UserList);}
        }catch(Exception e){System.out.println(e.getMessage());}
    
}
    }
