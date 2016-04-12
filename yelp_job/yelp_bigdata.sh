# /usr/lib - has all the installations for pig, hadoop, spark, etc

# user_id for which to process the hadoop and spark jobs
user_id=rpOyqD_893cqmDAtJLbdog;

# This script will be in the same directory, as the yelp directory.
# we are the yelp directory as our project root and will contain the data, jars and pig and hadoop jobs.

# if yelp dir is present in the user home, exit
# echo "Yelp directory is already present in the user home !!"
# echo "Remove this directory or make changes to the script" 

echo "============================="
echo "Running job for user $user_id"
echo "============================="

echo " ==> Copying files to HDFS"
# copy datasets to HDFS
hadoop fs -mkdir yelp
hadoop fs -copyFromLocal yelp_data/yelp_academic_dataset_user.json /user/cloudera/yelp/users.json
hadoop fs -copyFromLocal yelp_data/yelp_academic_dataset_review.json /user/cloudera/yelp/reviews.json

echo " ==> Copying pig dependencies to HDFS"
# copy all jar dependencies for Pig to yelp/pigPreprocess on HDFS
hadoop fs -mkdir yelp/pigPreprocess
hadoop fs -copyFromLocal ./pigPreprocess/* /user/cloudera/yelp/pigPreprocess/

echo " ==> Copying pig preprocessing script to HDFS"
# copy pig script to /user/cloudera/yelp on HDFS
hadoop fs -copyFromLocal preProcessFromJSON.pig /user/cloudera/yelp/

# Hadoop namespace for our Cloudera Distribution
# This is used in the Pig script to locate and load the dependencies
# hdfs:#quickstart.cloudera:8020/user/cloudera/yelp/pigPreProcess

# run pig script from terminal ( or from shell script using )
# when the script is running, it considers the /user/cloudera as the start point and hence
# it finds the inputs files by 'yelp/users.json'
echo " ==> Running pig script"
pig hdfs://quickstart.cloudera:8020/user/cloudera/yelp/preProcessFromJSON.pig

echo " ==> Copying Hadoop Jobs to HDFS"
# copy the Hadoop jobs (.jar) to HDFS
hadoop fs -copyFromLocal ./HadoopStep* yelp

echo " ==> Running Hadoop job 1"
# run the first Hadoop Job on the userlist
hadoop jar HadoopStep1.jar wordcountStopWords.wordcountStopWords.UserListDriver yelp/users.json yelp/hadoopStep1_output/

# echo " ==> Copying Hadoop job 1 output"
# copy the output to local directory
# hadoop fs -copyToLocal yelp/hadoopStep1_output # TODO add dir

echo " ==> Running Hadoop job 2"
# run the second Hadoop Job on the Step 1 output and userId to query
hadoop jar HadoopStep2.jar com.bigdata.UserBusinessRating yelp/joinTable1 yelp/hadoopStep2_output yelp/hadoopStep1_output/part-r-00000 $user_id


# setup for spark job
# create project dir in spark bin
echo " ==> Preparing spark project"
# recursively creates spark project directory in the current (yelp) directory
mkdir -p CFilter/src/main/scala

# copy the spark application code in the project source
cp SparkSource/CFilter.scala CFilter/src/main/scala/
cp SparkSource/CFilter.sbt CFilter

# go to CFilter directory located in the current (yelp) directory
cd CFilter

echo " ==> Building Spark job"
# now build the scala file using sbt build tool
# assuming sbt tool is located in the downloads
~/Downloads/sbt/bin/sbt package

echo " ==> Running Spark job"
# Execute Spark job
spark-submit --class "CFilter" --master local[4] target/scala-2.10/cfilter_2.10-1.0.jar $user_id > ../sparkOutput.txt

echo " ==> Final job output at sparkOutput.txt"