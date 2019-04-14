package edu.colorado.cuplv

/*
  ToraODEClass: ODE for the TORA system.

  Author: Sriram Sankaranarayanan
  Date: April 13, 2019
*/
import java.awt.{Color, Graphics2D}

class ToraODEClass(override var curState: scala.List[Double], val bigNetwork: Boolean = true)  extends ODESimulator {
    /*
        e = 0.1;
    dxdt =[ x(2);
            -x(1) + e * sin(x(3)) ;
            x(4);
            control_input ;];
     */
    val e = 0.1
    var cur_input = 0.0
    override def vectorField(u: Double, w: Double): List[(Double, Seq[Double]) => Double] =
        List(
            (t: Double, x: Seq[Double]) => x(1),
            (t: Double, x: Seq[Double]) => -x(0) + e * math.sin(x(2)),
            (t: Double, x: Seq[Double]) => x(3),
            (t: Double, x: Seq[Double]) => u
        )

    val x1Ind = new StateIndicator(-2,2,600,750, 330,20, "x1")
    val x2Ind = new StateIndicator(-2,2,600,750, 350,20, "x2")
    val x3Ind = new StateIndicator(-2, 2, 600, 750, 370,20, "x3" )
    val x4Ind = new StateIndicator(-2, 2, 600, 750, 390,20, "x4" )
    val uInd = new StateIndicator(-2, 2, 600, 750, 410,20, "u" )

    val neuralNetwork = {
        if (bigNetwork)
            NeuralNetworkFactory.readFromFile("networks/tora_giant_controller.nt")
        else
            NeuralNetworkFactory.readFromFile("networks/tora_tiny_controller.nt")
    }

    def drawZigZag(g: Graphics2D, x0: Int, x1: Int, y: Int, nPitches: Int, amp: Int) = {
        val quarterWidth: Double = ((x1.toDouble - x0.toDouble ) / (4.0 * nPitches.toDouble))
        val xFinal = (1 to nPitches).foldLeft[Double](x0.toDouble) {
            case (xCur, _) => {
                g.drawLine(xCur.toInt, y, (xCur+quarterWidth).toInt, y+amp)
                g.drawLine((xCur+quarterWidth).toInt, y+amp, (xCur+2*quarterWidth).toInt, y)
                g.drawLine((xCur + 2 * quarterWidth).toInt, y, (xCur+3*quarterWidth).toInt, y-amp)
                g.drawLine((xCur+3*quarterWidth).toInt, y-amp, (xCur+4*quarterWidth).toInt, y)
                xCur + 4 * quarterWidth
            }
        }
        g.drawLine(xFinal.toInt, y, x1, y)
    }

    override def render(g: Graphics2D): Unit = {
        val theta = curState(2)
        val disp = curState(0) - e * math.sin(curState(2))


        val cx = 400
        val cy = 700
        val scale = 50.0
        val xPos = (cx + scale * disp).toInt

        /*
           Draw the Box
         */

        g.setPaint(Color.BLACK)
        g.drawRect(xPos - 50, cy-50, 100, 100)
        g.setPaint(Color.RED)
        g.fillRect (10,cy-40, 30, 100)
        //g.drawLine(40, cy, xPos-50, cy) // ZIG ZAG LATER
        drawZigZag(g, 40, xPos-50, cy, 20,10)
        g.setPaint(Color.BLACK)
        g.fillOval(xPos-30,cy+40,20,20)
        g.fillOval(xPos+10, cy+40, 20, 20)
        g.setPaint(new Color(110,120,0))
        g.fillRect(5,cy+60,795,20)
        /*
            Draw the Pendulum
         */

        val pendX = xPos
        val pendY = cy
        val pendLen = 30
        val pendDX = (pendLen * math.sin(theta)).toInt
        val pendDY = (pendLen * math.cos(theta)).toInt
        g.fillOval(pendX-2, pendY-2,4,4 )
        g.drawLine(pendX, pendY, pendX + pendDX, pendY+pendDY)
        g.setPaint(Color.YELLOW)
        g.fillOval(pendX+ pendDX - 8, pendY+pendDY - 8, 16, 16)

        /*-- Render the network --*/
        val pat = neuralNetwork.getNetworkActivationPattern(curState)
        pat.foldLeft (50) ((y,lyrPat) => {
            NNDraw.drawNNLayer(g, y, lyrPat,nnThresh=1.0,x_min=20, x_max=780,deltaY=20)
        } )

        x1Ind.render(curState(0), g)
        x2Ind.render(curState(1), g)
        x3Ind.render(curState(2), g)
        x4Ind.render(curState(3), g)
        uInd.render(cur_input, g)
    }

    override def click(x: Double, y: Double) = {
        x1Ind.click(x,y) match {
            case Some(v) => setStateComponent(0, v)
            case None => ()
        }
        x2Ind.click(x,y) match {
            case Some(v) => setStateComponent(1, v)
            case None => ()
        }
        x3Ind.click(x,y) match {
            case Some(v) => setStateComponent(2, v)
            case None => ()
        }
        x4Ind.click(x,y) match {
            case Some(v) => setStateComponent(3, v)
            case None => ()
        }

    }

    override def step: Unit = {
        val bias = 10.0
        val cur_input_list = neuralNetwork.evalNetwork(curState)
        assert(cur_input_list.length == 1)
        val u = cur_input_list.head - bias
        cur_input = u
        simulate(u, 0.0, 0.1)
    }



}
