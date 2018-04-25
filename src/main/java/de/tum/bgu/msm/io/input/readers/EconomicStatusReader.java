package de.tum.bgu.msm.io.input.readers;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.input.CSVReader;
import de.tum.bgu.msm.io.input.Input;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
//import jdk.internal.util.xml.impl.Input;
import org.apache.log4j.Logger;

import java.util.HashMap;


    public class EconomicStatusReader extends CSVReader {


        private static final Logger logger = Logger.getLogger(EconomicStatusReader.class);
        private int hhSizeFactorIndex;
        private int inc0_500Index;
        private int inc500_900Index;
        private int inc900_1500Index;
        private int inc1500_2000Index;
        private int inc2000_2600Index;
        private int inc2600_3000Index;
        private int inc3000_3600Index;
        private int inc3600_4000Index;
        private int inc4000_4600Index;
        private int inc4600_5000Index;
        private int inc5000_5600Index;
        private int inc5600_6000Index;
        private int inc6000_6600Index;
        private int inc6600_7000Index;
        private int inc7000plusIndex;

        public EconomicStatusReader(DataSet dataSet) {
            super(dataSet);
        }

        @Override
        public void read() {
            super.read(Resources.INSTANCE.getString(Properties.ECONOMIC_STATUS), ",");
        }

        @Override
        protected void processHeader(String[] header) {
            hhSizeFactorIndex = MitoUtil.findPositionInArray("hhSizeFactor", header);
            inc0_500Index     = MitoUtil.findPositionInArray("Inc0-500", header);
            inc500_900Index   = MitoUtil.findPositionInArray("Inc500-900", header);
            inc900_1500Index  = MitoUtil.findPositionInArray("Inc900-1500", header);
            inc1500_2000Index = MitoUtil.findPositionInArray("Inc1500-2000", header);
            inc2000_2600Index = MitoUtil.findPositionInArray("Inc2000-2600", header);
            inc2600_3000Index = MitoUtil.findPositionInArray("Inc2600-3000", header);
            inc3000_3600Index = MitoUtil.findPositionInArray("Inc3000-3600", header);
            inc3600_4000Index = MitoUtil.findPositionInArray("Inc3600-4000", header);
            inc4000_4600Index = MitoUtil.findPositionInArray("Inc4000-4600", header);
            inc4600_5000Index = MitoUtil.findPositionInArray("Inc4600-5000", header);
            inc5000_5600Index = MitoUtil.findPositionInArray("Inc5000-5600", header);
            inc5600_6000Index = MitoUtil.findPositionInArray("Inc5600-6000", header);
            inc6000_6600Index = MitoUtil.findPositionInArray("Inc6000-6600", header);
            inc6600_7000Index = MitoUtil.findPositionInArray("Inc6600-7000", header);
            inc7000plusIndex  = MitoUtil.findPositionInArray("Inc7000+", header);
        }

        @Override
        protected void processRecord(String[] record) {
            float hhSizeFactor = Float.parseFloat(record[hhSizeFactorIndex]);
            int inc0_500     = Integer.parseInt(record[inc0_500Index    ]);
            int inc500_900   = Integer.parseInt(record[inc500_900Index  ]);
            int inc900_1500  = Integer.parseInt(record[inc900_1500Index ]);
            int inc1500_2000 = Integer.parseInt(record[inc1500_2000Index]);
            int inc2000_2600 = Integer.parseInt(record[inc2000_2600Index]);
            int inc2600_3000 = Integer.parseInt(record[inc2600_3000Index]);
            int inc3000_3600 = Integer.parseInt(record[inc3000_3600Index]);
            int inc3600_4000 = Integer.parseInt(record[inc3600_4000Index]);
            int inc4000_4600 = Integer.parseInt(record[inc4000_4600Index]);
            int inc4600_5000 = Integer.parseInt(record[inc4600_5000Index]);
            int inc5000_5600 = Integer.parseInt(record[inc5000_5600Index]);
            int inc5600_6000 = Integer.parseInt(record[inc5600_6000Index]);
            int inc6000_6600 = Integer.parseInt(record[inc6000_6600Index]);
            int inc6600_7000 = Integer.parseInt(record[inc6600_7000Index]);
            int inc7000plus  = Integer.parseInt(record[inc7000plusIndex ]);
            HashMap<String, Integer> economicStatus = Input.getEconomicStatus();
            economicStatus.put(hhSizeFactor+"_Inc0_500", inc0_500);
            economicStatus.put(hhSizeFactor+"_Inc500_900", inc500_900);
            economicStatus.put(hhSizeFactor+"_Inc900_1500", inc900_1500);
            economicStatus.put(hhSizeFactor+"_Inc1500_2000", inc1500_2000);
            economicStatus.put(hhSizeFactor+"_Inc2000_2600", inc2000_2600);
            economicStatus.put(hhSizeFactor+"_Inc2600_3000", inc2600_3000);
            economicStatus.put(hhSizeFactor+"_Inc3000_3600", inc3000_3600);
            economicStatus.put(hhSizeFactor+"_Inc3600_4000", inc3600_4000);
            economicStatus.put(hhSizeFactor+"_Inc4000_4600", inc4000_4600);
            economicStatus.put(hhSizeFactor+"_Inc4600_5000", inc4600_5000);
            economicStatus.put(hhSizeFactor+"_Inc5000_5600", inc5000_5600);
            economicStatus.put(hhSizeFactor+"_Inc5600_6000", inc5600_6000);
            economicStatus.put(hhSizeFactor+"_Inc6000_6600", inc6000_6600);
            economicStatus.put(hhSizeFactor+"_Inc6600_7000", inc6600_7000);
            economicStatus.put(hhSizeFactor+"_Inc7000plus", inc7000plus);
            Input.setEconomicStatus(economicStatus);
        }
    }

