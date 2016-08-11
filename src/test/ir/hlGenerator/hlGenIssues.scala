package ir.hlGenerator


import ir._
import ir.ast._
import ir.interpreter.Interpreter
import opencl.executor.{Compile, Execute, Executor}
import org.junit._
import opencl.ir._
import opencl.ir.pattern.toGlobal
import rewriting.{EnabledMappings, Lower}

import scala.language.reflectiveCalls

object hlGenIssues{
  @BeforeClass def before(): Unit = {
    Executor.loadLibrary()
    println("Initialize the executor")
    Executor.init()
  }

  @AfterClass def after(): Unit = {
    println("Shutdown the executor")
    Executor.shutdown()
  }
}
class hlGenIssues{
  @Ignore @Test def IssuePrivateMemoryNotEvaluable():Unit={
    val f = fun(
      Float,
      ArrayType(ArrayType(Float,32),32),
      Float,
      ArrayType(Float,32),
      ArrayType(Float,1),
      (p99,p102,p226,p239,p1901) =>{
        Map(fun((p24) =>
          Join() o Map(fun((p157) =>
            Reduce(fun((p20,p195)=>
              add(p20,p195)
            ))(add(p24,p99),p157)
          )) $ p102
        )) $ p1901/*(Reduce(fun((p215,p49) =>
          add(p215,p49)
        ))(add(p226,p226),p239))*/
      }
    )
    val fs = Lower.mapCombinations(f,new EnabledMappings(true, false, false, false, false, false))
    TypeChecker(fs.head)
    val code = Compile(fs.head)
    val Args = scala.collection.mutable.ArrayBuffer[Any]()
    for (j <- f.params.indices) {
      f.params(j).t match {
        case ArrayType(ArrayType(Float, l1), l2) =>
          Args += Array.tabulate(l1.eval, l2.eval)((r, c) => 1.0f)
        case ArrayType(Float, l1) =>
          Args += Array.fill(l1.eval)(2.0f)
        case Float =>
          Args += 3.0f
        case _=>
      }
    }
    val output_int = Interpreter(f).->[Vector[Vector[Float]]].runAndFlatten(Args:_*).toArray[Float]
    val(output_exe:Array[Float],_)= Execute(1,32)(code,fs.head,Args:_*)
    assert(output_exe.corresponds(output_int)(_==_))
  }
  @Ignore @Test def IssuePrivateMemoryNotEvaluable1():Unit={
    val f = fun(
      Float,
      ArrayType(ArrayType(Float,32),32),
      Float,
      (p99,p102,p38) => {
        Join()(Map(fun((p183)=>
          Map(fun((p157) =>
            Reduce(fun((p20,p195) =>
              add(p20,p195)
          ))(add(p183,p99),p157)
          ))(p102)
        ))((Reduce(fun((p113,p3) =>
          add(p113,p3)
        )))(p38 , Join() $ p102)))
      }
    )
    val fs = Lower.mapCombinations(f,new EnabledMappings(true, false, false, false, false, false))
    TypeChecker(fs.head)
    val code = Compile(fs.head)
    val Args = scala.collection.mutable.ArrayBuffer[Any]()
    for (j <- f.params.indices) {
      f.params(j).t match {
        case ArrayType(ArrayType(Float, l1), l2) =>
          Args += Array.tabulate(l1.eval, l2.eval)((r, c) => 1.0f)
        case ArrayType(Float, l1) =>
          Args += Array.fill(l1.eval)(2.0f)
        case Float =>
          Args += 3.0f
        case _=>
      }
    }
    val output_int = Interpreter(f).->[Vector[Vector[Float]]].runAndFlatten(Args:_*).toArray[Float]
    val(output_exe:Array[Float],_)= Execute(1,32)(code,fs.head,Args:_*)
    assert(output_exe.corresponds(output_int)(_==_))
  }
  @Ignore @Test def IssueWriteIntoReadonlyMemory():Unit={
    val f = fun(
      ArrayType(Float,32),
      ArrayType(Float,32),
      (p101,p241) =>{
        Map(fun((p66) =>
          Reduce(fun((p171,p223)=>
            add(p171,p223)
          ))(p66,p101)
        ))(p241)
      }
    )
    val fs = Lower.mapCombinations(f,new EnabledMappings(true, false, false, false, false, false))
    TypeChecker(fs.head)
    val code = Compile(fs.head)
    val Args = scala.collection.mutable.ArrayBuffer[Any]()
    for (j <- f.params.indices) {
      f.params(j).t match {
        case ArrayType(ArrayType(Float, l1), l2) =>
          Args += Array.tabulate(l1.eval, l2.eval)((r, c) => 1.0f)
        case ArrayType(Float, l1) =>
          Args += Array.fill(l1.eval)(2.0f)
        case Float =>
          Args += 3.0f
        case _=>
      }
    }
    val output_int = Interpreter(f).->[Vector[Vector[Float]]].runAndFlatten(Args:_*).toArray[Float]
    val(output_exe:Array[Float],_)= Execute(1,32)(code,fs.head,Args:_*)
    assert(output_exe.corresponds(output_int)(_==_))

  }
  //fixed
  @Test def IssueWriteIntoReadonlyMemory1():Unit={
    val f = fun(
      Float,
      ArrayType(ArrayType(Float,32),32),
      Float,
      (p226,p243,p99) =>{
        Map(fun((p16) =>
          Reduce(fun((p230,p12) =>
            add(p230,p12)
          ))(add(p226,p16),Join() $ p243)
        ))(Reduce(fun((p230,p12) =>
          add(p230,p12)
        ))(add(p226,p99),Join() $ p243))
      }
    )
    val fs = Lower.mapCombinations(f,new EnabledMappings(true, false, false, false, false, false))
    TypeChecker(fs.head)
    val code = Compile(fs.head)
    val Args = scala.collection.mutable.ArrayBuffer[Any]()
    for (j <- f.params.indices) {
      f.params(j).t match {
        case ArrayType(ArrayType(Float, l1), l2) =>
          Args += Array.tabulate(l1.eval, l2.eval)((r, c) => 1.0f)
        case ArrayType(Float, l1) =>
          Args += Array.fill(l1.eval)(2.0f)
        case Float =>
          Args += 3.0f
        case _=>
      }
    }
    val output_int = Interpreter(f).->[Vector[Vector[Float]]].runAndFlatten(Args:_*).toArray[Float]
    val(output_exe:Array[Float],_)= Execute(1,1)(code,fs.head,Args:_*)
    assert(output_exe.corresponds(output_int)(_==_))

  }
  @Ignore @Test def issue76(): Unit ={
    val f = fun(
      Float,
      ArrayType(Float,32),
      (p236,p116) =>{
        Map(fun((p200) =>
          add(p236,add(p236,p200))
        )) $ p116
      })
    val fs = Lower.mapCombinations(f,new EnabledMappings(true, false, false, false, false, false))
    val code = Compile(fs.head)
    val Args = scala.collection.mutable.ArrayBuffer[Any]()
    for (j <- f.params.indices) {
      f.params(j).t match {
        case ArrayType(ArrayType(Float, l1), l2) =>
          Args += Array.tabulate(l1.eval, l2.eval)((r, c) => 1.0f)
        case ArrayType(Float, l1) =>
          Args += Array.fill(l1.eval)(2.0f)
        case Float =>
          Args += 3.0f
        case _=>
      }
    }
    val output_int = Interpreter(f).->[Vector[Float]].run(Args:_*).toArray[Float]
    val(output_exe:Array[Float],_)= Execute(1,1024)(code,fs.head,Args:_*)
    assert(output_exe.corresponds(output_int)(_==_))
  }
  @Ignore @Test def issue78(): Unit={
    val f = fun(
      Float,
      ArrayType(Float,32),
      (p252,p174) =>{
        Map(fun((p30) =>
          add(p252,p30)
        ))(Reduce(fun((p89,p156) =>
          add(p89,p156)
        ))(p252,p174))
      }
    )
    val fs = Lower.mapCombinations(f,new EnabledMappings(true, false, false, false, false, false))
    val code = Compile(fs.head)
    val Args = scala.collection.mutable.ArrayBuffer[Any]()
    for (j <- f.params.indices) {
      f.params(j).t match {
        case ArrayType(ArrayType(Float, l1), l2) =>
          Args += Array.tabulate(l1.eval, l2.eval)((r, c) => 1.0f)
        case ArrayType(Float, l1) =>
          Args += Array.fill(l1.eval)(2.0f)
        case Float =>
          Args += 3.0f
        case _=>
      }
    }
    val output_int = Interpreter(f).->[Vector[Float]].run(Args:_*).toArray[Float]
    val(output_exe:Array[Float],_)= Execute(1,1024)(code,fs.head,Args:_*)
    assert(output_exe.corresponds(output_int)(_==_))

  }
}