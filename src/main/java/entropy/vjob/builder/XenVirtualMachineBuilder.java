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
package entropy.vjob.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import entropy.configuration.DefaultVirtualMachine;
import entropy.configuration.VirtualMachine;

/**
 * A builder instanto to read configuration file for the Xen Hypervisor.
 *
 * @author Fabien Hermenier
 */
public class XenVirtualMachineBuilder implements VirtualMachineBuilder {

    /**
     * The root of the configs.
     */
    private String configsDir;

    /**
     * The REGEX to read the amount of memory to allocate.
     */
    public static final Pattern MEMORY_PATTERN = Pattern.compile("^\\s*memory\\s*=\\s*'?(\\d+)'?.*");

    /**
     * The REGEX to read the number of VCPU.
     */
    public static final Pattern VCPU_PATTERN = Pattern.compile("^\\s*vcpus\\s*=\\s*(\\d+).*");


    /**
     * Make a new builder.
     *
     * @param dir the directory where the configuration files are stored
     */
    public XenVirtualMachineBuilder(String dir) {
        this.configsDir = dir;
    }

    /**
     * Get the root of the config files.
     *
     * @return a path
     */
    public String getConfigDir() {
        return this.configsDir;
    }

    /**
     * Retrieve a VirtualMachine by parsing the configuration file.
     * If no VCPU are denoted, assumed 1.
     *
     * @throws VirtualMachineBuilderException if an error occurred while parsing the file
     */
    @Override
    public VirtualMachine buildVirtualMachine(String id) throws VirtualMachineBuilderException {
        int mem = 0;
        int vcpu = 1;
        //Open the config file
        File f = new File(configsDir + "/" + id);
        if (!f.exists()) {
            return null;
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(f));
            String line = in.readLine();
            while (line != null) {
                Matcher m = MEMORY_PATTERN.matcher(line);
                if (m.matches()) {
                    mem = Integer.parseInt(m.group(1));
                }
                m = VCPU_PATTERN.matcher(line);
                if (m.matches()) {
                    vcpu = Integer.parseInt(m.group(1));
                }
                line = in.readLine();
            }
        } catch (IOException e) {
            throw new VirtualMachineBuilderException("Unable to build VM '" + id + ": " + e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new VirtualMachineBuilderException(e.getMessage(), e);
                }
            }
        }


        if (mem == 0) {
            throw new VirtualMachineBuilderException("Fail at building the VM '" + id + "': Unable to read the amount of memory to allocate");
        }
        return new DefaultVirtualMachine(id, vcpu, 0, mem);
    }

}
