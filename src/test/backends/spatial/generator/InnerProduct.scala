package backends.spatial.generator

import backends.c.host.host_ir.{OclFunc, ToGPU, ToHost}
import backends.{Backend, c, spatial}
import ir._
import ir.ast._
import ir.ast.debug.AssertType
import lift.arithmetic.SizeVar
import opencl.executor.TestWithExecutor
import org.junit.Assert._
import org.junit.Test

object InnerProduct extends TestWithExecutor

class InnerProduct {

  @Test
  def openclDotProduct(): Unit = {
    import opencl.ir._
    import opencl.ir.pattern._

    val N = 16
    val input: Array[Float] = (0 until N).toArray.map(_.toFloat)
    val gold: Float = (input, input).zipped.map(_*_).sum

    val commonCodeOutputPath = System.getProperty("user.dir") + "/src/test/backends/spatial/host"

    val scalarDotLambda: Lambda = fun(
      ArrayType(Float, N),
      ArrayType(Float, N),
      (x, y) =>
        toGlobal(MapSeq(id)) o
          ReduceSeq(add, 0.0f) o
          MapSeq(mult) $ Zip(x, y)
    )

    //    val code = opencl.executor.Compile(scalarDotLambda)
    //
    //    val (output, _) = Execute(1, 1)[Array[Float]](code, scalarDotLambda, input, input)
    //    println("OUT: " + output.head.toString)

    val codeOutputPath = s"$commonCodeOutputPath/00.OpenCLScalarDot"
    val hostCodeFileName = "opencl_scalar_dot_host.cpp"

    val hostingLambda = fun(
      ArrayType(Float, N),
      ArrayType(Float, N),
      (x, y) =>
        ToHost() $ OclFunc(scalarDotLambda, cpu_timer = true, gpu_timer = true)(ToGPU(x), ToGPU(y))
    )

    c.global.GlobalCompiler(hostingLambda, codeOutputPath, List(hostCodeFileName))

    val actualOutput: String = backends.c.common.executor.Executor.native_compile_and_run(
      codeOutputPath, hostCodeFileName)

    print(actualOutput)

    val pattern = raw"(?:.*\n)+(\d+).*\n".r

    val pattern(count) = actualOutput.stripMargin
    assertEquals(gold, count.toFloat, 0.001f)
  }

  @Test
  def openclDotProductTiled(): Unit = {
    import opencl.ir._
    import opencl.ir.pattern._

    val N = 64 //16

    val tileSize = 32 //4
    val outerParFactor = 2
    val innerParFactor = 16

    val input: Array[Float] = (0 until N).toArray.map(_.toFloat)
    val gold: Float = (input, input).zipped.map(_*_).sum

    val commonCodeOutputPath = System.getProperty("user.dir") + "/src/test/backends/spatial/host"

    val scalarDotLambdaTiled: Lambda = fun(
      ArrayType(Float, N),
      ArrayType(Float, N),
      (a, b) =>
        toGlobal(MapSeq(id)) o
          ReduceSeq(add, 0.0f) o
          Join() o
          MapSeq(fun(tile =>
            // Similar to the Spatial example
//            ReduceSeq(add, /*toGlobal(id) $ */0.0f) o MapSeq(mult) o
//              toLocal(MapSeq(fun(p => Tuple(id(p._0), id(p._1))))) $ tile)) o
            // Functionally correct (there is a bug in the compiler to do with overwriting the accumulator on each
            // iteration of the outer MapSeq
            ReduceSeq(add, toGlobal(id) $ 0.0f) o MapSeq(mult) $ tile)) o
          Split(tileSize) $ Zip(a, b)
    )

    val codeOutputPath = s"$commonCodeOutputPath/01.OpenCLTiledScalarDot"
    val hostCodeFileName = "opencl_tiled_scalar_dot_host.cpp"

    val hostingLambda = fun(
      ArrayType(Float, N),
      ArrayType(Float, N),
      (x, y) =>
        ToHost() $ OclFunc(scalarDotLambdaTiled, cpu_timer = true, gpu_timer = true)(ToGPU(x), ToGPU(y))
    )

    print(opencl.executor.Compile(scalarDotLambdaTiled))
    c.global.GlobalCompiler(hostingLambda, codeOutputPath, List(hostCodeFileName))

    val actualOutput: String = backends.c.common.executor.Executor.native_compile_and_run(
      codeOutputPath, hostCodeFileName)

    print(actualOutput)

    val pattern = raw"(?:.*\n)+(\d+).*\n".r

    val pattern(count) = actualOutput.stripMargin
    assertEquals(gold, count.toFloat, 0.001f)
  }

