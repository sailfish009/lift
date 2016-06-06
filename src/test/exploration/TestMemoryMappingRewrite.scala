package exploration

import apart.arithmetic.{SizeVar, Var}
import ir._
import ir.ast._
import opencl.executor.LongTestsEnabled
import opencl.ir._
import opencl.ir.pattern._
import org.junit.Assert._
import org.junit.Test
import rewriting.utils.Utils

class TestMemoryMappingRewrite {

  LongTestsEnabled()

  private val K = SizeVar("K")
  private val M = SizeVar("M")
  private val N = SizeVar("N")

  private val v__2 = SizeVar("")
  private val v__3 = SizeVar("")
  private val v__4 = SizeVar("")
  private val v__5 = SizeVar("")
  private val v__6 = SizeVar("")
  private val v__7 = SizeVar("")

  val mmTATiled2DBlocked = fun(ArrayType(ArrayType(Float, M), K), ArrayType(ArrayType(Float, N), K),(p_0, p_1) => FunCall(Join(), FunCall(Map(fun((p_2) => FunCall(TransposeW(), FunCall(Join(), FunCall(Map(fun((p_3) => FunCall(TransposeW(), FunCall(Join(), FunCall(Map(fun((p_4) => FunCall(Map(fun((p_5) => FunCall(Scatter(ReorderWithStride(v__3 / v__4)), p_5))), FunCall(TransposeW(), FunCall(Join(), FunCall(Map(fun((p_6) => FunCall(TransposeW(), FunCall(Map(fun((p_7) => FunCall(TransposeW(), p_7))), FunCall(TransposeW(), p_6))))), FunCall(TransposeW(), p_4))))))), FunCall(TransposeW(), FunCall(MapSeq(fun((p_8) => FunCall(Id(), p_8))), FunCall(ReduceSeq(fun((p_9, p_10) => FunCall(Map(fun((p_11) => FunCall(Join(), FunCall(Map(fun((p_12) => FunCall(MapSeq(fun((p_13) => FunCall(Id(), p_13))), FunCall(ReduceSeq(fun((p_14, p_15) => FunCall(Map(fun((p_16) => FunCall(Map(fun((p_17) => FunCall(add, FunCall(Get(0), p_17), FunCall(mult, FunCall(Get(1), p_16), FunCall(Get(1), p_17))))), FunCall(Zip(2), FunCall(Get(0), p_16), FunCall(Get(1), p_15))))), FunCall(Zip(2), p_14, FunCall(Get(0), p_15))))), FunCall(Get(0), p_12), FunCall(Zip(2), FunCall(Transpose(), FunCall(Get(1), p_11)), FunCall(Transpose(), FunCall(Get(1), p_12))))))), FunCall(Zip(2), FunCall(Get(0), p_11), FunCall(Split(v__4), FunCall(Gather(ReorderWithStride(v__3 / v__4)), FunCall(Transpose(), FunCall(Get(1), p_10))))))))), FunCall(Zip(2), p_9, FunCall(Split(v__5), FunCall(Transpose(), FunCall(Get(0), p_10))))))), FunCall(Map(fun((p_18) => FunCall(Map(fun((p_19) => FunCall(Map(fun((p_20) => FunCall(Map(fun((p_21) => FunCall(idfloat, p_21))), p_20))), p_19))), p_18))), Value("0.0f", ArrayType(ArrayType(ArrayType(ArrayType(Float, v__4), v__5), v__3*1/^v__4), v__6*1/^v__5))), FunCall(Zip(2), p_2, p_3))))))))), FunCall(Transpose(), FunCall(Map(fun((p_22) => FunCall(Transpose(), p_22))), FunCall(Split(v__7), FunCall(Map(fun((p_23) => FunCall(Split(v__3), p_23))), p_1))))))))), FunCall(Transpose(), FunCall(Map(fun((p_24) => FunCall(Transpose(), p_24))), FunCall(Split(v__7), FunCall(Map(fun((p_25) => FunCall(Split(v__6), p_25))), p_0)))))))

