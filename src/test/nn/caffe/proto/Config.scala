package nn.caffe.proto

import _root_.caffe.caffe.{LayerParameter, NetParameter, V1LayerParameter}
import com.typesafe.scalalogging.Logger
import nn.caffe.proto.Config.{Version, getType}
import nn.cnn.ExperimentParams
import nn.{cnn, conv, fc}

import scala.io.Source
import scalapb.TextFormatError

case class Config(netParam: NetParameter) {
  val version: Config.Version.Value = if (netParam.layers.nonEmpty) Version.V1 else Version.NEW
  var dataLayerV1: Option[V1LayerParameter] = None
  var dataLayerVNew: Option[LayerParameter] = None

  val layersWithSizesV1: Option[Seq[(V1LayerParameter, (Int, Int))]] = {
    version match {
      case Version.V1 => Some(fillMissingParametersV1())
      case Version.NEW => None
    }
  }
  
  val layersWithSizesVNew: Option[Seq[(LayerParameter, (Int, Int))]] = {
    version match {
      case Version.V1 => None
      case Version.NEW => Some(fillMissingParametersVNew())
    }
  }

  def fillMissingParametersV1(): Seq[(V1LayerParameter, (Int, Int))] = {
    dataLayerV1 = netParam.layers.find(layer => layer.`type`.get == V1LayerParameter.LayerType.DATA &&
      (layer.include.isEmpty || layer.include.exists(_.phase match {
        case Some(_root_.caffe.caffe.Phase.TEST) => true
        case None => true
        case _ => false
      }))
    ) match {
      case Some(l) => Some(l)
      case None => throw new java.util.NoSuchElementException("Cannot find a data layer in Caffe protofile")
    }

    val processedLayers = scala.collection.mutable.Map[V1LayerParameter, (Int, Int)]()

    // TODO: get input channels from Caffe
    processedLayers += ((dataLayerV1.get, (dataLayerV1.get.transformParam.get.cropSize.get, 3)))
    for (layer <- netParam.layers) {
      if (!processedLayers.contains(layer)) {

        def computeInputDimensions(currentLayer: V1LayerParameter): (Int, Int) = {
          if (processedLayers.contains(currentLayer))
            processedLayers(currentLayer)
          else {
            val parent = netParam.layers.find(l => currentLayer.bottom.contains(l.name.get)).get

            val currentLayerDimensions: (Int, Int) = getType(parent) match {
              case V1LayerParameter.LayerType.RELU => computeInputDimensions(parent)
              case V1LayerParameter.LayerType.LRN => computeInputDimensions(parent)
              case V1LayerParameter.LayerType.DATA => 
                (parent.transformParam.get.cropSize.get, 3) // TODO: get input channels from Caffe

              case V1LayerParameter.LayerType.CONVOLUTION =>
                def getStride(stride: Seq[Int]): Int = if (stride.nonEmpty) stride.head else 1

                ((computeInputDimensions(parent)._1 -
                  (parent.convolutionParam.get.kernelSize.head - getStride(parent.convolutionParam.get.stride)) +
                  parent.convolutionParam.get.pad.head * 2) / getStride(parent.convolutionParam.get.stride),
                parent.convolutionParam.get.numOutput.get)

              case V1LayerParameter.LayerType.POOLING =>
                val parentDimensions: (Int, Int) = computeInputDimensions(parent)
                ((parentDimensions._1 -
                  (parent.poolingParam.get.kernelSize.head - parent.poolingParam.get.stride.get) +
                  parent.poolingParam.get.pad.getOrElse(0) * 2) / parent.poolingParam.get.stride.get,
                  parentDimensions._2)

              case V1LayerParameter.LayerType.INNER_PRODUCT => 
                (parent.innerProductParam.get.numOutput.get, 1) // TODO: verify
              case V1LayerParameter.LayerType.CONCAT => computeInputDimensions(parent)
              case V1LayerParameter.LayerType.DROPOUT => computeInputDimensions(parent)
              case V1LayerParameter.LayerType.SOFTMAX => computeInputDimensions(parent)
              case V1LayerParameter.LayerType.SOFTMAX_LOSS => computeInputDimensions(parent)
              case V1LayerParameter.LayerType.ACCURACY => (parent.accuracyParam.get.topK.get, 1) // TODO: verify
              case V1LayerParameter.LayerType.ELTWISE => computeInputDimensions(parent)
              case _ =>
                parent.`type`.get.name match {
                  case "BatchNorm" => computeInputDimensions(parent)
                  case "Scale" => computeInputDimensions(parent)

                  case _ =>
                    throw new java.lang.IllegalArgumentException("Cannot recognize a layer from the proto file (" +
                      parent.`type`.get.name + ")")
                }
            }
            processedLayers += ((currentLayer, currentLayerDimensions))

            currentLayerDimensions
          }
        }

        computeInputDimensions(layer)
      }
    }
    netParam.layers.map(layer => (layer, processedLayers(layer)))
  }

  def fillMissingParametersVNew(): Seq[(LayerParameter, (Int, Int))] = {
    dataLayerVNew = netParam.layer.find(layer => getType(layer) == V1LayerParameter.LayerType.DATA &&
      (layer.include.isEmpty || layer.include.exists(_.phase match {
        case Some(_root_.caffe.caffe.Phase.TEST) => true
        case None => true
        case _ => false
      }))
    ) match {
      case Some(l) => Some(l)
      case None => throw new java.util.NoSuchElementException("Cannot find a data layer in Caffe protofile")
    }

    val processedLayers = scala.collection.mutable.Map[LayerParameter, (Int, Int)]()

    // TODO: here and below see fillMissingParametersV1
    processedLayers += ((dataLayerVNew.get, (dataLayerVNew.get.transformParam.get.cropSize.get, 1)))
    for (layer <- netParam.layer) {
      if (!processedLayers.contains(layer)) {

        def computeInputDimensions(currentLayer: LayerParameter): (Int, Int) = {
          if (processedLayers.contains(layer))
            processedLayers(layer)
          else {
            val parent = netParam.layer.find(layer => currentLayer.bottom.contains(layer.name.get)).get

            val currentLayerDimensions: (Int, Int) = getType(parent) match {
              case V1LayerParameter.LayerType.RELU => computeInputDimensions(parent)
              case V1LayerParameter.LayerType.LRN => computeInputDimensions(parent)
              case V1LayerParameter.LayerType.DATA => 
                (parent.transformParam.get.cropSize.get, 3)

              case V1LayerParameter.LayerType.CONVOLUTION =>
                def getStride(stride: Seq[Int]): Int = if (stride.nonEmpty) stride.head else 1

                ((computeInputDimensions(parent)._1 -
                  (parent.convolutionParam.get.kernelSize.head - getStride(parent.convolutionParam.get.stride)) +
                  parent.convolutionParam.get.pad.head * 2) / getStride(parent.convolutionParam.get.stride),
                  parent.convolutionParam.get.numOutput.get)

              case V1LayerParameter.LayerType.POOLING =>
                val parentDimensions: (Int, Int) = computeInputDimensions(parent)
                ((parentDimensions._1 -
                  (parent.poolingParam.get.kernelSize.head - parent.poolingParam.get.stride.get) +
                  parent.poolingParam.get.pad.getOrElse(0) * 2) / parent.poolingParam.get.stride.get,
                  parentDimensions._2)

              case V1LayerParameter.LayerType.INNER_PRODUCT => 
                (parent.innerProductParam.get.numOutput.get, 1)
              case V1LayerParameter.LayerType.CONCAT => computeInputDimensions(parent)
              case V1LayerParameter.LayerType.DROPOUT => computeInputDimensions(parent)
              case V1LayerParameter.LayerType.SOFTMAX => computeInputDimensions(parent)
              case V1LayerParameter.LayerType.SOFTMAX_LOSS => computeInputDimensions(parent)
              case V1LayerParameter.LayerType.ACCURACY => (parent.accuracyParam.get.topK.get, 1)
              case V1LayerParameter.LayerType.ELTWISE => computeInputDimensions(parent)
              case _ =>
                parent.`type`.get match {
                  case "BatchNorm" => computeInputDimensions(parent)
                  case "Scale" => computeInputDimensions(parent)

                  case _ =>
                    throw new java.lang.IllegalArgumentException("Cannot recognize a layer from the proto file (" +
                      parent.`type`.get + ")")
                }
            }
            processedLayers += ((layer, currentLayerDimensions))

            currentLayerDimensions
          }
        }

        computeInputDimensions(layer)
      }
    }
    netParam.layer.map(layer => (layer, processedLayers(layer)))
  }
}

