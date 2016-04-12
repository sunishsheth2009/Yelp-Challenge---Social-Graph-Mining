import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object CFilter {
  def main(args: Array[String]) {
    val conf = new SparkConf()
    conf.setAppName("CFilter")
    conf.set("spark.storage.memoryFraction", "1");
    val sc = new SparkContext(conf)

    // Load and parse the data
    val data = sc.textFile("yelp/hadoopStep2_output/part-r-00000")


    val dataRow = data.map( d => (d.split('\t')(0), d.split('\t')(1)))

    val UserSumCount = dataRow.map (line => (line._1, line._2.split(',')
                 .map(eachRating => (eachRating.split(':')(1).toDouble, 1))
                 .reduce((x, y) => (x._1 + y._1, x._2 + y._2))
                 ))

    val average = UserSumCount.map(eachUser => (eachUser._1, eachUser._2._1/eachUser._2._2))
    val sample = data.filter( d => (d.split("\\s+")(0).contains( args(0))))
    //.map(line => line.split("\\s+")(0), line.split("\\s+")(1))
    // println("---------------------")
    // sample.foreach(println)
    // println("---------------------")
    val mainUser = sample.map( d => (d.split("\\s+")(0), d.split("\\s+")(1))).take(1)
    val secondaryRows = data.mapPartitionsWithIndex { (idx, iter) => if (idx == 0) iter.drop(1) else iter }
    val otherUsers = secondaryRows.map( d => (d.split("\\s+")(0), d.split("\\s+")(1)))

    val mainUserRatingCount = mainUser(0)._2.split(',').size
    val r = scala.util.Random
    val randomNumber = r.nextInt(mainUserRatingCount)
    val mainUserRatings = mainUser(0)._2.split(',')
    val mainUserRatingsForTesting = mainUserRatings(randomNumber)
    val mainUserRatingsForProcessing = mainUserRatings.filter(! _.contains(mainUserRatingsForTesting)).mkString(",")

    val expectedRating = mainUserRatingsForTesting.split(":")(1)
    val expectedKey = mainUserRatingsForTesting.split(":")(0)

    val similarity = otherUsers.map(friends => (mainUser(0)._1,friends._1,calculateSimilarity(mainUserRatingsForProcessing,friends._2)))

    val weightMatrix = similarity.map(line => (line._2,line._3))

    //sc.parallelize()

    val friendsRatings = otherUsers.map( line => (line._1, line._2.split(",").filter(_.contains(expectedKey))))
    val friendsUserRatingsForTesting = friendsRatings.filter(line => line._2.size > 0).map( line => (line._1, line._2(0).split(":")(1).toDouble))

    similarity.foreach(println)
    //friendsUserRatingsForTesting.foreach(println)
    //average.foreach(println)

    val friendUserList = friendsUserRatingsForTesting.map(line => line._1).toArray

    val expectedUserAverageRating = average.filter(line => !friendUserList.contains(line._1)).map(line => line._2).take(1)

    val averageMinusActualRating = friendsUserRatingsForTesting.union(average).filter(line => friendUserList.contains(line._1)).reduceByKey(_-_)
    val SimRatingForEachUser = weightMatrix.union(averageMinusActualRating).filter(line => friendUserList.contains(line._1)).reduceByKey(_*_)
    val summation = SimRatingForEachUser.map(line => line._2).reduce(_+_)

    val kWeights = weightMatrix.filter(line => friendUserList.contains(line._1)).map(line => line._2).reduce(_+_)

    val addingFactor = (summation.toDouble/kWeights.toDouble)
    val calculatedRating = addingFactor + expectedUserAverageRating(0).toDouble
    val UserID = mainUser.map(line => line._1)
    println(UserID.mkString(",")+" -> "+expectedKey.mkString("")+":"+expectedRating+":"+calculatedRating)

    println("RMSE ------->" + Math.abs(calculatedRating.toDouble - expectedRating.toDouble))
  }

  def calculateSimilarity(user: String, friend: String): Double = {
    var similarity = 0.0

    val userRatings = user.split(',').map( eachRating => (eachRating.split(':')(0), eachRating.split(':')(1).toDouble))
    val friendRatings = friend.split(',').map( eachRating => (eachRating.split(':')(0), eachRating.split(':')(1).toDouble))

    val userRatingsCount = userRatings.size
    val friendRatingsCount = friendRatings.size

    val userBussiness = userRatings.map(line => line._1)
    val friendBussiness = friendRatings.map(line => line._1)
    val userMean = userRatings.map(line => line._2).reduce(_+_)/userRatingsCount
    var friendMean = friendRatings.map(line => line._2).reduce(_+_)/friendRatingsCount
    val intersectRatings = userBussiness.intersect(friendBussiness)
    val intersectCount = intersectRatings.size.toDouble
    val unionCount = (userRatingsCount + friendRatingsCount - intersectCount).toDouble

    val distanceMeasureOne = intersectCount/unionCount

    val unionRatings = userRatings.union(friendRatings)

    if( intersectCount > 0 ){
      val filteredCommonData = unionRatings.filter(line => intersectRatings.contains(line._1))
      val numerator = filteredCommonData.groupBy(_._1).map( line => ((userMean - line._2(0)._2) * (friendMean - line._2(1)._2))).reduce(_+_)
      val denominatorPartOne = filteredCommonData.groupBy(_._1).map( line => ((userMean - line._2(0)._2) * (friendMean - line._2(0)._2))).reduce(_+_)
      val denominatorPartTwo = filteredCommonData.groupBy(_._1).map( line => ((userMean - line._2(1)._2) * (friendMean - line._2(1)._2))).reduce(_+_)
      val denominator = Math.sqrt(denominatorPartOne * denominatorPartTwo)
      //val res2 = filteredCommonData.groupBy(_._1).map( line => (line._2.map(line=>line._2).reduce(_-_)))
      //val res3 = res2.map(line => Math.pow(line,2)).reduce(_+_).toDouble
      //val distanceMeasureTwo = (1/(1+Math.sqrt(res3)))
      var distanceMeasureTwo = 0.0
      if(denominator > 0){
        distanceMeasureTwo = numerator/denominator
      }else{
        distanceMeasureTwo = userMean
      }
      similarity = distanceMeasureTwo
      //similarity = 0.1 * distanceMeasureOne + 0.9 * distanceMeasureTwo
    }
    return similarity
  }

}


//val finalemp =
  //userRatingsCount.join(friendRatingsCount).
  //map { case((nk1,nk2) ,((parts1), (val1))) => (nk1,parts1,val1) }
