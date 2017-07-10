package de.tum.bgu.msm;

import com.pb.common.datafile.TableDataFileReader;
import com.pb.common.datafile.TableDataSet;
import com.pb.common.matrix.Matrix;
import com.pb.common.util.ResourceUtil;
import omx.OmxMatrix;
import omx.hdf5.OmxHdf5Datatype;
import org.apache.log4j.Logger;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * Utilities for the Transport in Microsimulation Orchestrator (TIMO)
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 *
 */

public class MitoUtil {

    private static Logger logger = Logger.getLogger(MitoUtil.class);
    private static Random rand;
    private static String baseDirectory = "";


    public static void initializeRandomNumber(ResourceBundle rb) {
        // initialize random number generator
        int seed = ResourceUtil.getIntegerProperty(rb, "random.seed");
        if (seed == -1)
            rand = new Random();
        else
            rand = new Random(seed);
    }


    public static void initializeRandomNumber(Random randSetting) {
        rand = randSetting;
    }

    public static Random getRand () {
        return rand;
    }


    public static String getBaseDirectory() {
        return baseDirectory;
    }

    public static void setBaseDirectory(String baseDirectoryInput) {
        baseDirectory = baseDirectoryInput;
    }

    public static float rounder(float value, int digits) {
        // rounds value to digits behind the decimal point
        return Math.round(value * Math.pow(10, digits) + 0.5)/(float) Math.pow(10, digits);
    }

    public static ResourceBundle createResourceBundle (String fileName) {
        // read properties file and return as ResourceBundle
        File propFile = new File(fileName);
        return ResourceUtil.getPropertyBundle(propFile);
    }

    public static TableDataSet readCSVfile (String fileName) {
        // read csv file and return as TableDataSet
        File dataFile = new File(fileName);
        TableDataSet dataTable;
        boolean exists = dataFile.exists();
        if (!exists) {
            final String msg = "File not found: " + fileName;
            logger.error(msg);
//            System.exit(1);
            throw new RuntimeException(msg) ;
            // from the perspective of the junit testing infrastructure, a "System.exit(...)" is not a test failure ... and thus not detected.  kai, aug'16
        }
        try {
            TableDataFileReader reader = TableDataFileReader.createReader(dataFile);
            dataTable = reader.readFile(dataFile);
            reader.close();
        } catch (Exception e) {
            logger.error("Error reading file " + dataFile);
            throw new RuntimeException(e);
        }
        return dataTable;
    }

    public static int getHighestVal(int[] array) {
        // return highest number in int array
        int high = Integer.MIN_VALUE;
        for (int num: array) high = Math.max(high, num);
        return high;
    }


    public static int findPositionInArray (String element, String[] arr){
        // return index position of element in array arr
        int ind = -1;
        for (int a = 0; a < arr.length; a++) if (arr[a].equalsIgnoreCase(element)) ind = a;
        if (ind == -1) logger.error ("Could not find element " + element +
                " in array (see method <findPositionInArray> in class <SiloUtil>");
        return ind;
    }


    public static Integer getSum (Integer[] array) {
        Integer sm = 0;
        for (Integer value: array) sm += value;
        return sm;
    }

    public static float getSum (float[] array) {
        float sm = 0;
        for (float value: array) sm += value;
        return sm;
    }

    public static int getSum(int[][] array) {
        // return array of two-dimensional int array
        int sm = 0;
        for (int[] anArray : array) {
            for (int i = 0; i < array[0].length; i++)  sm += anArray[i];
        }
        return sm;
    }


    public static float getSum(float[][] array) {
        // return array of two-dimensional int array
        int sm = 0;
        for (float[] anArray : array) {
            for (int i = 0; i < array[0].length; i++)  sm += anArray[i];
        }
        return sm;
    }


    public static double getSum (double[] array) {
        // return sum of all elements in array
        double sum = 0;
        for (double val: array) sum += val;
        return sum;
    }


