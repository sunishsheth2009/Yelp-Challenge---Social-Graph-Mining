package com.bigdata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class UserBusinessRating {
    public static class UserBusinessRatingMapper extends Mapper<Object, Text, Text, MapWritable> {

        private Set<String> Users = new HashSet<String>();

        // initial set up to fill the hash set with stop word list
        protected void setup(Context context) throws IOException, InterruptedException {

            Configuration connection = context.getConfiguration();
            System.out.println("1. entered setup*****************************************");
            try {

                FileSystem file = FileSystem.get(new Configuration());
                // initialize buffer reader to read the file from path given in
                // the arguments
                // arg[2] userlist file path
                BufferedReader r = new BufferedReader(new InputStreamReader(file.open(new Path(connection.get("filepath")))));
                String user = connection.get("UserId");
                System.out.println("************************\n user is :: " + user + "\n*****************************************************");
                Users.add(user);
//                String line = r.readLine();
                
                String line;
                
                while((line = r.readLine()) != null){
                    String[] temp = line.split("\\s+");
//                    System.out.println("************************\n temp[0] is :: " + temp[0] + "\n*****************************************************");
                    if (temp[0].equals(user)) {

                        String friendList = temp[1];
                        StringTokenizer st = new StringTokenizer(friendList, ",\"");

                        while (st.hasMoreTokens()) {
                            String temp3 = st.nextToken();
                            if (!temp3.equals("[") && !temp3.equals("]")) {
                                // System.out.println(temp3);
                                Users.add(temp3); // add users in to hash set
                            }
                        }
//                        System.out.println("************************\n user is :: " + user +"\n***"+ Users.size() +"\n ***" + "\n*****************************************************");
                        // since the user will occur only once on the left side, we can break the look
                        break;
                    }
                }
                
                
            } catch (Exception e) {
                System.out.println("*******00000000000000000000000000000000000000000000000000" + e.getMessage());
            }
            
            System.out.println("user list " + Users.toString());
        }

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            String[] line = value.toString().split("\\s+");
            String user = line[0].trim();
            String rating = line[1].trim();
            String Bid = line[2].trim();
            
//            System.out.println("\t ^^^^^^^^^^line^^^^^^^^^^" + value.toString());
//            System.out.println("\t ^^^^^^^^^^^^^^^^^^^^ split " + user + "---" + rating + "<><><>" + Bid);
            
            if (Users.size() != 0 && Users.contains(user)) {
//                System.out.println("Contains " + user);
                MapWritable mapWritable = new MapWritable();
                String keyUser = user;

                mapWritable.put(new Text(Bid), new Text(rating));
                context.write(new Text(keyUser), mapWritable);
                
//                System.out.println("\t ^^^^^^^^^^^^^^^^^^^^" + mapWritable.values());
            }
        }
    }

    public static class UserBusinessRatingReducer extends Reducer<Text, MapWritable, Text, Text> {

        public void reduce(Text key, Iterable<MapWritable> values, Context context) throws IOException, InterruptedException {

            HashMap<String, String> Resultmap = new HashMap<String, String>();
            // value is list if HAshmap. To create new result value hashmap,
            // we need to traverse list of hashmaps and save it into result
            // hashmap

            for (MapWritable myMap : values) {
                for (Entry<Writable, Writable> ValueKey : myMap.entrySet()) {

                    String bid = ValueKey.getKey().toString();
                    String rating = ValueKey.getValue().toString();

                    Resultmap.put(bid, rating);
                }
            }

            Text Resultvalue = new Text();
            String tempval = "";

            for (Map.Entry<String, String> e : Resultmap.entrySet()) {
                if (null != e.getKey() && null != e.getValue()) {
                    tempval += e.getKey() + ":" + e.getValue() + ",";

                }
            }
            if (tempval.length() > 0)
                tempval = tempval.substring(0, tempval.length() - 1);

            Resultvalue.set(tempval);
            context.write(key, Resultvalue);
        }
    }

    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();
        conf.set("filepath", args[2]); // userlist
        conf.set("UserId", args[3]); // userid
        Job job = Job.getInstance(conf, "userlist");// Job Name

        job.setJarByClass(UserBusinessRating.class);
        job.setMapperClass(UserBusinessRatingMapper.class);
        job.setReducerClass(UserBusinessRatingReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setMapOutputValueClass(MapWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0])); // join table
        FileOutputFormat.setOutputPath(job, new Path(args[1])); // ouput path
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}
