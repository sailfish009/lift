package prog_gen

import ir.ArrayType
import lift.arithmetic._
import opencl.ir._
import org.junit._
import org.junit.Assert._

class TestInputGenerator {


  @Test
  def primitiveType(): Unit = {
    val t = Float

    assertTrue(InputGenerator()(t).isInstanceOf[Float])
  }

  @Test
  def fixedSizeArray(): Unit = {
    val t = ArrayType(Float, 32)

    val arg = InputGenerator()(t)

    assertTrue(arg.isInstanceOf[Array[Float]])
    assertEquals(32, arg.asInstanceOf[Array[Float]].length)
  }

  @Test
  def nonFixedSizeArray(): Unit = {
    val N = SizeVar("N")
    val t = ArrayType(Float, N)
    val map = Map[ArithExpr, Cst]((N, Cst(64)))

    val arg = InputGenerator(map)(t)

    assertTrue(arg.isInstanceOf[Array[Float]])
    assertEquals(map(N).eval, arg.asInstanceOf[Array[Float]].length)
  }

  @Test
  def nonFixedSize2DArray(): Unit = {
    val N = SizeVar("N")
    val M = SizeVar("M")
    val t = ArrayType(ArrayType(Float, M), N)
    val map = Map[ArithExpr, Cst]((N, Cst(64)), (M, Cst(128)))

    val arg = InputGenerator(map)(t)

    assertTrue(arg.isInstanceOf[Array[Array[Float]]])
    assertEquals(map(N).eval, arg.asInstanceOf[Array[Array[Float]]].length)
    assertEquals(map(M).eval, arg.asInstanceOf[Array[Array[Float]]].head.length)
  }


}
