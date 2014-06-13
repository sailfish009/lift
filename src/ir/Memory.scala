package ir

abstract class Memory {  
  def variable : Var
  def size : Expr
  def t : Type
}

object NullMemory extends Memory {
  val variable = Var("NULL")
  val size = Cst(0)
  val t = UndefType
}