  val mmTBMali = fun(ArrayType(ArrayType(Float, K), M), ArrayType(ArrayType(Float, K), N),(p_0, p_1) => FunCall(Join(), FunCall(Map(fun((p_2) => FunCall(TransposeW(), FunCall(Join(), FunCall(Map(fun((p_3) => FunCall(TransposeW(), FunCall(Map(fun((p_4) => FunCall(TransposeW(), p_4))), FunCall(TransposeW(), FunCall(MapSeq(fun((p_5) => FunCall(Id(), p_5))), FunCall(ReduceSeq(fun((p_6, p_7) => FunCall(Map(fun((p_8) => FunCall(Join(), FunCall(Map(fun((p_9) => FunCall(Reduce(fun((p_10, p_11) => FunCall(add, p_10, p_11))), FunCall(Get(0), p_9), FunCall(asScalar(), FunCall(Map(fun((p_12) => FunCall(VectorizeUserFun(4,mult), FunCall(Get(0), p_12), FunCall(Get(1), p_12)))), FunCall(Zip(2), FunCall(asVector(4), FunCall(Get(1), p_8)), FunCall(asVector(4), FunCall(Get(1), p_9)))))))), FunCall(Zip(2), FunCall(Get(0), p_8), FunCall(Transpose(), FunCall(Get(1), p_7))))))), FunCall(Zip(2), p_6, FunCall(Transpose(), FunCall(Get(0), p_7)))))), FunCall(Map(fun((p_13) => FunCall(Map(fun((p_14) => FunCall(idfloat, p_14))), p_13))), Value("0.0f", ArrayType(ArrayType(Float, v__3), v__4))), FunCall(Zip(2), FunCall(Split(v__5), FunCall(Transpose(), p_2)), FunCall(Split(v__5), FunCall(Transpose(), p_3)))))))))), FunCall(Split(v__3), p_1)))))), FunCall(Split(v__4), p_0))))

  val gemvAmd = fun(ArrayType(ArrayType(Float, M), N), ArrayType(Float, M), ArrayType(Float, N), Float, Float,(p_0, p_1, p_2, p_3, p_4) => FunCall(Map(fun((p_5) => FunCall(Map(fun((p_6) => FunCall(add, FunCall(mult, p_6, p_3), FunCall(mult, FunCall(Get(1), p_5), p_4)))), FunCall(Reduce(fun((p_7, p_8) => FunCall(add, p_7, p_8))), Value("0.0f", Float), FunCall(Join(), FunCall(Map(fun((p_9) => FunCall(MapSeq(fun((p_10) => FunCall(Id(), p_10))), FunCall(ReduceSeq(fun((p_11, p_12) => FunCall(add, p_11, FunCall(mult, FunCall(Get(0), p_12), FunCall(Get(1), p_12))))), FunCall(idfloat, Value("0.0f", Float)), p_9)))), FunCall(Split(M*1/^v__2), FunCall(Gather(ReorderWithStride(v__2)), FunCall(Zip(2), p_1, FunCall(Get(0), p_5)))))))))), FunCall(Zip(2), p_0, p_2)))

  def getHash(lambda: Lambda) =
    Utils.Sha256Hash(Utils.dumpLambdaToMethod(lambda))

