/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2011 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving

import scala.collection.{ mutable, immutable }
import scala.tools.nsc.io.{ Directory, File, Path }
import java.io.{ ObjectInputStream, ObjectOutputStream }
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._

class DiskBackedConcurrentMap[A, B](val mapFile: File) extends mutable.ConcurrentMap[A, B] {
  private def load() = {
    if (!mapFile.exists) new ConcurrentHashMap[A, B]
    else {
      val in = new ObjectInputStream(mapFile.inputStream())
      val map = in.readObject();
      in.close()

      map.asInstanceOf[ConcurrentHashMap[A, B]]
    }
  }

  private val _map: ConcurrentHashMap[A, B] = load()

  // Map
  def get(key: A): Option[B] = Option(_map.get(key))
  def iterator: Iterator[(A, B)] = _map.entrySet().iterator map (x => (x.getKey, x.getValue))
  def +=(kv: (A, B)): this.type = { _map.put(kv._1, kv._2); this }
  def -=(key: A): this.type = { _map.remove(key); this }    
  
  // mutable.ConcurrentMap
  def putIfAbsent(k: A, v: B): Option[B] = Option(_map.putIfAbsent(k, v))
  def remove(k: A, v: B): Boolean = _map.remove(k, v)
  def replace(k: A, oldvalue: B, newvalue: B): Boolean = _map.replace(k, oldvalue, newvalue)
  def replace(k: A, v: B): Option[B] = Option( _map.replace(k, v))
    
  def store() = {
    val fstream = mapFile.bufferedOutput()
    val out = new ObjectOutputStream(fstream)
    out.writeObject(_map)
    out.close()
    fstream.close()
  }
}