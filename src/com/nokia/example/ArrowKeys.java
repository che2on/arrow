/*
 * Copyright © 2011 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */
package com.nokia.example;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.sensor.ChannelInfo;
import javax.microedition.sensor.Data;
import javax.microedition.sensor.DataListener;
import javax.microedition.sensor.SensorInfo;
import javax.microedition.sensor.SensorConnection;
import javax.microedition.sensor.SensorManager;

public class ArrowKeys extends MIDlet implements CommandListener, DataListener {
    private static final int BUFFER_SIZE = 1;
    private static final boolean IS_TRIGGERING_EVENT_ALWAYS = false;
    private static StringItem arrow;
    private static int exEvent = -1;
    private static Command exitCommand = new Command("Exit", Command.EXIT, 1);
    private SensorConnection sensor;
    
    public ArrowKeys() {
        Form form = new Form("ArrowKeys");
        form.addCommand(exitCommand);
        form.setCommandListener(this);
        arrow = new StringItem("Direction","");
        form.append(arrow);
        Display.getDisplay(this).setCurrent(form);
    }

	public void startApp() {
		sensor = openSensor();
		if (sensor==null) return;
		sensor.setDataListener(this, BUFFER_SIZE);
	}

	public void destroyApp(boolean par) {
		sensor.removeDataListener();
		try{
			sensor.close();
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}

	public void pauseApp() {}

	private SensorConnection openSensor(){
		SensorInfo infos[] = SensorManager.findSensors("acceleration", null);
		if (infos.length == 0) return null;
		try{
			return (SensorConnection)Connector.open(infos[0].getUrl());
		}catch(SecurityException se){
			se.printStackTrace();
			return null;
		}
		catch(IOException ioe){
			ioe.printStackTrace();
			System.out.println("Couldn't open sensor : "
					+ infos[0].getUrl()+"!");
			return null;
		}
        catch(IllegalArgumentException iae) {
			iae.printStackTrace();
			return null;

        }
	}

	public void commandAction(Command command, Displayable screen) {
		// exit command
		if (command == exitCommand){
			setStopped(true);
			destroyApp(false);
			notifyDestroyed();
		}
	}

	private synchronized void setStopped(boolean isStopped){
		notify();
	}

	/**
	 * The method returns an action event (UP,DOWN,LEFT,RIGHT) that
	 * corresponds to the given axis x and y accelerations.
	 * @param axis_x
	 * @param axis_y
	 * @return the corresponding action key
	 */
	private static int getActionKey(double axis_x, double axis_y){
		// axis_x: LEFT or RIGHT
		if (Math.abs(axis_x)>Math.abs(axis_y)){
			return axis_x<0?Canvas.RIGHT:Canvas.LEFT;
		}
		// axis_y: UP or DOWN
		return axis_y<0?Canvas.UP:Canvas.DOWN;
	}

	/**
	 * The method returns action events that
	 * corresponds to the given acceleration data.
	 * Valid return values are:
	 * Canvas.UP
	 * Canvas.DOWN
	 * Canvas.RIGHT
	 * Canvas.LEFT
	 * @param data the acceleration data
	 * @return the action event array
	 */

	private static int[] data2actionEvents(Data[] data){

		ChannelInfo cInfo = data[0].getChannelInfo();
		boolean isInts = cInfo.getDataType() == ChannelInfo.TYPE_INT? true: false;
		int[] events = new int[BUFFER_SIZE];

		if (isInts){
			int[][] ints = new int[2][BUFFER_SIZE];
			for (int i=0; i<2; i++){
				ints[i] = data[i].getIntValues();
			}
			for (int i=0; i<BUFFER_SIZE; i++){
				events[i] = getActionKey(ints[0][i], ints[1][i]);
			}
			return events;
		}
		double[][] doubles = new double[2][BUFFER_SIZE];
		for (int i=0; i<2; i++){
			doubles[i] = data[i].getDoubleValues();
		}
		for (int i=0; i<BUFFER_SIZE; i++){
			events[i] = getActionKey(doubles[0][i], doubles[1][i]);
		}
		return events;
	}

	public void dataReceived(SensorConnection sensor, Data[] d,	boolean isDataLost) {
		int[] events = data2actionEvents(d);

		for (int i=0; i<BUFFER_SIZE; i++){
			if (events[i] == exEvent && !IS_TRIGGERING_EVENT_ALWAYS)
				continue;

			exEvent = events[i];
			switch(events[i]){
			case Canvas.UP:
				arrow.setText("^");
				break;
			case Canvas.DOWN:
				arrow.setText("v");
				break;
			case Canvas.LEFT:
				arrow.setText("<");
				break;
			case Canvas.RIGHT:
				arrow.setText(">");
				break;
			default:
				arrow.setText("");
			}
		}
	}
}
