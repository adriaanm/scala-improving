package improving

trait Reflection {
  def tryCast[T](x: Any)(implicit mf: Manifest[T]): Option[T] = 
    if (mf.erasure.isInstance(x)) Some(x.asInstanceOf[T]) else None
}

object Reflection extends Reflection
