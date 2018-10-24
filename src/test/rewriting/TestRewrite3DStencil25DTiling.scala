package rewriting

import ir.ast.{Slide2D, Slide3D, Transpose, fun, _}
import ir.{ArrayType, ArrayTypeWSWC}
import lift.arithmetic.SizeVar
import opencl.executor._
import opencl.generator.stencil.MapSeqSlideHelpers
import opencl.generator.stencil.acoustic.StencilUtilities
import opencl.ir._
import opencl.ir.pattern.{MapGlb, toPrivate, _}
import org.junit.Assert._
import org.junit._
import rewriting.rules.Rules
import rewriting.utils.NumberExpression

object TestRewrite3DStencil25DTiling extends TestWithExecutor

class TestRewrite3DStencil25DTiling
{

  val O = 2 + SizeVar("O")
  val N = 2 + SizeVar("N")
  val M = 2 + SizeVar("M")

  def original1DStencil(size: Int, step: Int) = fun(
    ArrayTypeWSWC(Float, N),
    (input) =>
     MapSeq(MapSeq(id)) o MapSeq(
        fun(neighbours => {
          toGlobal(MapSeqUnroll(id)) o ReduceSeq(absAndSumUp,0.0f) $ neighbours
        } )) o Slide(size,step) $ input
  )

  def stencil1D(a: Int ,b :Int) = fun(
    ArrayTypeWSWC(Float, N),
    (input) =>
      toGlobal(MapSeqSlide(MapSeqUnroll(id) o ReduceSeqUnroll(absAndSumUp,0.0f), a,b)) $ input
  )

  def jacobi(m: Param) = {
    val `tile[1][1][1]` = m.at(1).at(1).at(1)
    val `tile[0][1][1]` = m.at(0).at(1).at(1)
    val `tile[1][0][1]` = m.at(1).at(0).at(1)
    val `tile[1][1][0]` = m.at(1).at(1).at(0)
    val `tile[1][1][2]` = m.at(1).at(1).at(2)
    val `tile[1][2][1]` = m.at(1).at(2).at(1)
    val `tile[2][1][1]` = m.at(2).at(1).at(1)

    val stencil = toPrivate(fun(x => add(x, `tile[0][1][1]`))) o
      toPrivate(fun(x => add(x, `tile[1][0][1]`))) o
      toPrivate(fun(x => add(x, `tile[1][1][0]`))) o
      toPrivate(fun(x => add(x, `tile[1][1][2]`))) o
      toPrivate(fun(x => add(x, `tile[1][1][1]`))) o
      toPrivate(fun(x => add(x, `tile[1][2][1]`))) $ `tile[2][1][1]`

    toGlobal(id) $ stencil
  }

  /** 1D **/
  @Test
  def reduceSlide1DTestSize3Step1(): Unit = {

    val slidesize = 3
    val slidestep = 1
    val size = 100
    val values = Array.tabulate(size) { (i) => (i + 1).toFloat }
    val gold = values.sliding(slidesize,slidestep).toArray.map(x => x.reduceLeft(_ + _))

    //DotPrinter.withNumbering("/home/reese/scratch/","MSSrewrite",original1DStencil(3,1),true)
    println(NumberExpression.breadthFirst(original1DStencil(3,1)).mkString("\n\n"))
    val rewriteStencil1D = Rewrite.applyRuleAtId(original1DStencil(3,1),0,Rules.mapSeqSlide)
    println(rewriteStencil1D)

    val (output : Array[Float], _) = Execute(2, 2)[Array[Float]](MapSeqSlideHelpers.stencil1D(slidesize, slidestep), values)
    assertArrayEquals(gold, output, 0.1f)

  }

