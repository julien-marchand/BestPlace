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
package entropy.tool;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.configuration.parser.PlainTextConfigurationSerializer;

/**
 * Affiche le taux d'utilisation des ressources en fonction du temps
 *
 * @author fabien
 */
public class ShowResources {


    public static void main(String[] args) {

        String rep = "/Users/fabien/Entropy2/bench_cluster09/eval_pastel/run_ctx4/2009-04-16/";

        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

        File root = new File(rep);
        int totalcpuCapa = 0, totalMemCapa = 0, totalcpuConso = 0, totalMemConso = 0;
        for (File f : root.listFiles()) {
            String date = f.getName();
            if (date.endsWith("-source.txt")) {
                try {
                    Configuration c = PlainTextConfigurationSerializer.getInstance().read(f.getAbsolutePath());
                    int cpuCapa = 0, cpuConso = 0, memCapa = 0, memConso = 0;
                    for (Node n : c.getOnlines()) {
                        cpuCapa += n.getNbOfCPUs();
                        memCapa += n.getMemoryCapacity();
                        if (c.getRunnings().size() > 0) {
                            totalcpuCapa += cpuCapa;
                            totalMemCapa += memCapa;
                        }
                        for (VirtualMachine vm : c.getRunnings(n)) {
                            cpuConso += (vm.getCPUConsumption() > 100 ? 1 : 0);
                            memConso += vm.getMemoryConsumption();
                            totalcpuConso += cpuConso;
                            totalMemConso += memConso;
                        }
                    }
                    Date d = df.parse(date.substring(0, date.indexOf("-")));
                    System.out.println((d.getTime() / 1000) + " " + cpuCapa + " " + cpuConso + " " + memCapa + " " + memConso);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            } else {
                System.out.println("skipping " + date);
            }
        }
        System.err.println((100.0 * totalcpuConso / totalcpuCapa) + "% CPU\t" + (100.0 * totalMemConso / totalMemCapa) + "%Mem");
    }
}