  @Test
  def mmKepler(): Unit = {

    val keplerGold = fun(ArrayType(ArrayType(Float, M), K), ArrayType(ArrayType(Float, N), K),(p_0, p_1) => FunCall(Join(), FunCall(MapWrg(1)(fun((p_2) => FunCall(TransposeW(), FunCall(Join(), FunCall(MapWrg(0)(fun((p_3) => FunCall(TransposeW(), FunCall(Join(), FunCall(Map(fun((p_4) => FunCall(Map(fun((p_5) => FunCall(Scatter(ReorderWithStride(v__3 / v__4)), p_5))), FunCall(TransposeW(), FunCall(Join(), FunCall(Map(fun((p_6) => FunCall(TransposeW(), FunCall(Map(fun((p_7) => FunCall(TransposeW(), p_7))), FunCall(TransposeW(), p_6))))), FunCall(TransposeW(), p_4))))))), FunCall(TransposeW(), FunCall(toGlobal(fun((p_8) => FunCall(MapSeq(fun((p_9) => FunCall(MapLcl(1)(fun((p_10) => FunCall(MapLcl(0)(fun((p_11) => FunCall(MapSeq(fun((p_12) => FunCall(MapSeq(fun((p_13) => FunCall(idfloat, p_13))), p_12))), p_11))), p_10))), p_9))), p_8))), FunCall(ReduceSeq(fun((p_14, p_15) => FunCall(fun((p_16) => FunCall(MapLcl(1)(fun((p_17) => FunCall(Join(), FunCall(MapLcl(0)(fun((p_18) => FunCall(MapSeq(fun((p_19) => p_19)), FunCall(ReduceSeq(fun((p_20, p_21) => FunCall(fun((p_22) => FunCall(MapSeq(fun((p_23) => FunCall(MapSeq(fun((p_24) => FunCall(add, FunCall(Get(0), p_24), FunCall(mult, FunCall(Get(1), p_23), FunCall(Get(1), p_24))))), FunCall(Zip(2), FunCall(Get(0), p_23), FunCall(Get(1), p_22))))), FunCall(Zip(2), p_20, FunCall(Get(0), p_22)))), FunCall(toPrivate(fun((p_25) => FunCall(fun((p_26) => FunCall(Tuple(2), FunCall(MapSeq(fun((p_27) => FunCall(idfloat, p_27))), FunCall(Get(0), p_26)), FunCall(MapSeq(fun((p_28) => FunCall(idfloat, p_28))), FunCall(Get(1), p_26)))), p_25))), p_21)))), FunCall(Get(0), p_18), FunCall(Zip(2), FunCall(Transpose(), FunCall(Get(1), p_17)), FunCall(Transpose(), FunCall(Get(1), p_18))))))), FunCall(Zip(2), FunCall(Get(0), p_17), FunCall(Split(v__4), FunCall(Gather(ReorderWithStride(v__3 / v__4)), FunCall(Transpose(), FunCall(Get(1), p_16))))))))), FunCall(Zip(2), p_14, FunCall(Split(v__5), FunCall(Transpose(), FunCall(Get(0), p_16)))))), FunCall(toLocal(fun((p_29) => FunCall(fun((p_30) => FunCall(Unzip(), FunCall(MapLcl(1)(fun((p_31) => FunCall(Tuple(2), FunCall(MapLcl(0)(fun((p_32) => FunCall(idfloat, p_32))), FunCall(Get(0), p_31)), FunCall(MapLcl(0)(fun((p_33) => FunCall(idfloat, p_33))), FunCall(Get(1), p_31))))), FunCall(Zip(2), FunCall(Get(0), p_30), FunCall(Get(1), p_30))))), p_29))), p_15)))), FunCall(MapLcl(1)(fun((p_34) => FunCall(MapLcl(0)(fun((p_35) => FunCall(MapSeq(fun((p_36) => FunCall(MapSeq(fun((p_37) => FunCall(idfloat, p_37))), p_36))), p_35))), p_34))), Value("0.0f", ArrayType(ArrayType(ArrayType(ArrayType(Float, v__4), v__5), v__3*1/^v__4), v__6*1/^v__5))), FunCall(Zip(2), p_2, p_3))))))))), FunCall(Transpose(), FunCall(Map(fun((p_38) => FunCall(Transpose(), p_38))), FunCall(Split(v__7), FunCall(Map(fun((p_39) => FunCall(Split(v__3), p_39))), p_1))))))))), FunCall(Transpose(), FunCall(Map(fun((p_40) => FunCall(Transpose(), p_40))), FunCall(Split(v__7), FunCall(Map(fun((p_41) => FunCall(Split(v__6), p_41))), p_0)))))))
    val keplerHash = getHash(keplerGold)

    val mapped = MemoryMappingRewrite.lowerLambda(mmTATiled2DBlocked)
    assertTrue(mapped.exists(getHash(_) == keplerHash))
  }

