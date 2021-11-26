package de.tum.bgu.msm.run.calibration;

import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.munich.MunichImplementationConfig;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CalibrateMultipleDcOpenModels {

    public static void main(String[] args) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new FileReader(args[1]));
        String header[] = bufferedReader.readLine().split(",");
        int scenarioIndex = MitoUtil.findPositionInArray("scenario", header);
        int fileIndex = MitoUtil.findPositionInArray("filename", header);

        String line;
        while ((line = bufferedReader.readLine())!= null){
            System.out.println("Running scenario " + line.split(",")[scenarioIndex]);
            final String dcCoefFile = "input/destinationChoice/" +  line.split(",")[fileIndex];
            System.out.println("Using file " + dcCoefFile);
            CalibrateDestinationChoiceOpenModel model =
                    CalibrateDestinationChoiceOpenModel.standAloneModelMultipleRuns(args[0],
                            MunichImplementationConfig.get(),
                            dcCoefFile,
                            line.split(",")[scenarioIndex]);
            model.run();
        }

        bufferedReader.close();




    }
}
