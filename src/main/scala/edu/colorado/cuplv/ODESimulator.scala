package edu.colorado.cuplv
/*
  ODESimulator: Interface to the numrecip library.

  Author: Sriram Sankaranarayanan
  Date: April 13, 2019
*/

import java.awt.Graphics2D

import com.github.fons.nr.ode.{Factory, OdeSolverT, Solver}

import scala.util.{Failure, Success, Try}
import scala.util.Try

class OdeSolverException(s: String) extends Exception

trait ODESimulator {
    var curState: List[Double]
    def getState: List[Double] = curState
    def setStateComponent(j: Int, v: Double): Unit =
        curState = (0 until  curState.length).map( i => if (i == j) v else curState(i)).toList

    def vectorField (u: Double, w: Double): List[(Double, Seq[Double])=> Double]
    def simulate(control_input: Double, disturbance: Double, delta: Double): Unit = {
        val ode: Try[OdeSolverT] = Factory(Solver.RKE56, delta/100, (0.0, curState), vectorField(control_input, disturbance),  0.000001)
        ode match {
            case Success(ode1) =>{
                ode1(delta) match {
                    case Success(odeRes) => { curState = odeRes(delta).get }
                    case _ => throw new OdeSolverException("ODE Solver failed -- Bailing out")
                }
            }
            case _ => throw new OdeSolverException("ODE Solver failed -- Bailing out")
        }
    }

    def render(g: Graphics2D)
    def step: Unit
    def click(x: Double, y: Double): Unit
}

/*
// - Old experimental stuff -- Commented out and moved elsewhere
object CartPoleODESimulator extends ODESimulator {
    val m = 0.21
    val M = 0.815
    val g = 9.8
    val l = 0.305
    val e = 0.01

    override def vectorField(control_input: Double, w : Double): List[(Double, Seq[Double]) => Double] = List(
        (t: Double, x: Seq[Double]) => x(1) ,
        (t: Double, x: Seq[Double]) => {

            val num = 4 * control_input - 4 * e * x(1) + 4 * m * l * x(2)*x(2) * math.sin(x(2)) - 3 * m * g * math.sin(x(2)) * math.cos(x(2))
            val den =   4 * (M + m) - 3 * m * math.cos(x(2)) * math.cos(x(2))
            num / den},
        (t: Double, x: Seq[Double]) => {x(3)} ,
        (t: Double, x: Seq[Double]) => {
            val num = (M + m) * g * math.sin(x(2)) - (control_input - e * x(1)) * math.cos(x(2)) - m * l * x(3) * x(3) * math.sin(x(2)) * math.cos(x(2))
            val den = l * ((4 / 3) * (M + m) - m * math.cos(x(2)) * math.cos(x(2)))// (l * ((4 / 3) * (M + m) - m * (cos(x(3)) ^ 2)))
            num/den
        }
    )

}

*/