    public static float getSum(float[][][] array) {
        // return array of three-dimensional double array
        float sm = 0;
        for (float[][] anArray : array) {
            for (int i = 0; i < array[0][0].length; i++) {
                for (int j = 0; j < array[0].length; j++) sm += anArray[i][j];
            }
        }
        return sm;
    }


    static public String customFormat(String pattern, double value ) {
        // function copied from: http://docs.oracle.com/javase/tutorial/java/data/numberformat.html
        // 123456.789 ###,###.###  123,456.789 The pound sign (#) denotes a digit, the comma is a placeholder for the grouping separator, and the period is a placeholder for the decimal separator.
        // 123456.789 ###.##       123456.79   The value has three digits to the right of the decimal point, but the pattern has only two. The format method handles this by rounding up.
        // 123.78     000000.000   000123.780  The pattern specifies leading and trailing zeros, because the 0 character is used instead of the pound sign (#).
        // 12345.67   $###,###.### $12,345.67  The first character in the pattern is the dollar sign ($). Note that it immediately precedes the leftmost digit in the formatted output.
        DecimalFormat myFormatter = new DecimalFormat(pattern);
        return myFormatter.format(value);
    }


    public static int select (Random rand, double[] probabilities) {
        // select item based on probabilities (for zero-based double array)
        double selPos = getSum(probabilities) * rand.nextDouble();
        double sum = 0;
        for (int i = 0; i < probabilities.length; i++) {
            sum += probabilities[i];
            if (sum > selPos) {
                return i;
            }
        }
        return probabilities.length - 1;
    }


    public static int[] createIndexArray (int[] array) {
        // create indexArray for array

        int[] index = new int[getHighestVal(array) + 1];
        for (int i = 0; i < array.length; i++) {
            index[array[i]] = i;
        }
        return index;
    }


    public static float[] scaleArray (float[] array, float maxVal) {
        // scale float array so that largest value equals maxVal

        float highestVal = Float.MIN_VALUE;
        for (float val: array) highestVal = Math.max(val, highestVal);
        for (int i = 0; i < array.length; i++) array[i] = (float) ((array[i] * maxVal * 1.) / (highestVal * 1.));
        return array;
    }


    public static PrintWriter openFileForSequentialWriting(String fileName, boolean appendFile) {
        // open file and return PrintWriter object

        File outputFile = new File(fileName);
        try {
            FileWriter fw = new FileWriter(outputFile, appendFile);
            BufferedWriter bw = new BufferedWriter(fw);
            return new PrintWriter(bw);
        } catch (IOException e) {
            logger.error("Could not open file <" + fileName + ">.");
            return null;
        }
    }


    public static Matrix convertOmxToMatrix (OmxMatrix omxMatrix) {
        // convert OMX matrix into java matrix

        OmxHdf5Datatype.OmxJavaType type = omxMatrix.getOmxJavaType();
        String name = omxMatrix.getName();
        int[] dimensions = omxMatrix.getShape();
        if (type.equals(OmxHdf5Datatype.OmxJavaType.FLOAT)) {
            float[][] fArray = (float[][]) omxMatrix.getData();
            Matrix mat = new Matrix(name, name, dimensions[0], dimensions[1]);
            for (int i = 0; i < dimensions[0]; i++) {
                for (int j = 0; j < dimensions[1]; j++) {
                    mat.setValueAt(i + 1, j + 1, fArray[i][j]);
                }
            }
            return mat;
        } else if (type.equals(OmxHdf5Datatype.OmxJavaType.DOUBLE)) {
            double[][] dArray = (double[][]) omxMatrix.getData();
            Matrix mat = new Matrix(name, name, dimensions[0], dimensions[1]);
            for (int i = 0; i < dimensions[0]; i++) {
                for (int j = 0; j < dimensions[1]; j++) {
                    mat.setValueAt(i + 1, j + 1, (float) dArray[i][j]);
                }
            }
            return mat;
        } else {
            logger.info("OMX Matrix type " + type.toString() + " not yet implemented. Program exits.");
            System.exit(1);
            return null;
        }
    }

}
