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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A Mock Xen HTTP server.
 * @author Fabien Hermenier
 *
 */
public class MockXendHTTP extends Thread {

	
	private ServerSocket srv;
	
	public MockXendHTTP(int p) throws IOException {
		srv = new ServerSocket(p);
	}

    @Override
	public void run() {
		Socket client;
		int nb = 0;
		while (true) {
			
			try {
				System.out.println((++nb) + " request(s)");
				client = srv.accept();
				BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
			
				String line = "line " + reader.readLine();
				while (line.length() > 0) {
					System.out.println(line);
					line = reader.readLine();
				}
				Thread.sleep((long) (Math.random()*1000));
				writer.println("Http/1.1 200 OK");
				writer.println("Content-length: 0");
				writer.println("Expires: -1");
				writer.println("Pragma: no-cache");
				writer.println("Cache-control: no-cache");
				writer.println("");
				reader.close();
				writer.close();
				client.close();
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
	}
	
	
}