  @Test
  def stencil3DJacobiMSSComparisons(): Unit = {

    val localDimX = 10
    val localDimY = 10
    val localDimZ = 6

    val slidesize = 3
    val slidestep = 1

    val data = StencilUtilities.createDataFloat3DInOrder(localDimX, localDimY, localDimZ)
    val stencilarr3D = data.map(x => x.map(y => y.map(z => Array(z))))
    val stencilarrpadded3D = StencilUtilities.createDataFloat3DWithPaddingInOrder(localDimX, localDimY, localDimZ)


    val m = SizeVar("M")
    val n = SizeVar("N")
    val o = SizeVar("O")



    def jacobi3D(a: Int, b: Int) = fun(
      ArrayTypeWSWC(ArrayTypeWSWC(ArrayTypeWSWC(Float, o+2), n+2), m+2),
      (mat) => {
        MapGlb(2)(MapGlb(1)(MapGlb(0)(fun(m => {
          jacobi(m)
        })))) o Slide3D(a,b) $ mat
      })

    def jacobi3DHighLevel(a: Int, b: Int) = fun(
      ArrayType(ArrayType(ArrayType(Float, o+2), n+2), m+2),
      mat => {
        Map(Map(Map(fun(m => jacobi(m))))) o
          // Slide3D
          Map(Map(Transpose()) o Transpose()) o
          Slide(a, b) o Map(Map(Transpose()) o Slide(a, b) o Map(Slide(a, b))) $ mat
      })

    def jacobi3DmapseqslideHighLevel(a : Int, b : Int) = fun(
      ArrayType(ArrayType(ArrayType(Float, o+2),n+2),m+2),
      mat =>
        Map(Map( fun(x => {
          MapSeqSlide( fun(m => jacobi(m)), a,b)
        } o Transpose() o Map(Transpose()) $ x

        ))) o
          // Slide2D
          Map(Transpose()) o Slide(a, b) o Map(Slide(a, b)) $ mat)


    def jacobi3Dmapseqslide(a : Int, b : Int) = fun(
      ArrayTypeWSWC(ArrayTypeWSWC(ArrayTypeWSWC(Float, o+2),n+2),m+2),
      (mat) =>
        MapGlb(1)(MapGlb(0)( fun (x => {
          toGlobal(MapSeqSlide(fun(m => {
            jacobi(m)
          }),a,b))  } o Transpose() o Map(Transpose()) $ x

        ))) o Slide2D(a,b) $ mat)


    val (output_org: Array[Float], _) = Execute(2,2,2,2,2,2, (true,true))[Array[Float]](jacobi3D(slidesize,slidestep), stencilarrpadded3D)
    val (output_MSS: Array[Float], _) = Execute(2,2,2,2,2,2, (true,true))[Array[Float]](jacobi3Dmapseqslide(slidesize,slidestep), stencilarrpadded3D)

    assertArrayEquals(output_MSS, output_org, StencilUtilities.stencilDelta)

  }

  @Test
  def stencil3DJacobiMSSComparisonsWithRewriteRule(): Unit = {

    val localDimX = 10
    val localDimY = 10
    val localDimZ = 6

    val slidesize = 3
    val slidestep = 1

    val data = StencilUtilities.createDataFloat3DInOrder(localDimX, localDimY, localDimZ)
    val stencilarr3D = data.map(x => x.map(y => y.map(z => Array(z))))
    val stencilarrpadded3D = StencilUtilities.createDataFloat3DWithPaddingInOrder(localDimX, localDimY, localDimZ)


    val m = SizeVar("M")
    val n = SizeVar("N")
    val o = SizeVar("O")


    def jacobi3D(a: Int, b: Int) = fun(
      ArrayTypeWSWC(ArrayTypeWSWC(ArrayTypeWSWC(Float, o+2), n+2), m+2),
      (mat) => {
        MapGlb(2)(MapGlb(1)(MapGlb(0)(fun(m => {
          jacobi(m)
        })))) o Slide3D(a,b) $ mat
      })

    def jacobi3DMapSeqSlideWithRule(a : Int, b : Int) = fun(
      ArrayTypeWSWC(ArrayTypeWSWC(ArrayTypeWSWC(Float, o+2),n+2),m+2),
      (mat) => mat)


    val (output_org: Array[Float], _) = Execute(2,2,2,2,2,2, (true,true))[Array[Float]](jacobi3D(slidesize,slidestep), stencilarrpadded3D)
    val (output_MSS: Array[Float], _) = Execute(2,2,2,2,2,2, (true,true))[Array[Float]](jacobi3DMapSeqSlideWithRule(slidesize,slidestep), stencilarrpadded3D)

    assertArrayEquals(output_MSS, output_org, StencilUtilities.stencilDelta)

  }

