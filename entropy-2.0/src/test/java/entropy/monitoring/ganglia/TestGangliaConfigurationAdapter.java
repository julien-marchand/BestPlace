/*
 * Copyright (c) 2009 Ecole des Mines de Nantes.
 * 
 *     This file is part of Entropy.
 * 
 *     Entropy is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     Entropy is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 * 
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
*/

package entropy.monitoring.ganglia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.Configuration;

/**
 * Unit tests for GangliaConfigurationAdapter.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"}, sequential = true)
public class TestGangliaConfigurationAdapter {

    /**
     * Base dir of the resources.
     */
    public static final String RESOURCES_DIR = "src/test/resources/entropy/monitoring/ganglia/TestGangliaConfigurationAdapter.";

    /**
     * Start a mock Ganglia meta daemon to serve A request.
     *
     * @param response the response to send
     */
    private void startDummyMetaServer(String response) {
        try {
            ServerSocket srv = new ServerSocket(GangliaConfigurationAdapter.DEFAULT_PORT);
            Socket client = srv.accept();
            BufferedReader reader = new BufferedReader(new FileReader(response));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            String line = reader.readLine();
            while (line != null) {
                out.write(line);
                line = reader.readLine();
            }
            out.close();
            srv.close();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test all the accessors.
     */
    public void testGets() {
        GangliaConfigurationAdapter a = new GangliaConfigurationAdapter("localhost", 80);
        Assert.assertEquals(a.getHostname(), "localhost");
        Assert.assertEquals(a.getPort(), 80);
    }

    /**
     * Test the access to the XML dump of the gmetad.
     */
    public void testReadXMLDump() {
        Thread t = new Thread() {
            @Override
			public void run() {
                startDummyMetaServer(RESOURCES_DIR + "sample.xml");
            }
        };
        t.start();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        GangliaConfigurationAdapter a = new GangliaConfigurationAdapter("localhost");
        try {
            String buffer = a.readXMLDump();
            t.join();
            Assert.assertTrue(buffer.length() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test the full extraction method.
     */
    public void testExtractConfiguration() {
        Thread t = new Thread() {
            @Override
			public void run() {
                startDummyMetaServer(RESOURCES_DIR + "sample.xml");
            }
        };
        t.start();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        GangliaConfigurationAdapter a = new GangliaConfigurationAdapter("localhost");
        try {
            Configuration c = a.extractConfiguration();
            t.join();
            Assert.assertEquals(c.getOnlines().size(), 10);
            Assert.assertEquals(c.getRunnings().size(), 60);
            //Quick check of the configuration
        } catch (Exception e) {
            e.printStackTrace();
            try {
                t.join();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            Assert.fail(e.getMessage());
        }
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
