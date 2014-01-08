/*
 * Copyright (c) 2010 Ecole des Mines de Nantes.
 *
 *      This file is part of Entropy.
 *
 *      Entropy is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Lesser General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      Entropy is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public License
 *      along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package entropy.execution.driver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import entropy.plan.action.Startup;

/**
 * Driver to boot a node using Wake-On-Lan.
 * As this method is not sure, the magic packet can be sent multiple times.
 * @author Fabien Hermenier
 * 
 */
public class WoLStartup extends Driver {

	/**
	 * Default destination port to send the magic packet.
	 */
	public static final int DEFAULT_PORT = 9;
	

	/**
	 * The number of magic packets to send.
	 */
	private int nbPackets;
	
	/**
	 * Default number of magic packets to send.
	 */
	public static final int DEFAULT_NB_PACKETS = 10;
		
	/**
	 * The action to execute.
	 */
	private Startup st;
	
	/**
	 * Make a new concrete startup action.
	 * @param action the startup action     
	 */
	public WoLStartup(Startup action) {
		super(action);
		this.st = action;
		this.nbPackets = DEFAULT_NB_PACKETS;
	}
	
	/**
	 * Set the number of packets to send.
	 * @param nb a positive
	 */
	public void setNbPackets(int nb) {
		this.nbPackets = nb;
	}

	/**
	 * Parse the MAC address.
	 * @param mac the mac in a String format: 6 hexadecimal number each separated by "-" or ":"
	 * @return a array of 6 bytes
	 */
    private static byte[] getMacBytes(String mac) {

        byte[] bytes = new byte[6];
        String[] hex = mac.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new NumberFormatException("Invalid MAC address.");
        }
        for (int i = 0; i < 6; i++) {
               bytes[i] = (byte) Integer.parseInt(hex[i], 16);
        }
        return bytes;
    }


    /**
     * Execute the startup action by sending magic packets.
     * @throws DriverException if an error occurs.
     */
	@Override
    public void execute() throws DriverException {
							
        try {
        	InetAddress ip = InetAddress.getByName(this.st.getNode().getName());
        	String mac = this.st.getNode().getMACAddress();
        	if (mac == null) {
        		throw new DriverException(this, "Unable to get the MAC Address of the node '" + this.st.getNode().getName() + "'");
        	}
            byte[] macBytes = getMacBytes(mac);
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }                    
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, ip, DEFAULT_PORT);
            DatagramSocket socket = new DatagramSocket();
            
            for (int i = 0; i < this.getNbPackets(); i++) {
            	socket.send(packet);
            }
            socket.close();           
        } catch (IOException e) {
            throw new DriverException(this, e.getMessage(), e);
        } catch (NumberFormatException e) {
        	throw new DriverException(this, "Failed at sending the magic packet", e);
        }
	}

	/**
	 * Get the number of magic packets that are sent.
	 * @return a positive integer
	 */
	public int getNbPackets() {
		return this.nbPackets;
	}

	@Override
	public String toString() {
		return "wol(" + this.st.toString() + ")";
	}
}