  @Test
  def stencil3DJacobiComparisonsCoalescedWithPadConstant(): Unit = {

    val localDimX = 8
    val localDimY = 6
    val localDimZ = 4

    val slidesize = 3
    val slidestep = 1

    val data = StencilUtilities.createDataFloat3DInOrder(localDimX, localDimY, localDimZ)
    val stencilarr3D = data.map(x => x.map(y => y.map(z => Array(z))))
    val stencilarrpadded3D = StencilUtilities.createDataFloat3DWithPaddingInOrder(localDimX, localDimY, localDimZ)
    val stencilarrOther3D = stencilarrpadded3D.map(x => x.map(y => y.map(z => z * 2.0f)))

    val m = SizeVar("M")
    val n = SizeVar("N")
    val o = SizeVar("O")

    val Nx = localDimX
    val Ny = localDimY
    val Nz = localDimZ

    def jacobi3D(a: Int, b: Int) = fun(
      ArrayTypeWSWC(ArrayTypeWSWC(ArrayTypeWSWC(Float, Nx),Ny),Nz),
      (mat) => {
        MapGlb(2)(MapGlb(1)(MapGlb(0)(fun(m => {
            jacobi(m)
        })))) o Slide3D(a,b) o PadConstant3D(1,1,1,0.0f) $ mat
      })

    def jacobi3DMapSeqSlide(a : Int, b : Int) = fun(
      ArrayTypeWSWC(ArrayTypeWSWC(ArrayTypeWSWC(Float, Nx),Ny),Nz),
      (mat) =>
        Map(TransposeW()) o TransposeW() o Map(TransposeW()) o
          MapGlb(0)(MapGlb(1)( fun (x => {
            toGlobal(MapSeqSlide(fun(m => {
              jacobi(m)
            }),a,b))  } o Transpose() o Map(Transpose()) $ x
          ))) o Slide2D(a,b) o Map(Transpose())  o Transpose() o Map(Transpose()) o PadConstant3D(1,1,1,0.0f) $ mat)



    val (output_org: Array[Float], _) = Execute(2,2,2,2,2,2, (true,true))[Array[Float]](jacobi3D(slidesize,slidestep), data)
    val (output_MSS: Array[Float], _) = Execute(2,2,2,2,2,2, (true,true))[Array[Float]](jacobi3DMapSeqSlide(slidesize,slidestep), data)


    assertArrayEquals(output_MSS, output_org, StencilUtilities.stencilDelta)

  }

  @Test
  def stencil3DJacobiComparisonsCoalescedWithPadConstantWithRewriteRule(): Unit = {

    val localDimX = 8
    val localDimY = 6
    val localDimZ = 4

    val slidesize = 3
    val slidestep = 1

    val data = StencilUtilities.createDataFloat3DInOrder(localDimX, localDimY, localDimZ)
    val stencilarr3D = data.map(x => x.map(y => y.map(z => Array(z))))
    val stencilarrpadded3D = StencilUtilities.createDataFloat3DWithPaddingInOrder(localDimX, localDimY, localDimZ)
    val stencilarrOther3D = stencilarrpadded3D.map(x => x.map(y => y.map(z => z * 2.0f)))

    val m = SizeVar("M")
    val n = SizeVar("N")
    val o = SizeVar("O")

    val Nx = localDimX
    val Ny = localDimY
    val Nz = localDimZ

    def jacobi3D(a: Int, b: Int) = fun(
      ArrayTypeWSWC(ArrayTypeWSWC(ArrayTypeWSWC(Float, Nx),Ny),Nz),
      (mat) => {
        MapGlb(2)(MapGlb(1)(MapGlb(0)(fun(m => {
          jacobi(m)
        })))) o Slide3D(a,b) o PadConstant3D(1,1,1,0.0f) $ mat
      })

    def jacobi3DMapSeqSlideWithRewriteRule(a : Int, b : Int) = fun(
      ArrayTypeWSWC(ArrayTypeWSWC(ArrayTypeWSWC(Float, Nx),Ny),Nz),
      (mat) => mat)

    val (output_org: Array[Float], _) = Execute(2,2,2,2,2,2, (true,true))[Array[Float]](jacobi3D(slidesize,slidestep), data)
    val (output_MSS: Array[Float], _) = Execute(2,2,2,2,2,2, (true,true))[Array[Float]](jacobi3DMapSeqSlideWithRewriteRule(slidesize,slidestep), data)


    assertArrayEquals(output_MSS, output_org, StencilUtilities.stencilDelta)

  }

}