  @Test
  def mmHawaii(): Unit = {

    val hawaiiGold = fun(ArrayType(ArrayType(Float, M), K), ArrayType(ArrayType(Float, N), K),(p_0, p_1) => FunCall(Join(), FunCall(MapWrg(1)(fun((p_2) => FunCall(TransposeW(), FunCall(Join(), FunCall(MapWrg(0)(fun((p_3) => FunCall(TransposeW(), FunCall(Join(), FunCall(Map(fun((p_4) => FunCall(Map(fun((p_5) => FunCall(Scatter(ReorderWithStride(v__3 / v__4)), p_5))), FunCall(TransposeW(), FunCall(Join(), FunCall(Map(fun((p_6) => FunCall(TransposeW(), FunCall(Map(fun((p_7) => FunCall(TransposeW(), p_7))), FunCall(TransposeW(), p_6))))), FunCall(TransposeW(), p_4))))))), FunCall(TransposeW(), FunCall(toGlobal(fun((p_8) => FunCall(MapSeq(fun((p_9) => FunCall(MapLcl(1)(fun((p_10) => FunCall(MapLcl(0)(fun((p_11) => FunCall(MapSeq(fun((p_12) => FunCall(MapSeq(fun((p_13) => FunCall(idfloat, p_13))), p_12))), p_11))), p_10))), p_9))), p_8))), FunCall(ReduceSeq(fun((p_14, p_15) => FunCall(fun((p_16) => FunCall(MapLcl(1)(fun((p_17) => FunCall(Join(), FunCall(MapLcl(0)(fun((p_18) => FunCall(MapSeq(fun((p_19) => p_19)), FunCall(ReduceSeq(fun((p_20, p_21) => FunCall(fun((p_22) => FunCall(MapSeq(fun((p_23) => FunCall(MapSeq(fun((p_24) => FunCall(add, FunCall(Get(0), p_24), FunCall(mult, FunCall(Get(1), p_23), FunCall(Get(1), p_24))))), FunCall(Zip(2), FunCall(Get(0), p_23), FunCall(Get(1), p_22))))), FunCall(Zip(2), p_20, FunCall(Get(0), p_22)))), FunCall(toLocal(fun((p_25) => FunCall(fun((p_26) => FunCall(Tuple(2), FunCall(MapSeq(fun((p_27) => FunCall(idfloat, p_27))), FunCall(Get(0), p_26)), FunCall(MapSeq(fun((p_28) => FunCall(idfloat, p_28))), FunCall(Get(1), p_26)))), p_25))), FunCall(toPrivate(fun((p_29) => FunCall(fun((p_30) => FunCall(Tuple(2), FunCall(Get(0), p_30), FunCall(MapSeq(fun((p_31) => FunCall(idfloat, p_31))), FunCall(Get(1), p_30)))), p_29))), p_21))))), FunCall(Get(0), p_18), FunCall(Zip(2), FunCall(Transpose(), FunCall(Get(1), p_17)), FunCall(Transpose(), FunCall(Get(1), p_18))))))), FunCall(Zip(2), FunCall(Get(0), p_17), FunCall(Split(v__4), FunCall(Gather(ReorderWithStride(v__3 / v__4)), FunCall(Transpose(), FunCall(Get(1), p_16))))))))), FunCall(Zip(2), p_14, FunCall(Split(v__5), FunCall(Transpose(), FunCall(Get(0), p_16)))))), p_15))), FunCall(MapLcl(1)(fun((p_32) => FunCall(MapLcl(0)(fun((p_33) => FunCall(MapSeq(fun((p_34) => FunCall(MapSeq(fun((p_35) => FunCall(idfloat, p_35))), p_34))), p_33))), p_32))), Value("0.0f", ArrayType(ArrayType(ArrayType(ArrayType(Float, v__4), v__5), v__3*1/^v__4), v__6*1/^v__5))), FunCall(Zip(2), p_2, p_3))))))))), FunCall(Transpose(), FunCall(Map(fun((p_36) => FunCall(Transpose(), p_36))), FunCall(Split(v__7), FunCall(Map(fun((p_37) => FunCall(Split(v__3), p_37))), p_1))))))))), FunCall(Transpose(), FunCall(Map(fun((p_38) => FunCall(Transpose(), p_38))), FunCall(Split(v__7), FunCall(Map(fun((p_39) => FunCall(Split(v__6), p_39))), p_0)))))))
    val hawaiiHash = getHash(hawaiiGold)

    val mapped = MemoryMappingRewrite.lowerLambda(mmTATiled2DBlocked)
    assertTrue(mapped.exists(getHash(_) == hawaiiHash))
  }

