package improving
package benchmark

import scala.collection.mutable.ListBuffer

trait Benchmark {
  private def coinflip(): Boolean = math.abs(util.Random.nextInt()) % 2 == 0
  
  case class Run[+T](result: T, millis: Long)
  case class RaceResult(time1: Long, time2: Long) {
    def speedup = math.abs(time2 - time1) * 100 / (time2 max time1)
    def winner  = if (time2 > time1) "First" else "Second"
    
    def resultString = {
      if (time1 == time2 || speedup == 0) "Virtual tie"
      else "%s body %d%% faster.".format(winner, speedup)
    }
    override def toString = resultString
  }
  case class Race[+T, +U](f1: () => T, f2: () => U) {
    /** Keeps dialing up the repetitions until the same speedup is
     *  seen on three consecutive races.
     */
    def converge() = {
      val startReps = 2
      val results = new ListBuffer[RaceResult]
      def lastThree = results takeRight 3 map (_.speedup)
      
      def nextReps(reps: Int): Int = {
        if (reps < 64) reps * 2
        else (reps * 6 / 5)
      }
      def loop(reps: Int): (Int, RaceResult) = {
        results += this(reps)
        
        if (results.size >= 3 && lastThree.distinct.size == 1)
          (reps, results.last)
        else
          loop(nextReps(reps))
      }

      loop(startReps)
    }
    
    def multitime(reps: Int)(body: => Unit): Long = {
      var total: Long = 0
      var index = 0
      while (index < reps) {
        val t1 = System.currentTimeMillis
        body
        val t2 = System.currentTimeMillis
        total += (t2 - t1)
        index += 1
      }
      total
    }
    
    def apply(reps: Int) = {
      /** Ham-fisted attempt to avoid first-run bias. */
      val (time1, time2) = {
        if (coinflip()) {
          val t1 = multitime(reps)(f1())
          val t2 = multitime(reps)(f2())
          (t1, t2)
        }
        else {
          val t2 = multitime(reps)(f2())
          val t1 = multitime(reps)(f1())
          (t1, t2)
        }
      }
      
      RaceResult(time1, time2)
    }
  }
  
  def time[T](body: => T): Run[T] = {
    val t1 = System.currentTimeMillis
    val res = body
    val t2 = System.currentTimeMillis
    
    Run(res, t2 - t1)
  }
  
  /** Keeps dialing up the repetitions until the same speedup is
   *  seen on three consecutive races.
   */
  def converge[T, U](body1: => T, body2: => U) = {
    val (reps, res) = race(body1, body2).converge()
    println("Convergence at " + reps + " repetitions.")
    res
  }

  def race[T,U](body1: => T, body2: => U): Race[T, U] =
    Race(() => body1, () => body2)
}

object Benchmark extends Benchmark