/**
  * Created by nm on 11/06/18.
  */
object Config {
  def load(fileName: String): NetParameter = {
    val result: Either[TextFormatError, NetParameter] = 
      _root_.scalapb.TextFormat.fromAscii(NetParameter, Source.fromFile(fileName).getLines.mkString)
    result match {
      case Left(err) =>
        throw new java.lang.IllegalStateException(
          "Encountered text format error while parsing " + fileName + "\n" + err.msg)
      case Right(netParam) => netParam
    }    
  }
  
  def main(args: Array[String]): Unit = {
    // Example usage
//    val result = load("/home/nm/avus_remotes/lift/src/test/nn/caffe/proto/GoogleNet.prototxt")
    val result = load("/home/nm/avus_remotes/lift/src/test/nn/caffe/proto/ResNet-101-deploy-5.prototxt")
//    val result: NetParameter = load("/home/nm/avus_remotes/lift/src/test/nn/caffe/proto/VGG_ILSVRC_19_layers_deploy_1.prototxt")
    //result
  }
  
  // V1LayerParameter has a case class for each layer type. Newer LayerParameter stores layer type as string.
  // This type class matches the string to the respective case class of V1LayerParameter
  trait LayerParamType[T] {
    def getType(layerparam: T): V1LayerParameter.LayerType  
  }
  
