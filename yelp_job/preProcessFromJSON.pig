/*
	Pig Script to preprocess JSON and create the files
*/

/* Register the dependencies for reading json using elephant bird for hadoop */

Register hdfs://quickstart.cloudera:8020/user/cloudera/yelp/pigPreprocess/json-simple-1.1.jar;
Register hdfs://quickstart.cloudera:8020/user/cloudera/yelp/pigPreprocess/elephant-bird-pig-4.1.jar
Register hdfs://quickstart.cloudera:8020/user/cloudera/yelp/pigPreprocess/elephant-bird-core-4.1.jar
Register hdfs://quickstart.cloudera:8020/user/cloudera/yelp/pigPreprocess/elephant-bird-hadoop-compat-4.1.jar

/* Configure */
/* 
	instead of users.json we are using yelp/users.json as we are running the script directly from terminal
	otherwise, do pig -- grunt> cd yelp -- grunt> exec this_filename.pig
*/

SET elephantbird.jsonloader.nestedLoad 'true';
User = Load 'yelp/users.json' using com.twitter.elephantbird.pig.load.JsonLoader()as json:map[];
UserTable = FOREACH User GENERATE $0#'user_id'AS user_id:chararray, $0#'name' AS name:chararray;
Review = Load 'yelp/reviews.json' using com.twitter.elephantbird.pig.load.JsonLoader()as json:map[];
ReviewTable = FOREACH Review GENERATE $0#'user_id' AS user_id:chararray , $0#'stars' AS stars:int , $0#'business_id' AS business_id:chararray;

joinTable = join ReviewTable by user_id,UserTable by user_id using 'replicated';
STORE joinTable into 'yelp/joinTable1';