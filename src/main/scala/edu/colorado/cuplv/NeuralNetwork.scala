package edu.colorado.cuplv

import scala.io.Source




class NeuralNetwork(n_inputs: Int, n_output: Int, n_hidden: Int,
                    lstOfLayers: List[Int], lyrData: List[List[(List[Double], Double)]]) {

    def dotProd(a: List[Double], b: List[Double]) = {
        assert(a.length == b.length)
        (a zip b).foldLeft(0.0) {
            case (acc, (x,y)) => acc + x * y
        }
    }

    def evalLayer(inputs: List[Double],
                  lyrWeights: List[(List[Double], Double)]) = {
        val raw_out = lyrWeights.map {
            case (w, b) => dotProd(w, inputs) + b
        }
        raw_out.map(x => math.max(x, 0.0))
    }



    def evalNetwork(inputValues: List[Double]) = {
        assert(inputValues.length == n_inputs)
        lyrData.foldLeft (inputValues) {
            case (cur_inputs, newLayerWeights) => evalLayer(cur_inputs, newLayerWeights)
        }
    }

    def getNetworkActivationPattern(inputValues: List[Double]): List[List[Option[Double]]] = {
        assert(inputValues.length == n_inputs)
        val (_, pattern) = lyrData.foldLeft[(List[Double], List[List[Option[Double]]])] ((inputValues, List())) {
            case ((cur_inputs, curPattern), newLayerWeights) =>  {
                val new_inputs = evalLayer(cur_inputs, newLayerWeights)
                val layer_pattern = new_inputs.map {case x => if (x <= 0) None else Some(x)}
                (new_inputs, layer_pattern::curPattern)
            }
        }
        pattern.reverse
    }

    def printNetworkInfo: Unit = {
        println(n_inputs)
        println(n_output)
        println(n_hidden)
        lstOfLayers.foreach(println(_))
        lyrData.foreach( lyr => lyr.foreach {
            case (wts, bias) => { wts.foreach(println(_))
                                  println(bias)
               }
        })
    }

}

object NeuralNetworkFactory {

    type LayerSpec = (List[Double], Double)

    def extractForSingleNeuron(idx: Int, num_prev: Int, lst: List[String]): (Int, LayerSpec) = {
        val lst1 = lst.slice(idx, idx + num_prev).map(_.toDouble)
        val bias1 = lst(idx+num_prev).toDouble
        (idx+num_prev+1, (lst1, bias1))
    }

    def extractForHiddenLayer(idx: Int, num_prev: Int,  num_neurons: Int, lst: List[String]): (Int, List[LayerSpec]) = {
        val (finalIdx, lyrList) = (1 to num_neurons).foldLeft[(Int, List[LayerSpec])] ((idx, List())) {
            case ((idx, lstSoFar), _) => {
                val (newIdx, lyrSpec) = extractForSingleNeuron(idx, num_prev, lst)
                (newIdx, lyrSpec::lstSoFar)
            }
        }
        (finalIdx, lyrList.reverse)
    }

    def readFromFile(networkFileName: String): NeuralNetwork = {
        val file_lines: List[String] = Source.fromFile(networkFileName).getLines.toList
        val n_inputs = file_lines(0).toInt
        val n_outputs = file_lines(1).toInt
        val n_hidden = file_lines(2).toInt
        val lstOfLayers0: List[Int] = (1 to n_hidden).map( i => file_lines(2+i).toInt).toList
        val lstOfLayers: List[Int] = lstOfLayers0++List(n_outputs)
        val idx = n_hidden + 3
        /* Now read the layers and organize the matrix
         *  For Each Layer we have
         *     Layer = List[ (List[Double], Double) ]
         *     Network has a list of Layers
         */
        val (_, finalIdx, lstFinalRev) = (1 to n_hidden+1).foldLeft[(Int, Int, List[List[LayerSpec]])] ((n_inputs, idx,  List())) {
            case ( (n_prev, idx, lstSoFar), j ) => {
                val n_cur = lstOfLayers(j-1)
                val (newIdx, lyrList) = extractForHiddenLayer(idx, n_prev, n_cur, file_lines)
                (n_cur, newIdx, lyrList::lstSoFar)
            }
        }
        val lstFinal = lstFinalRev.reverse
        println(s"Final IDX: $finalIdx  and List Length: ${file_lines.length}")
        assert(finalIdx == file_lines.length)
        new NeuralNetwork(n_inputs, n_outputs, n_hidden, lstOfLayers, lstFinal)
    }
}