  def getType[T](layerparam: T)(implicit lp: LayerParamType[T]) = lp.getType(layerparam)
  
  implicit val v1GetType: LayerParamType[V1LayerParameter] = new LayerParamType[V1LayerParameter] {
    def getType(layerparam: V1LayerParameter): V1LayerParameter.LayerType = layerparam.`type`.get
  }
  
  implicit val vGetType: LayerParamType[LayerParameter] = new LayerParamType[LayerParameter] {
    def getType(layerparam: LayerParameter): V1LayerParameter.LayerType = 
      V1LayerParameter.LayerType.values.find(_.name.toLowerCase() == layerparam.`type`.get.toLowerCase()).get
  }

  object Version extends Enumeration {
    type Version = Value
    val V1, NEW = Value
  }

  def configToExperimentParams(protoFilePath: String): Seq[ExperimentParams] = {

    val logger = Logger(this.getClass)
    logger.info("Processing PROTO config file \"" + protoFilePath + "\"")

    val config: nn.caffe.proto.Config = new Config(load(protoFilePath))
    
    val experimentName: String = config.netParam.name.get

    {
      config.version match {
        case Config.Version.V1 => config.layersWithSizesV1.get
        case Config.Version.NEW => config.layersWithSizesVNew.get}
    }.zipWithIndex.filter(layerAndNo => {
      layerAndNo._1._1 match {
        case layerV1: V1LayerParameter =>
          nn.caffe.proto.Config.getType(layerV1) == V1LayerParameter.LayerType.CONVOLUTION
        case layerVNew: LayerParameter =>
          nn.caffe.proto.Config.getType(layerVNew) == V1LayerParameter.LayerType.CONVOLUTION
      }}).map{
      case ((layer, inputDimensions), i) =>
        val nInputs: Int = {
          config.version match {
            case Config.Version.V1 => config.dataLayerV1.get.dataParam
            case Config.Version.NEW => config.dataLayerVNew.get.dataParam
          }}.get.batchSize.get

        val layerName: String = config.version match {
          case Config.Version.V1 => layer.asInstanceOf[V1LayerParameter].name.get
          case Config.Version.NEW => layer.asInstanceOf[LayerParameter].name.get
        }

        val paddedInputSize: Int = inputDimensions._1 + 2 * {
          config.version match {
            case Config.Version.V1 => layer.asInstanceOf[V1LayerParameter].convolutionParam.get.pad.head
            case Config.Version.NEW => layer.asInstanceOf[LayerParameter].convolutionParam.get.pad.head
          }}

        val nKernels: Int = {
          config.version match {
            case Config.Version.V1 => layer.asInstanceOf[V1LayerParameter].convolutionParam.get.numOutput.get
            case Config.Version.NEW => layer.asInstanceOf[LayerParameter].convolutionParam.get.numOutput.get
          }}

        val kernelSize: Int = {
          config.version match {
            case Config.Version.V1 => layer.asInstanceOf[V1LayerParameter].convolutionParam.get.kernelSize.head
            case Config.Version.NEW => layer.asInstanceOf[LayerParameter].convolutionParam.get.kernelSize.head
          }}

        val kernelStride: Int = {
          def getStride(stride: Seq[Int]): Int = if (stride.nonEmpty) stride.head else 1
          config.version match {
            case Config.Version.V1 => getStride(layer.asInstanceOf[V1LayerParameter].convolutionParam.get.stride)
            case Config.Version.NEW => getStride(layer.asInstanceOf[LayerParameter].convolutionParam.get.stride)
          }}

        new ExperimentParams(
          experimentName = experimentName,
          kernelOutputSubfolder = i.toString,
          layerName = layerName,
          layerNo = i,

          // TODO: generalise nBatches and channels
          exactParams = Some(
            ExperimentParams.Exact(
              inputConfig = cnn.InputConfig(1, nInputs, paddedInputSize, inputDimensions._2),
              convDimensions = conv.Experiment.Config.Dimensions(nKernels, kernelSize, kernelStride),
              fcDimensions = fc.Experiment.Config.Dimensions(1))),

          dim = None,

          inputTileSizeRange = List(
            (in: cnn.InputConfig, c: conv.Experiment.Config.Dimensions) =>
              (c.kernelSize to in.inputSize by 1).toList),

          elsPerThreadRange = List(
            (in: cnn.InputConfig, c: conv.Experiment.Config.Dimensions) =>
              (1 to (in.nChannels * c.kernelSize * c.kernelSize) by 1).toList),

          kernelsPerGroupRange = List(
            (in: cnn.InputConfig, c: conv.Experiment.Config.Dimensions) =>
              (1 to c.nKernels by 1).toList),
          
          coalesceRange = List(List(true, false)),
          unrollReduceRange = List(List(true, false)),
          vectorLenRange = List(List(1, 2, 4)),

          multsPerThreadRange = List(
            (_: cnn.InputConfig, _: fc.Experiment.Config.Dimensions) =>
              List(1)),
          neuronsPerWrgRange = List(
            (_: cnn.InputConfig, _: fc.Experiment.Config.Dimensions) =>
              List(1))
//          inputTileSizeRange = List(
//            (in: cnn.InputConfig, c: conv.Experiment.Config.Dimensions) =>
//              /*(c.kernelSize to in.inputSize by 1).toList*/List(4)),
//
//          elsPerThreadRange = List(
//            (in: cnn.InputConfig, c: conv.Experiment.Config.Dimensions) =>
//              /*(1 to (in.nChannels * c.kernelSize * c.kernelSize) by 1).toList*/List(576)),
//
//          kernelsPerGroupRange = List(
//            (in: cnn.InputConfig, c: conv.Experiment.Config.Dimensions) =>
//              /*(1 to c.nKernels by 1).toList*/List(64)),
//
//          coalesceRange = List(List(false)),
//          unrollReduceRange = List(List(true)),
//          vectorLenRange = List(List(4)),
//
//
//          multsPerThreadRange = List(
//            (_: cnn.InputConfig, _: fc.Experiment.Config.Dimensions) =>
//              List(1)),
//          neuronsPerWrgRange = List(
//            (_: cnn.InputConfig, _: fc.Experiment.Config.Dimensions) =>
//              List(1))
        )
    }
  }
}
