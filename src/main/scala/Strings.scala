package improving

trait Strings {
  /** null becomes "", otherwise identity */
  def onull(s: String): String = if (s == null) "" else s
  def onull(s: Any): String    = if (s == null) "" else s.toString

  /** Some(x) for Strings which are neither null nor "", None for those. */
  def oempty(s: String) = if (s == null || s == "") None else Some(s)
  
  def onil(xs: Any*): List[String] = xs.toList map onull filterNot (_ == "")
  
  /** String cleanup */
  def sanitize[T](s: String)(f: String => T) = f(onull(s))
  
  /** naively split on whitespace. */
  def words(s: String) = s.trim split "\\s+" filterNot (_ == "") toList
  def lines(s: String) = s.trim split "\\n" toList
  
  /** Some(x) for Ints which are >= 0, None otherwise. */
  def optIndex(index: Int) = if (index < 0) None else Some(index)
  
  /** Given Some(x), returns a safe x.toString ; "" for all others.
   */
  def optToString(opt: Option[_]) = opt map onull getOrElse ""
  
}
object Strings extends Strings
