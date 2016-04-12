package wordcountStopWords.wordcountStopWords;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.json.*;

import com.google.gson.*;

public class Shobhit_MapRed {
    public static class Map extends Mapper<LongWritable, Text, Text, Text> {
        static String totalline = new String();
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            Scanner sc = new Scanner(new File("user_small.json"));
            String str = "";
            str = sc.nextLine();
            
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
        
            
         /*   JsonElement jelement = new JsonParser().parse(str);
            JsonObject  obj1 = jelement.getAsJsonObject();
//            JSONObject obj1 = new JSONObject(str);

            Text business_id = new Text();
            NullWritable nullValue = NullWritable.get();

            //---------------------------------------------------------------------
//            String tokens = null;
//            if(value!=null){
//                tokens = value.toString();
//            }
            //if (tokens.length() > 0) {
                String userid = (obj1.get("user_id")).toString();//tokens.split(Pattern.quote("^"));
                //System.out.println(arrayofTokens[0]);
                if(userid!=null){
                    business_id.set(userid); // set word as each input keyword
                    context.write(business_id, nullValue);
                    //context.write(word, one); // create a pair <keyword, null>
                }
            //}
*/
            //---------------------------------------------------------------------
        }
    }

//    public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable> {
//        private IntWritable result = new IntWritable();
//
//        public void reduce(Text key, Iterable<IntWritable> values, Context context)
//                throws IOException, InterruptedException {
//            int sum = 0; // initialize the sum for each keyword
//            for (IntWritable val : values) {
//                sum += val.get();
//            }
//            result.set(sum);
//            context.write(key, result);
//        }
//    }
        // Driver program
        public static void main(String[] args) throws Exception
        {
            Configuration conf = new Configuration();
            String[] otherArgs = new GenericOptionsParser(conf,args).getRemainingArgs();
            // get all args
            if (otherArgs.length != 2) {
                System.err.println("Usage: Assign1_Problem1 <in> <out>");
                System.exit(2);
            }
            // create a job with name "wordcount"
            Job job = new Job (conf, "Palo Alto Count");

            job.setJarByClass(Shobhit_MapRed.class);

            Path inputFile = new Path(otherArgs[0]);
            Path outputFile = new Path(otherArgs[1]);

            job.setMapperClass(Map.class);
            //job.setReducerClass(Reduce.class);
            // uncomment the following line to add the Combiner
            //job.setCombinerClass(Reduce.class);

            job.setOutputKeyClass(Text.class);// set output key type
            job.setOutputValueClass(NullWritable.class);//set output value type
            //set the HDFS path of the input data
            FileInputFormat.addInputPath(job, inputFile);
            // set the HDFS path for the output
            FileOutputFormat.setOutputPath(job, outputFile);
            //Wait till job completion
            System.exit(job.waitForCompletion(true) ? 0 : 1);
        }

}