  @Test
  def spatialDotProduct(): Unit = {
    import backends.spatial.common.ir._
    import backends.spatial.host

    val N = 1024
    val input: Array[Float] = (0 until 16).toArray.map(_.toFloat)
    val gold: Float = (input, input).zipped.map(_*_).sum

    val commonCodeOutputPath = System.getProperty("user.dir") + "/src/test/backends.spatial/host"

    val scalarDotLambda: Lambda = null
//      fun(
//      ArrayType(Float, N),
//      ArrayType(Float, N),
//      (x, y) =>
//        toGlobal(MapSeq(id)) o
//          ReduceSeq(add, 0.0f) o
//          MapSeq(mult) $ Zip(x, y)
//    )

    val expectedOutCode = """
       Accel {
        // Allocate local SRAMs
        val s1 = SRAM[T](len)
        val s2 = SRAM[T](len)

        // Transfer data
        s1 load d1
        s2 load d2
  
        // Multiply and accumulate
        out := Reduce(Reg[T](0))(len by 1) { i =>
          s1(i) * s2(i)
        }(_ + _)
      }"""

    val runTimeLambda: Lambda = fun(
      ArrayType(Float, N),
      ArrayType(Float, N),
      (x, y) =>
        host.ir.ast.AccelFun(scalarDotLambda)(x, y)
    )

    val out = spatial.common.RuntimeCompiler(runTimeLambda)
    
  }

  @Test
  def spatialDotProductTiled(): Unit = {
    import backends.spatial.accel.ir._
    import backends.spatial.accel.ir.pattern.{SpFold, toSRAM}
    import backends.spatial.common.ir._
    import backends.spatial.host.ir.ast.AccelFun
    Backend.setSpatial()

    val N = SizeVar("N") //1024
    val tileSize = SizeVar("tileSize") //32
    val outerParFactor = SizeVar("outerParFactor") //2
    val innerParFactor = SizeVar("innerParFactor") //16
//    val N = 1024
//    val tileSize = 32
//    val outerParFactor = 2
//    val innerParFactor = 16

    val expectedOutCode = """
      Accel {
        out := Reduce(Reg[T](0.to[T]))(N by tileSize par outerParFactor){i =>
          val aBlk = SRAM[T](tileSize)
          val bBlk = SRAM[T](tileSize)
          Parallel {
            aBlk load a(i::i+tileSize par innerParFactor)
            bBlk load b(i::i+tileSize par innerParFactor)
          }
          Reduce(Reg[T](0.to[T]))(ts par innerParFactor){ii => aBlk(ii) * bBlk(ii) }{_+_}
        }{_+_}
      }
    """


    val idArray = UserFun("idArray", Array("arr"),
      "arr", Seq(ArrayType(Float, tileSize)), ArrayType(Float, tileSize)) // TODO: generalise array size


    val scalaDotLambdaTiled: Lambda = fun(
      ArrayType(Float, N),
      ArrayType(Float, N),
      (a, b) =>
        SpFold(chunkSize = tileSize, stride = tileSize, factor = outerParFactor,
          fMap = fun(

            ArrayType(TupleType(Float, Float), tileSize), tileAB => {
              val tileA = Get(Unzip() $ tileAB, 0)
              val tileB = Get(Unzip() $ tileAB, 1)

              val tileABsram = Zip(
                /*Parallel(*/
                toSRAM(idArray) $ tileA,
                toSRAM(idArray) $ tileB
                /*)*/)

              SpFold(chunkSize = 1, stride = 1, factor = innerParFactor,
                fMap = backends.spatial.accel.ir.pattern.MapSeq(mult),
                fReduce = add,
                init = Value(0.0f, Float)) $ tileABsram
            }),
          fReduce = add,
          init = Value(0.0f, Float)) $
          Zip(a, b))

    val dotProductRuntimeLambda = fun(
      ArrayType(Float, N),
      ArrayType(Float, N),
      (a, b) =>
        AccelFun(scalaDotLambdaTiled)(a, b))

    backends.spatial.common.RuntimeCompiler(dotProductRuntimeLambda)
  }

