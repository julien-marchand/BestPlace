/*
 * Copyright (c) Fabien Hermenier
 *
 * This file is part of Entropy.
 *
 * Entropy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Entropy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package entropy.tools;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import entropy.configuration.Configuration;
import entropy.configuration.parser.ConfigurationSerializerException;
import entropy.configuration.parser.FileConfigurationSerializer;
import entropy.configuration.parser.FileConfigurationSerializerFactory;
import entropy.configuration.parser.PlainTextConfigurationSerializer;
import entropy.configuration.parser.ProtobufConfigurationSerializer;
import entropy.plan.TimedReconfigurationPlan;
import entropy.plan.parser.FileTimedReconfigurationPlanSerializer;
import entropy.plan.parser.FileTimedReconfigurationPlanSerializerFactory;
import entropy.plan.parser.PlainTextTimedReconfigurationPlanSerializer;
import entropy.plan.parser.ProtobufTimedReconfigurationPlanSerializer;
import entropy.plan.parser.TimedReconfigurationPlanSerializerException;

/**
 * A tool to convert serialized configuration or plan in different format.
 *
 * @author Fabien Hermenier
 */
public final class DataConverter {

    public static final String PLAN_MODE = "-plan";

    public static final String CFG_MODE = "-cfg";

    public static final String INPUT_FORMAT_FLAG = "-if";

    public static final String OUT_FORMAT_FLAG = "-of";

    public static final String PB_FORMAT = "pbd";

    public static final String TXT_FORMAT = "txt";

    private DataConverter() {
    }

    private static void exitOnError(String msg) {
        System.err.println(msg);
    }

    /**
     * Launcher.
     *
     * @param args arguments to pass
     */
    public static void main(String[] args) {

        if (args.length < 5) {
            System.err.println("Convert configuration file");
            System.err.println("Usage: dataConv [-plan | -cfg] [-if pbd|txt] -of pbd|txt input_files output");
            System.err.println("input_files: series of files. Format may be inferred wrt. the files extension");
            System.err.println("output: the ouput folder");
            System.err.println("Supported datafile:");
            System.err.println("\t-plan reconfiguration plan");
            System.err.println("\t-cfg configuration");
            System.err.println("supported formats:");
            System.err.println("\tpbd - binary format from protobuf messages");
            System.err.println("\ttxt - plain text format");
            System.exit(1);
        }
        String outputPath;
        String inputFormat = null;
        String outputFormat = null;
        String mode = null;
        List<String> inputs = new LinkedList<String>();
        int i = 0;
        while (i < args.length) {
            if (args[i].equals(PLAN_MODE) || args[i].equals(CFG_MODE)) {
                mode = args[i];
            } else if (args[i].equals(INPUT_FORMAT_FLAG)) {
                inputFormat = args[i + 1];
                i++;
            } else if (args[i].equals(OUT_FORMAT_FLAG)) {
                outputFormat = args[i + 1];
                i++;
            } else if (i != args.length - 1) {
                inputs.add(args[i]);
            }
            i++;
        }

        outputPath = args[args.length - 1];

        //Check the args

        if (mode == null) {
            exitOnError("Type of files must be specified");
        }
        if (outputFormat == null) {
            exitOnError("Output format must be specified");
        }

        if (inputFormat != null && (inputFormat.equals(PB_FORMAT)
                || inputFormat.equals(TXT_FORMAT))) {
            exitOnError("Unsupported input format: " + inputFormat);
        }

        if (inputs.size() == 0) {
            exitOnError("No input configurations");
        }

        if (mode.equals(PLAN_MODE)) {
            convertPlan(inputs, inputFormat, outputFormat, outputPath);
        } else {
            convertConfiguration(inputs, inputFormat, outputFormat, outputPath);
        }
    }

    private static void convertConfiguration(List<String> inputs, String inputFormat, String outputFormat, String outputPath) {
        FileConfigurationSerializer out = null;
        if (outputFormat.equals(PB_FORMAT)) {
            out = ProtobufConfigurationSerializer.getInstance();
        } else if (outputFormat.equals(TXT_FORMAT)) {
            out = PlainTextConfigurationSerializer.getInstance();
        } else {
            exitOnError("Unsupported output format: " + outputFormat);
        }
        for (String input : inputs) {
            FileConfigurationSerializer src = null;
            if (inputFormat == null) {
                src = FileConfigurationSerializerFactory.getInstance().getSerializer(input);
                if (src == null) {
                    System.err.println("Skipping '" + input + "': not compatible with the input format");
                    continue;
                }
            } else if (inputFormat.equals(PB_FORMAT)) {
                src = ProtobufConfigurationSerializer.getInstance();
            } else if (inputFormat.equals(TXT_FORMAT)) {
                src = PlainTextConfigurationSerializer.getInstance();
            }

            //Make the conversion
            String outputName = convertFileName(input, outputPath, outputFormat);
            Configuration cfg = null;
            try {
                cfg = src.read(input);

            } catch (IOException e) {
                System.err.println("Error while reading '" + input + "': " + e.getMessage());
            } catch (ConfigurationSerializerException e) {
                System.err.println("Error while parsing '" + input + "': " + e.getMessage());
            }

            try {
                out.write(cfg, outputName);
            } catch (IOException e) {
                System.err.println("Error while writing '" + outputName + "': " + e.getMessage());
            }
        }
    }

    private static void convertPlan(List<String> inputs, String inputFormat, String outputFormat, String outputPath) {
        FileTimedReconfigurationPlanSerializer out = null;
        if (outputFormat.equals(PB_FORMAT)) {
            out = ProtobufTimedReconfigurationPlanSerializer.getInstance();
        } else if (outputFormat.equals(TXT_FORMAT)) {
            out = PlainTextTimedReconfigurationPlanSerializer.getInstance();
        } else {
            exitOnError("Unsupported output format: " + outputFormat);
        }
        for (String input : inputs) {
            FileTimedReconfigurationPlanSerializer src = null;
            if (inputFormat == null) {
                src = FileTimedReconfigurationPlanSerializerFactory.getInstance().getSerializer(input);
                if (src == null) {
                    System.err.println("Skipping '" + input + "': not compatible with the input format");
                    continue;
                }
            } else if (inputFormat.equals(PB_FORMAT)) {
                src = ProtobufTimedReconfigurationPlanSerializer.getInstance();
            } else if (inputFormat.equals(TXT_FORMAT)) {
                src = PlainTextTimedReconfigurationPlanSerializer.getInstance();
            }

            //Make the conversion
            String outputName = convertFileName(input, outputPath, outputFormat);
            TimedReconfigurationPlan plan = null;
            try {
                plan = src.read(input);
            } catch (IOException e) {
                System.err.println("Error while reading '" + input + "': " + e.getMessage());
            } catch (TimedReconfigurationPlanSerializerException e) {
                System.err.println("Error while parsing '" + input + "': " + e.getMessage());
            }

            try {
                out.write(plan, outputName);
            } catch (IOException e) {
                System.err.println("Error while writing '" + outputName + "': " + e.getMessage());
            }
        }
    }

    private static String convertFileName(String path, String outputPath, String outputFormat) {
        File x = new File(path);
        String name = x.getName().substring(0, x.getName().lastIndexOf("."));
        return outputPath + "/" + name + "." + outputFormat;
    }
}
