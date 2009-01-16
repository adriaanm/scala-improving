package org.improving.reflect

import scala.reflect.Manifest

object Util
{
  def tryCast[T](x: Any)(implicit mf: Manifest[T]): Option[T] = 
    if (mf.erasure.isInstance(x)) Some(x.asInstanceOf[T]) else None
}