  @Test
  def mmVectorLoads(): Unit = {

    val vectorLoadsGold = fun(ArrayType(ArrayType(Float, M), K), ArrayType(ArrayType(Float, N), K),(p_0, p_1) => FunCall(Join(), FunCall(MapWrg(1)(fun((p_2) => FunCall(TransposeW(), FunCall(Join(), FunCall(MapWrg(0)(fun((p_3) => FunCall(TransposeW(), FunCall(Join(), FunCall(Map(fun((p_4) => FunCall(Map(fun((p_5) => FunCall(Scatter(ReorderWithStride(v__3 / v__4)), p_5))), FunCall(TransposeW(), FunCall(Join(), FunCall(Map(fun((p_6) => FunCall(TransposeW(), FunCall(Map(fun((p_7) => FunCall(TransposeW(), p_7))), FunCall(TransposeW(), p_6))))), FunCall(TransposeW(), p_4))))))), FunCall(TransposeW(), FunCall(toGlobal(fun((p_8) => FunCall(MapSeq(fun((p_9) => FunCall(MapLcl(1)(fun((p_10) => FunCall(MapLcl(0)(fun((p_11) => FunCall(MapSeq(fun((p_12) => FunCall(MapSeq(fun((p_13) => FunCall(idfloat, p_13))), p_12))), p_11))), p_10))), p_9))), p_8))), FunCall(ReduceSeq(fun((p_14, p_15) => FunCall(fun((p_16) => FunCall(MapLcl(1)(fun((p_17) => FunCall(Join(), FunCall(MapLcl(0)(fun((p_18) => FunCall(MapSeq(fun((p_19) => p_19)), FunCall(ReduceSeq(fun((p_20, p_21) => FunCall(fun((p_22) => FunCall(MapSeq(fun((p_23) => FunCall(MapSeq(fun((p_24) => FunCall(add, FunCall(Get(0), p_24), FunCall(mult, FunCall(Get(1), p_23), FunCall(Get(1), p_24))))), FunCall(Zip(2), FunCall(Get(0), p_23), FunCall(Get(1), p_22))))), FunCall(Zip(2), p_20, FunCall(Get(0), p_22)))), FunCall(toPrivate(fun((p_25) => FunCall(fun((p_26) => FunCall(Tuple(2), FunCall(MapSeq(fun((p_27) => FunCall(idfloat, p_27))), FunCall(Get(0), p_26)), FunCall(MapSeq(fun((p_28) => FunCall(idfloat, p_28))), FunCall(Get(1), p_26)))), p_25))), p_21)))), FunCall(Get(0), p_18), FunCall(Zip(2), FunCall(Transpose(), FunCall(Get(1), p_17)), FunCall(Transpose(), FunCall(Get(1), p_18))))))), FunCall(Zip(2), FunCall(Get(0), p_17), FunCall(Split(v__4), FunCall(Gather(ReorderWithStride(v__3 / v__4)), FunCall(Transpose(), FunCall(Get(1), p_16))))))))), FunCall(Zip(2), p_14, FunCall(Split(v__5), FunCall(Transpose(), FunCall(Get(0), p_16)))))), FunCall(toLocal(fun((p_29) => FunCall(fun((p_30) => FunCall(Unzip(), FunCall(MapLcl(1)(fun((p_31) => FunCall(Tuple(2), FunCall(asScalar(), FunCall(MapLcl(0)(fun((p_32) => FunCall(VectorizeUserFun(4,idfloat), p_32))), FunCall(asVector(4), FunCall(Get(0), p_31)))), FunCall(asScalar(), FunCall(MapLcl(0)(fun((p_33) => FunCall(VectorizeUserFun(4,idfloat), p_33))), FunCall(asVector(4), FunCall(Get(1), p_31))))))), FunCall(Zip(2), FunCall(Get(0), p_30), FunCall(Get(1), p_30))))), p_29))), p_15)))), FunCall(MapLcl(1)(fun((p_34) => FunCall(MapLcl(0)(fun((p_35) => FunCall(MapSeq(fun((p_36) => FunCall(MapSeq(fun((p_37) => FunCall(idfloat, p_37))), p_36))), p_35))), p_34))), Value("0.0f", ArrayType(ArrayType(ArrayType(ArrayType(Float, v__4), v__5), v__3*1/^v__4), v__6*1/^v__5))), FunCall(Zip(2), p_2, p_3))))))))), FunCall(Transpose(), FunCall(Map(fun((p_38) => FunCall(Transpose(), p_38))), FunCall(Split(v__7), FunCall(Map(fun((p_39) => FunCall(Split(v__3), p_39))), p_1))))))))), FunCall(Transpose(), FunCall(Map(fun((p_40) => FunCall(Transpose(), p_40))), FunCall(Split(v__7), FunCall(Map(fun((p_41) => FunCall(Split(v__6), p_41))), p_0)))))))
    val vectorLoadsHash = getHash(vectorLoadsGold)

    val mapped = MemoryMappingRewrite.lowerLambda(mmTATiled2DBlocked)
    assertTrue(mapped.exists(getHash(_) == vectorLoadsHash))
  }

