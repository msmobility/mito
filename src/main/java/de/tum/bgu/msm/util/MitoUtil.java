package de.tum.bgu.msm.util;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Utilities for the Transport in Microsimulation Orchestrator (TIMO)
 *
 * @author Rolf Moeckel / Nico Kühnel
 * Created on Sep 18, 2016 in Munich, Germany
 */

public final class MitoUtil {

    private MitoUtil() {
    }

    private static final Logger logger = Logger.getLogger(MitoUtil.class);
    private static Random rand;
    private static String baseDirectory = "";


    public static void initializeRandomNumber() {
        int seed = Resources.INSTANCE.getInt(Properties.RANDOM_SEED);
        rand = new Random(seed);
    }

    public static void initializeRandomNumber(Random randSetting) {
        rand = randSetting;
    }

    public static String getBaseDirectory() {
        return baseDirectory;
    }

    public static void setBaseDirectory(String baseDirectoryInput) {
        baseDirectory = baseDirectoryInput;
    }

    public static float rounder(float value, int digits) {
        // rounds value to digits behind the decimal point
        return Math.round(value * Math.pow(10, digits) + 0.5) / (float) Math.pow(10, digits);
    }

    private static int getHighestVal(int[] array) {
        // return highest number in int array
        int high = Integer.MIN_VALUE;
        for (int num : array) {
            high = Math.max(high, num);
        }
        return high;
    }

    public static int findPositionInArray(String element, String[] arr) {
        // return index position of element in array arr
        int ind = -1;
        for (int a = 0; a < arr.length; a++) {
            if (arr[a].equalsIgnoreCase(element)) {
                ind = a;
            }
        }
        if (ind == -1) {
            logger.error("Could not find element " + element +
                    " in array (see method <findPositionInArray> in class <SiloUtil>");
        }
        return ind;
    }

    public static Integer getSum(Integer[] array) {
        Integer sm = 0;
        for (Integer value : array) {
            sm += value;
        }
        return sm;
    }

    public static float getSum(float[] array) {
        float sm = 0;
        for (float value : array) {
            sm += value;
        }
        return sm;
    }

    private static double getSum(double[] array) {
        double sum = 0;
        for (double val : array) {
            sum += val;
        }
        return sum;
    }


    private static double getSum(Collection<Double> values) {
        double sm = 0;
        for (Double value : values) {
            sm += value;
        }
        return sm;
    }


    static public String customFormat(String pattern, double value) {
        // function copied from: http://docs.oracle.com/javase/tutorial/java/data/numberformat.html
        // 123456.789 ###,###.###  123,456.789 The pound sign (#) denotes a digit, the comma is a placeholder for the grouping separator, and the period is a placeholder for the decimal separator.
        // 123456.789 ###.##       123456.79   The value has three digits to the right of the decimal point, but the pattern has only two. The format method handles this by rounding up.
        // 123.78     000000.000   000123.780  The pattern specifies leading and trailing zeros, because the 0 character is used instead of the pound sign (#).
        // 12345.67   $###,###.### $12,345.67  The first character in the pattern is the dollar sign ($). Note that it immediately precedes the leftmost digit in the formatted output.
        DecimalFormat myFormatter = new DecimalFormat(pattern);
        return myFormatter.format(value);
    }

    public static int select(double[] probabilities, Random random) {
        // select item based on probabilities (for zero-based double array)
        double selPos = getSum(probabilities) * random.nextDouble();
        double sum = 0;
        for (int i = 0; i < probabilities.length; i++) {
            sum += probabilities[i];
            if (sum > selPos) {
                return i;
            }
        }
        return probabilities.length - 1;
    }

