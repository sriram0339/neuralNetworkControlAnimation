package edu.colorado.cuplv

/*
  Ball and Beam class: Implements the simulator and graphics for the ball and beam ODE.

  Author: Sriram Sankaranarayanan
  Date: April 14, 2019
*/
import java.awt.{Color, Graphics2D, Polygon}

class BallAndBeamODEClass(override var curState: List[Double]) extends ODESimulator {
    /*
    x(2);
            -9.8*x(3) + 1.6 * x(3)^3 + x(1)*x(4)^2;
            x(4);
            control_input
     */
    var cur_input: Double = 0
    override def vectorField(u: Double, w: Double): List[(Double, Seq[Double]) => Double] =
        List(
            (t: Double, x: Seq[Double]) => x(1),
            (t: Double, x: Seq[Double]) => -9.8 * math.sin(x(2)) + x(0)*(x(3)*x(3)) ,
            (t: Double, x: Seq[Double]) => x(3),
            (t: Double, x: Seq[Double]) => u
        )

    val rInd = new StateIndicator(-2,2,400,750, 200,25, "x")
    val vInd = new StateIndicator(-2,2,400,750, 250,25, "v")
    val thetaInd = new StateIndicator(-0.5,0.5,400,750, 300,25, "\u0398")
    val omegaInd = new StateIndicator(-1,1,400,750, 350,25, "\u03C9")
    val uInd = new StateIndicator(-10, 10, 400, 750, 400, 25, "u")
    val neuralNetwork = NeuralNetworkFactory.readFromFile("networks/controller_network_ball_and_beam.nt")
    override def render(g: Graphics2D): Unit = {

        val beamLen = 350
        //val beamWidth = 20
        val theta = curState(2)
        val ballPos = curState(0)
        g.setPaint(Color.RED)
        val cx = 400
        val cy = 650
        val dx = (beamLen * math.cos(theta)).toInt
        val dy = (beamLen * math.sin(theta)).toInt

        val lst = List(( - beamLen, -5), (beamLen, -5), (beamLen, 5), (-beamLen, 5))
        val rot_lst : List[(Double, Double)] = lst.map {
            case (x,y) => (x* math.cos(theta) - y *math.sin(theta), x * math.sin(theta)+y*math.cos(theta))
        }
        val xPoints = (rot_lst.map { case (x,y) => x.toInt + cx} ).toArray
        val yPoints =  (rot_lst.map { case (x,y) => -y.toInt + cy} ).toArray
        g.fillPolygon(xPoints, yPoints, 4)
        g.setPaint(Color.CYAN)
        g.drawLine(cx - dx, cy + dy, cx + dx, cy - dy)

        g.setPaint(Color.BLACK)
        val drx = (120.0 * ballPos * math.cos(theta)).toInt
        val dry = (120.0 * ballPos * math.sin(theta)).toInt
        g.fillOval(cx + drx -5 , cy - dry - 15  , 10,10)

        g.fillPolygon(Array(cx,cx-20,cx+20), Array(cy,cy+20,cy+20), 3)
        rInd.render(ballPos,g)
        thetaInd.render(theta,g)
        omegaInd.render(curState(3), g)
        vInd.render(curState(2),g)
        uInd.render(cur_input, g)
        /*-- Render the network --*/
        val pat = neuralNetwork.getNetworkActivationPattern(curState)
        pat.foldLeft (10) ((y,lyrPat) => {
            NNDraw.drawNNLayer(g, y, lyrPat,10,800,20, 0.4)
        } )
    }

    override def click(x: Double, y: Double) = {
        rInd.click(x,y) match {
            case Some(v) => setStateComponent(0, v)
            case None => ()
        }
        vInd.click(x,y) match {
            case Some(v) => setStateComponent(1, v)
            case None => ()
        }
        thetaInd.click(x,y) match {
            case Some(v) => setStateComponent(2, v)
            case None => ()
        }
        omegaInd.click(x,y) match {
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
        simulate(u, 0.0, 0.2)
    }

}
