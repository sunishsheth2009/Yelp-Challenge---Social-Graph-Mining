
The Project should maintain the following structure as submitted.
Extract the source from zip and it should look like below

yelp_job
|	HadoopStep1.jar
|	HadoopStep2.jar
|	pigPreprocess
|	preProcessFromJSON.pig
|	SparkSource
|	yelp_bigdata.sh
+	yelp_data // will be empty. As input files are very big, it is not feasible to attach in the submission.


Add the following input files to the yelp_data directory. They have the same names
	
	-	yelp_academic_dataset_review.json
	-	yelp_academic_dataset_user.json


Now on HDFS, make sure that there is no 'yelp' directory in the user directory. if it exists, please delete/rename it to avoid any conflicts

Now, run the yelp_bigdata.sh using 

./yelp_bigdata.sh

The script will display the message on start of each step in the workflow and will notify when completed.

To Verify the result of the script, please look at the output file 'SparkJob_output.txt' 

This output is translated into neo4j queries which are used to populate the db with the result.
The translated file from the above out is located in the same folder 'Neo4J_insertQueries.txt'
Note : Requires neo4j installed on the system and running.