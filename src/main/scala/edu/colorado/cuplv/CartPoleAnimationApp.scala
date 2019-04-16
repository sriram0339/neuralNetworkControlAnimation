package edu.colorado.cuplv

/*
  CartPoleAnimationApp: Implements the main SimpleSwingApplication interface.
  This sets up the menu, canvas and the listeners for clicks.

  Author: Sriram Sankaranarayanan
  Date: April 12, 2019
*/

import java.awt.{Canvas, Color, Dimension, Graphics2D}

import scala.annotation.tailrec
import scala.swing.BorderPanel.Position.{Center, South}
import scala.swing.{Action, BorderPanel, Button, Dimension, MainFrame, MenuBar, MenuItem, Panel, SimpleSwingApplication}
import scala.swing.event.{ButtonClicked, MouseClicked}

class AnimateEventHandler(var sp: ODESimulator) {

    var enabled = true
    val sleepTime = 80

    def stop = {enabled = false}

    def click(x: Double, y: Double) = sp.click(x,y)

    @tailrec
    final def loop(canvas: Panel): Unit = {
        sp.step
        canvas.repaint()
        Thread.sleep(sleepTime)
        if (enabled)
            loop(canvas)
        else
            return
    }

    def startThread(canvas: Panel): Unit = {
        val runThread = new Thread {
            override def run() = {
                loop(canvas)
            }
        }
        runThread.start()
    }

   def getDefaultCanvas: Panel =
       new Panel {
           preferredSize = new Dimension(800, 800)
           override def paintComponent(g: Graphics2D) = {
	   	g.setPaint(Color.WHITE)
		g.clearRect(0,0, 800,800)

	   	sp.render(g)
		g.dispose()
            }
       }
}



object CartPoleAnimationApp extends SimpleSwingApplication {
    def top = new MainFrame {
        title = "Nailed It!"
        //val sp = new SimplePendulumClass(List(3.15,-0.8))
        val sp = new ToraODEClass(List(0.5,-0.55,-0.3,0.55))
        val global = new AnimateEventHandler(sp)
        val canvas = global.getDefaultCanvas
        val button = new Button {
            text = "STOP!"
            foreground = Color.blue
            background = Color.red
            borderPainted = true
            enabled = true
            tooltip = "Click and See"
        }
        size = new Dimension(800,800)
        menuBar = new MenuBar {

            contents += new MenuItem( Action("Inv. Pendulum") {
               global.sp = new SimplePendulumClass(List(3.15,-0.8))
            })
            contents += new MenuItem(
                Action("Ball \u00dcnd Beam") {
                    global.sp = new BallAndBeamODEClass(List(0.55,0.55,0.55,0.55))
                }
            )
            contents += new MenuItem(
                Action("Tora (Big Network)") {
                    global.sp = new ToraODEClass(List(0.6,-0.5,-0.3,0.5))
                }
            )

            contents += new MenuItem(
                Action("Tora (Tiny Network)") {
                    global.sp = new ToraODEClass(List(0.6,-0.5,-0.3,0.5), false)
                }
            )

        }
        contents = new BorderPanel {
            layout(canvas) = Center
            layout(button) = South

        }

        listenTo(canvas.mouse.clicks)
        listenTo(button)

        //size = new Dimension(500,550)
        reactions += {
            case MouseClicked(_, point, _, _, _) =>{
                global.click(point.x, point.y)
            }
            case ButtonClicked(c) if c == button =>{
                global.stop
            }
        }

        global.startThread(canvas)
    }

}