    public static int select(double[] probabilities) {
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

    public static int select(float[] probabilities, Random random) {
        double selPos = getSum(probabilities) * random.nextDouble();
        double sum = 0;
        for (int i = 0; i < probabilities.length; i++) {
            sum += probabilities[i];
            if (sum > selPos) {
                return i;
            }
        }
        return probabilities.length - 1;
    }

    public static <T> T select(Map<T, Double> mappedProbabilities) {
        // select item based on probabilities (for mapped double probabilities)
        return select(mappedProbabilities, getSum(mappedProbabilities.values()));
    }

    public static <T> T select(Map<T, Double> mappedProbabilities, double sum) {
        return select(mappedProbabilities, rand, sum);
    }

    public static <T> T select(Map<T, Double> mappedProbabilities, Random random) {
        return select(mappedProbabilities, random, getSum(mappedProbabilities.values()));
    }

    public static <T> T select(Map<T, Double> probabilities, Random random, double sum) {
        // select item based on probabilities (for mapped double probabilities)
        double selectedWeight = random.nextDouble() * sum;
        double select = 0;
        for (Map.Entry<T, Double> entry : probabilities.entrySet()) {
            select += entry.getValue();
            if (select > selectedWeight) {
                return entry.getKey();
            }
        }
        throw new RuntimeException("Error selecting item from weighted probabilities");
    }



    public static <T> T select(Random rand, T... objects) {
        return objects[rand.nextInt(objects.length)];
    }

    public static <T> T select(List<T> objects) {
        return objects.get(rand.nextInt(objects.size()));
    }

    public static <T> T select(Random rand, List<T> objects) {
        return objects.get(rand.nextInt(objects.size()));
    }

    public static int[] createIndexArray(int[] array) {
        // create indexArray for array

        int[] index = new int[getHighestVal(array) + 1];
        for (int i = 0; i < array.length; i++) {
            index[array[i]] = i;
        }
        return index;
    }


    public static float[] scaleArray(float[] array, float maxVal) {
        // scale float array so that largest value equals maxVal

        float highestVal = Float.MIN_VALUE;
        for (float val : array) {
            highestVal = Math.max(val, highestVal);
        }
        for (int i = 0; i < array.length; i++) {
            array[i] = (float) ((array[i] * maxVal * 1.) / (highestVal * 1.));
        }
        return array;
    }

    public static void scaleMapTo(Map<?, Float> map, float maxVal) {
        // scale float value map so that largest value equals maxVal

        float highestValueTmp = Float.MIN_VALUE;
        for (Float value : map.values()) {
            highestValueTmp = Math.max(value, highestValueTmp);
        }
        final float highestValue = highestValueTmp;
        map.replaceAll((k, v) -> (float) ((v * maxVal * 1.) / (highestValue * 1.)));
    }

    public static void scaleMapTo(Map<?, Double> map, double maxVal) {
        // scale double value map so that largest value equals maxVal

        double highestValueTmp = Double.MIN_VALUE;
        for (Double value : map.values()) {
            highestValueTmp = Math.max(value, highestValueTmp);
        }
        final double highestValue = highestValueTmp;
        map.replaceAll((k, v) -> (double) ((v * maxVal * 1.) / (highestValue * 1.)));
    }

    public static void scaleMapBy(Map<?, Float> map, float by) {
        // scale float value map so that each values is multiplied by the given value
        map.replaceAll((k, v) -> (float) ((v * by)));
    }

    public static void scaleMapBy(Map<?, Double> map, double by) {
        // scale double value map so that each values is multiplied by the given value

        map.replaceAll((k, v) -> (double) ((v * by)));
    }


    public static PrintWriter openFileForSequentialWriting(String fileName, boolean appendFile) {
        // open file and return PrintWriter object

        File outputFile = new File(fileName);
        if(outputFile.getParent() != null) {
            File parent = outputFile.getParentFile();
            parent.mkdirs();
        }

        try {
            FileWriter fw = new FileWriter(outputFile, appendFile);
            BufferedWriter bw = new BufferedWriter(fw);
            return new PrintWriter(bw);
        } catch (IOException e) {
            logger.error("Could not open file <" + fileName + ">.");
            return null;
        }
    }

    public static Random getRandomObject() {
        return rand;
    }
}
