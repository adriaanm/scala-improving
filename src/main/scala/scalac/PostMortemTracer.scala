/* NSC -- new Scala compiler
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Paul Phillips
 */

package improving

import PostMortemTracer._
import scala.collection.{ mutable, immutable }

/** A class which stores all the calls to indicated methods
 *  without outputting anything until the jvm shuts down.
 *  This way we can see what's going on during those sensitive
 *  moments when printing types tends to break everything.
 */
class PostMortemTracer {
  scala.sys addShutdownHook show()
  private val startTime = System.nanoTime
  
  def quieter(s: String) = List(
    """\Qscala.collection.\E""" -> "sc."
  ).foldLeft(s) { case (s, (from, to)) => s.replaceAll(from, to) }

  def show() {
    println("Elapsed time: " + ((System.nanoTime - startTime) / 1000000L) + " ms.")
    
    ( calls.groupBy(_.method).toList 
          map { case (k, calls) => (k, { val xs = calls map (_.pos.nanos) ; (xs.size, xs.sum) }) }
       sortBy { case (_, (size, _)) => size }
          map { case (k, (size, sum)) => "%20s: %s calls / %s ms".format(k, size, sum.toDouble / 1000000d) }
      foreach println
    )

    calls.sorted foreach { call =>
      pending get call.pos.in foreach { finishing =>
        for (x <- finishing.reverse)
          println(x.outString)

        pending -= call.pos.in
      }
      println(quieter("" + call))
    }
    // should be at most one outstanding
    for (xs <- pending.values ; x <- xs)
      println(x.outString)
  }

  private val pendingDepth      = mutable.HashMap[String, Int]() withDefaultValue 0
  private val pending           = mutable.HashMap[Int, List[Call]]() withDefaultValue Nil // open calls not yet completed
  private val finished          = mutable.HashSet[Int]()        // completed calls
  private var currentPointer    = 0                             // index of the next new call
  private var trailingPointer   = 0                             // index of the oldest call in progress
  private var calls: List[Call] = Nil                           // list of all calls

  /** Records a method call for display after shutdown.
   */
  def apply[T](pf: PartialFunction[T, Any])(method: String, args: Any*)(body: => T): T = {
    val in        = currentPointer
    val active    = trailingPointer until in filterNot finished
    val recursive = pendingDepth contains method

    currentPointer += 1
    val startTime = if (recursive) 0 else System.nanoTime
    val result    = body
    val endTime   = if (recursive) 0 else System.nanoTime

    finished += in
    val out = currentPointer

    while (finished(trailingPointer))
      trailingPointer += 1

    if (pf isDefinedAt result) {
      val pos = CallPos(in, out, active.size, endTime - startTime)
      val newCall = new Call(pos, method, args.toList, pf(result))
      if (!pos.isSimple) {
        pending(pos.out) ::= newCall
        pendingDepth(method) += 1
      }
      calls ::= newCall
    }
    result
  }
}

object PostMortemTracer {
  private case class CallPos(
    in: Int,    // pointer when call is made 
    out: Int,   // pointer when call returns
    depth: Int, // nesting/indentation depth
    nanos: Long // nanos spent in call
  ) {
    // no visible calls between in and out
    def isSimple = in + 1 == out
    def spaces = "  " * depth
    override def toString = spaces + in + (
      if (isSimple) "" else "/" + out
    )
    override def equals(other: Any) = other match {
      case x: CallPos => in == x.in
      case _          => false
    }
    override def hashCode = in
  }

  private class Call(
    val pos: CallPos,           // metadata
    val method: String,         // called method
    val args: List[Any],        // arguments
    val result: Any             // return value
  ) extends Ordered[Call] {

    import pos._

    def compare(other: Call) = pos.in compare other.pos.in
    def stringify(value: Any): String = value match {
      case xs: Traversable[_] if xs.isEmpty => "Nil"
      case x                                => "" + x
    }
    def argsString = args map stringify mkString ", "
    def resultString = stringify(result)

    def inString = (
      "%s: %s(%s) == %s".format(
        spaces + in, method, argsString,
        if (isSimple) resultString else "<" + out + ">"
      )
    )
    def outString = spaces + "<" + in + "> " + resultString
    override def toString = inString
    override def equals(other: Any) = other match {
      case x: Call  => pos == x.pos
      case _        => false
    }
    override def hashCode = pos.hashCode
  }
}
