package edu.colorado.cuplv

/*
  NNDraw: Utility for drawing a neural network layer by layer

  Author: Sriram Sankaranarayanan
  Date: April 13, 2019
*/

import java.awt.Color

import scala.swing.Graphics2D

object NNDraw {
    def drawNNLayer(g: Graphics2D, y: Int, lyrPat: List[Option[Double]],
                    x_min: Int = 20, x_max: Int = 250, deltaY: Int = 20, nnThresh: Double = 5.0 ): Int = {
        val nNeurons = lyrPat.length
        val rad: Double = math.min((x_max - x_min).toDouble/nNeurons.toDouble, 30)
        val xmin: Int = ((x_max+x_min)/2 - nNeurons*rad/2).toInt
        val _ = lyrPat.foldLeft (xmin) {
            case (x, Some(v)) => {
                val gr = (math.min(v, nnThresh)/nnThresh*255).toInt
                g.setPaint(new Color(0, gr, gr))
                g.fillOval(x, y, rad.toInt, deltaY)
                x + rad.toInt
            }
            case (x, None) => {
                g.setPaint(Color.RED)
                g.fillOval(x, y, rad.toInt, deltaY)
                x + rad.toInt
            }
        }

        y + deltaY
    }
}
