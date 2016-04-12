package wordcountStopWords.wordcountStopWords;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserListMap extends Mapper<LongWritable,Text,Text,Text>{
	
	public void map(LongWritable key, Text value, Context context)
	{
		String UserID=" ";
        JSONArray UserList=null;
        String line = value.toString();
        String[] tuple = line.split("\\n");
        
        try{
            for(int i=0;i<tuple.length; i++){
                JSONObject obj = new JSONObject(tuple[i]);
                UserID = obj.getString("user_id");
                //UserList = obj.getString("friends");
                UserList =  obj.getJSONArray("friends");
               }   
            context.write(new Text(UserID), new Text(UserList.toString()));
        }catch(Exception e){System.out.println(e.getMessage());}
    
		
	}

}
