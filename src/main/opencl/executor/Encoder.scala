package opencl.executor

import java.nio.{ByteBuffer, ByteOrder}

import ir._
import opencl.ir._

/**
 * A tool to encode scala arrays into byte buffers.
 * One instance of this class can encode several arrays but they must all match
 * the type given in the constructor *and* fit into the specified allocated.
 *
 * The reason why this allocated space cannot always be retrieved from the type
 * is explained in the ScalaDoc comment at the top of `OpenCLMemoryAllocator.scala`
 *
 * @param arrayType Lift type of the values to be encoded
 * @param sizeof Allocated size in bytes
 */
class Encoder(arrayType: ArrayType, sizeof: Int) {
  /**
   * Encode an array that matches the type (and allocated size) of the
   * `Encoder` instance into a byte buffer.
   */
  def encode(array: Array[_]): ByteBuffer = {
    val buffer = ByteBuffer.allocate(sizeof)
    buffer.position(0)
    buffer.order(endianness)
    putArray(arrayType, array, buffer)
    buffer
  }

  /**
   * Main encoding function. Recursively encode the array `array` of type `ty`
   * and write it into `buffer` at its current position.
   */
  private def putArray(ty: ArrayType, array: Array[_], buffer: ByteBuffer): Unit = {
    putHeader(ty, array, buffer)
    val capacity = getCapacityOr(ty, array.length)

    ty.elemT match {
      case st: ScalarType => putScalarArray(st, capacity, array, buffer)

      case tt: TupleType => tt.elemsT.head match {
        case st: ScalarType => putScalarArray(st, capacity * tt.elemsT.length, array, buffer)
        case other => throw new NotImplementedError(s"Encoding of tuples of $other")
      }

      case VectorType(st, vLen) =>
        val array2D = array.asInstanceOf[Array[Array[_]]]
        val before = buffer.position()
        array2D.foreach {
          arr => putScalarArray(st, vLen.evalInt, arr, buffer)
        }
        buffer.position(before + capacity * vLen.evalInt * baseSize)

      case elemT: ArrayType =>
        val array2D = array.asInstanceOf[Array[Array[_]]]
        val currPos = buffer.position()
        if (elemT.hasFixedAllocatedSize) {
          array2D.foreach(putArray(elemT, _, buffer))
          val sizeOfElem = Type.getAllocatedSize(elemT).evalInt
          buffer.position(currPos + capacity * sizeOfElem)
        } else {
          val ofsSize = capacity * baseSize
          val offsets = ByteBuffer.allocate(ofsSize)
          offsets.order(endianness)
          buffer.position(currPos + ofsSize)
          array2D.foldLeft(capacity)((ofs, arr) => {
            putArray(elemT, arr, buffer)
            putIntegers(Array(ofs), offsets)
            (buffer.position() - currPos)/baseSize
          })
          val endPos = buffer.position()
          buffer.position(currPos)
          offsets.position(0)
          buffer.put(offsets)
          buffer.position(endPos)
        }

      case NoType | UndefType => throw new IllegalArgumentException(s"Cannot encode $ty")
    }
  }

  // ---
  // Some private helper functions
  // ---

  /** Write the header of an array depending on its type. */
  private def putHeader(ty: ArrayType, array: Array[_], buffer: ByteBuffer): Unit = {
    val header = Array.fill(ty.getHeaderSize)(array.length)
    putIntegers(header, buffer)
  }
  
  /** Wrapper on the top of `ByteBuffer.put` that
   *  1. Increments the position
   *  2. Call the appropriate `asXXXBuffer()` method depending on baseSize
   */
  private def putIntegers(values: Array[Int], buffer: ByteBuffer): Unit = {
    val before = buffer.position()
    baseSize match {
      case 1 => buffer.put(values.map(_.toByte))
      case 4 => buffer.asIntBuffer().put(values)
      case 8 => buffer.asLongBuffer().put(values.map(_.toLong))
      case _ => throw new IllegalArgumentException()
    }
    buffer.position(before + baseSize * values.length)
  }

  private def putScalarArray(st: ScalarType, capacity: Int,
                             array: Array[_], buffer: ByteBuffer): Unit = {
    val before = buffer.position()
    st match {
      case Int => buffer.asIntBuffer().put(array.asInstanceOf[Array[Int]])
      case Float => buffer.asFloatBuffer().put(array.asInstanceOf[Array[Float]])
      case Double => buffer.asDoubleBuffer().put(array.asInstanceOf[Array[Double]])
      case Bool => buffer.put(array.asInstanceOf[Array[Boolean]].map(b => (if (b) 1 else 0).toByte))
    }
    buffer.position(before + baseSize * capacity)
  }
  
  /** Shorthand to get the capacity of an array of fall back on its length if
   * the capacity is not in the type.
   */
  private def getCapacityOr(ty: ArrayType, fallback: Int): Int = ty match {
    case c: Capacity => c.capacity.evalInt
    case _ => fallback
  }

  val endianness = ByteOrder.LITTLE_ENDIAN // First approximation, FIXME (at some point)
  private lazy val baseType = Type.getBaseScalarType(arrayType)
  private lazy val baseSize = Type.getAllocatedSize(baseType).evalInt
}
