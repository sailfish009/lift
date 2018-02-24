package utils.paternoster.gui

import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.image.WritableImage
import javafx.scene.layout.Pane
import javafx.scene.paint.{Color, Paint}
import javafx.scene.text.Font

import utils.paternoster.logic.Graphics.GraphicalPrimitive



/**
  * Created by federico on 16/08/17.
  */

class MainPane(val width:Int, val height:Int) extends Pane {
  //General scaling
  var unitX = 120d
  var unitY = 60d
  //Used to separate things
  val smallX = 1
  val smallY = 1
  val canvas = new Canvas(width,height)
  //canvas.setScaleX(0.5)
  //canvas.setScaleY(0.5)
  this.getChildren.add(canvas)

  def draw(primitives:Iterable[GraphicalPrimitive]) = {
    val gc = this.canvas.getGraphicsContext2D
    gc.setFont(new Font(gc.getFont.getName,10))
    val context = JavaFXRenderer.Context(gc, unitX, unitY, smallX, smallY,width.toDouble,height.toDouble)
    JavaFXRenderer.drawPrimitives(primitives, context)
  }

  def renderToSvg(primitives:Iterable[GraphicalPrimitive]): Unit ={

  }

  def getSnapShot(wim: WritableImage): Unit ={
    canvas.snapshot(null,wim)
  }
  def getFontSize():Double ={
    this.canvas.getGraphicsContext2D.getFont.getSize
  }
  def getGraphicsContext(): GraphicsContext ={
    canvas.getGraphicsContext2D
  }
  def setCanvasWidth(width:Double):Unit={
    this.canvas.setWidth(width)
  }
  def setCanvasHeight(height:Double):Unit={
  this.canvas.setHeight(height)
  }
}
