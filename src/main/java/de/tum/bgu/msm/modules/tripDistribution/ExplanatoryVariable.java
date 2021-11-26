package de.tum.bgu.msm.modules.tripDistribution;

public class ExplanatoryVariable {

    public final static String expDistance_km= "expDistance_km";
    public final static String distance_km= "distance_km";
    public static String alphaDistance_km = "alphaDistance_km";
    public static String calibrationFactorAlphaDistance = "calibrationFactorAlphaDistance";
    public final static String attraction = "attraction";
    public final static String logAttraction= "logAttraction";
    public static String calibrationFactorBetaExpDistance = "calibrationFactorBetaExpDistance";
    public static String tomTomOdIntensity = "tomTomOdIntensity";
    public static String numberOfTweets = "twitterCount";
    public static String numberOfTweetsPerArea = "twitterCountDensity";
    public static String numberOfFlickrPics = "flickrCount";
    public static String osmOther = "other";
    public static String osmFood= "food";
    public static String osmRetail = "retail";
    public static String osmLeisure = "leisure";
    public static String osmEducation = "education";


    public static String[] getOsmTypes() {
        return new String[]{osmOther, osmEducation, osmFood, osmRetail, osmLeisure};
    }

    public static String[] getAllOptionalVars() {
        return  new String[]{osmOther, osmEducation, osmFood, osmRetail, osmLeisure, tomTomOdIntensity,
                numberOfFlickrPics, numberOfTweets, numberOfTweetsPerArea};
    }
}