  @Test
  def mmMali(): Unit = {

    val maliGold = fun(ArrayType(ArrayType(Float, K), M), ArrayType(ArrayType(Float, K), N),(p_0, p_1) => FunCall(Join(), FunCall(MapGlb(0)(fun((p_2) => FunCall(TransposeW(), FunCall(Join(), FunCall(MapGlb(1)(fun((p_3) => FunCall(TransposeW(), FunCall(Map(fun((p_4) => FunCall(TransposeW(), p_4))), FunCall(TransposeW(), FunCall(toGlobal(fun((p_5) => FunCall(MapSeq(fun((p_6) => FunCall(MapSeq(fun((p_7) => FunCall(MapSeq(fun((p_8) => FunCall(idfloat, p_8))), p_7))), p_6))), p_5))), FunCall(ReduceSeq(fun((p_9, p_10) => FunCall(fun((p_11) => FunCall(MapSeq(fun((p_12) => FunCall(Join(), FunCall(MapSeq(fun((p_13) => FunCall(MapSeq(fun((p_14) => p_14)), FunCall(ReduceSeq(fun((p_15, p_16) => FunCall(add, p_15, p_16))), FunCall(Get(0), p_13), FunCall(asScalar(), FunCall(MapSeq(fun((p_17) => FunCall(VectorizeUserFun(4,mult), FunCall(Get(0), p_17), FunCall(Get(1), p_17)))), FunCall(Zip(2), FunCall(asVector(4), FunCall(Get(1), p_12)), FunCall(asVector(4), FunCall(Get(1), p_13))))))))), FunCall(Zip(2), FunCall(Get(0), p_12), FunCall(Transpose(), FunCall(Get(1), p_11))))))), FunCall(Zip(2), p_9, FunCall(Transpose(), FunCall(Get(0), p_11))))), p_10))), FunCall(MapSeq(fun((p_18) => FunCall(MapSeq(fun((p_19) => FunCall(idfloat, p_19))), p_18))), Value("0.0f", ArrayType(ArrayType(Float, v__3), v__4))), FunCall(Zip(2), FunCall(Split(v__5), FunCall(Transpose(), p_2)), FunCall(Split(v__5), FunCall(Transpose(), p_3)))))))))), FunCall(Split(v__3), p_1)))))), FunCall(Split(v__4), p_0))))
    val maliHash = getHash(maliGold)

    val mapped = MemoryMappingRewrite.lowerLambda(mmTBMali)
    assertTrue(mapped.exists(getHash(_) == maliHash))
  }

  @Test
  def gemv(): Unit = {

    val gemvAmdGold = fun(ArrayType(ArrayType(Float, M), N), ArrayType(Float, M), ArrayType(Float, N), Float, Float,(p_0, p_1, p_2, p_3, p_4) => FunCall(MapWrg(0)(fun((p_5) => FunCall(toGlobal(fun((p_6) => FunCall(MapLcl(0)(fun((p_7) => FunCall(add, FunCall(toLocal(fun((p_8, p_9) => FunCall(mult, p_8, p_9))), p_7, p_3), FunCall(toLocal(fun((p_10, p_11) => FunCall(mult, p_10, p_11))), FunCall(Get(1), p_5), p_4)))), p_6))), FunCall(MapSeq(fun((p_12) => FunCall(toLocal(fun((p_13) => FunCall(idfloat, p_13))), p_12))), FunCall(ReduceSeq(fun((p_14, p_15) => FunCall(add, p_14, p_15))), FunCall(idfloat, Value("0.0f", Float)), FunCall(Join(), FunCall(MapLcl(0)(fun((p_16) => FunCall(MapSeq(fun((p_17) => FunCall(toLocal(fun((p_18) => FunCall(idfloat, p_18))), p_17))), FunCall(ReduceSeq(fun((p_19, p_20) => FunCall(fun((p_21) => FunCall(add, p_19, FunCall(mult, FunCall(Get(0), p_21), FunCall(Get(1), p_21)))), p_20))), FunCall(idfloat, Value("0.0f", Float)), p_16)))), FunCall(Split(M*1/^v__2), FunCall(Gather(ReorderWithStride(v__2)), FunCall(Zip(2), p_1, FunCall(Get(0), p_5))))))))))), FunCall(Zip(2), p_0, p_2)))
    val gemvAmdHash = getHash(gemvAmdGold)

    val mapped = MemoryMappingRewrite.lowerLambda(gemvAmd)
    assertTrue(mapped.exists(getHash(_) == gemvAmdHash))
  }

}