  @Test
  def spatialGEMMTiled(): Unit = {
    import backends.spatial.accel.ir.pattern.{SpForeach, SpMemFold, toDRAM, toSRAM}
    import backends.spatial.common.ir._
    import backends.spatial.accel.ir._

    val M = 128
    val P = 64
    val N = 96
    val outerFactorI = 2
    val outerFactorJ = 2
    val outerFactorK = 2
    val tileMsize = 32
    val tileNsize = 32
    val tileParFactor = 16
    val innerFactorI = 1
    val innerFactorJ = 1

    val idArray2d = UserFun("idArray", Array("arr"),
      "arr", Seq(ArrayType(ArrayType(Float, tileNsize), tileMsize)),
      ArrayType(ArrayType(Float, tileNsize), tileMsize)) // TODO: generalise array size

    val idArray1d = UserFun("idArray", Array("arr"),
      "arr", Seq(ArrayType(Float, tileMsize)), ArrayType(Float, tileMsize)) // TODO: generalise array size

    val gemmTiled: Lambda = fun(
      ArrayType(ArrayType(Float, P), M),
      ArrayType(ArrayType(Float, P), N),
      ArrayType(ArrayType(Float, N), M),
      (a, b, c) =>
        toDRAM(SpForeach(
          chunkSize = tileMsize, stride = tileMsize, factor = outerFactorI,
          f = fun(
            ArrayType(TupleType(ArrayType(Float, P), ArrayType(Float, N)), tileMsize),
            tileACrows => {
              val tileArows =
                AssertType(ArrayType(ArrayType(Float, tileMsize), P), "tileArows.type") o
                  Transpose() $ Get(Unzip() $ tileACrows, 0)
              val tileCrows =
                AssertType(ArrayType(ArrayType(Float, tileMsize), N), "tileCrows.type") o
                  Transpose() $ Get(Unzip() $ tileACrows, 1)
              SpForeach(chunkSize = tileNsize, stride = tileNsize, factor = outerFactorJ,
                f = fun(
                  ArrayType(TupleType(ArrayType(Float, P), ArrayType(Float, tileMsize)), tileNsize),
                  tileBcolsC => {

                    val tileBcols =
                      AssertType(ArrayType(ArrayType(Float, tileNsize), P), "tileBcols.type") o
                        Transpose() $ Get(Unzip() $ tileBcolsC, 0)
                    val tileCsram =
                      AssertType(ArrayType(ArrayType(Float, tileNsize), tileMsize), "tileCsram.type") o
                        toSRAM(idArray2d) o Transpose() $ Get(Unzip() $ tileBcolsC, 1)

                    SpMemFold(chunkSize = tileNsize, stride = tileNsize, factor = outerFactorK,
                      fMap = fun(
                        ArrayType(TupleType(ArrayType(Float, tileMsize), ArrayType(Float, tileNsize)), tileNsize),
                        tileAB => {
                          val tileA = AssertType(ArrayType(ArrayType(Float, tileMsize), tileNsize), "tileA") $
                            Get(Unzip() $ tileAB, 0)
                          val tileBsram = AssertType(ArrayType(ArrayType(Float, tileNsize), tileNsize), "tileBsram") o
                            toSRAM(idArray2d) $ Get(Unzip() $ tileAB, 1)

                          SpForeach(chunkSize = 1, stride = 1, factor = innerFactorI,
                            f = fun(
                              // TODO: get rid of the extra dimension of 1 element
                              ArrayType(TupleType(ArrayType(Float, tileNsize), ArrayType(Float, tileNsize)), 1),
                              tileRowABsram => {
                                val tileRowAsram = AssertType(ArrayType(Float, tileNsize), "tileRowAsram") o
                                  toSRAM(idArray1d) o Join() $ Get(Unzip() $ tileRowABsram, 0)
                                val tileRowBsram = AssertType(ArrayType(Float, tileNsize), "tileRowBsram") o
                                  Join() $ Get(Unzip() $ tileRowABsram, 1)

                                SpForeach(
                                  chunkSize = 1,
                                  stride = 1,
                                  factor = innerFactorJ,
                                  f = fun(Float, elBsram =>
                                    /*Pipe {*/
                                    SpMemFold(chunkSize = 1, stride = 1, factor = tileParFactor,
                                      fMap = fun(Float, elAsram => mult(elBsram, elAsram)),
                                      fReduce = add, init = Value(0.0f, Float)
                                    ) $ tileRowAsram
                                    /*}*/
                                  )) $ tileRowBsram
                              })) $ Zip(tileA, tileBsram)
                        }),
                      fReduce = add, init = tileCsram
                    ) $ Zip(tileArows, tileBcols)
                  })) $ Zip(b, tileCrows)
            }))) $ Zip(a, c))

    val expectedOutCode = """  
      Accel {

        Foreach(M by tileMsize par outerFactorI,
                N by tileNsize par outerFactorJ) { (i, j) =>

          val tileC = SRAM[T](tileMsize, tileNsize).buffer
          tileC load c_dram(i::i+tileMsize, j::j+tileNsize par tileParFactor)

          MemFold(tileC par tileParFactor)(P by tileMsize par outerFactorK) { k =>

            val tileCaccum = SRAM[T](tileMsize, tileNsize)

            val bSRAM      = SRAM[T](tileNsize, tileNsize)
            bSRAM load b_dram(k::k+tileNsize, j::j+tileNsize par tileParFactor)

            Foreach(tileMsize by 1 par innerFactorI) { ii =>

              val aSRAM = SRAM[T](tileNsize)

              aSRAM load a_dram(i+ii, k::k+tileNsize par tileParFactor)

              Foreach(tileNsize by 1 par innerFactorJ) { jj =>

                Pipe {
                  tileCaccum(ii,jj) = Reduce(Reg[T])(tileMsize by 1 par tileParFactor) { kk =>
                    bSRAM(kk, jj) * aSRAM(kk)
                  }{_+_}
                }
              }
            }
            tileCaccum
          }{_+_}

          cDRAM(i::i+tileNsize, j::j+tileMsize par tileParFactor) store tileC
        }
      }
    """
  }


  // TODO: remove the scribbles about views

  //                                       x(a/s * s, b)   x(a, b)
  // ViewAccess(ViewMap( ViewAccess(ViewMap( ViewSplit(ViewMem())))))
  //    i                    j               i*s + j

  //                                         x(s, a/s, b)   x(a/s * s, b)   x(a, b)
  // ViewAccess(ViewMap( ViewAccess(ViewMap( ViewTranspose( ViewSplit(ViewMem()))))))
  //    j                    i               i, j           i*s + j

  //                     x(a*b.len + b)    x(a, b)
  // ViewAccess(ViewMap( ViewJoin(       ViewMem()))))
  // i                    i/b.len, i%b.len
}