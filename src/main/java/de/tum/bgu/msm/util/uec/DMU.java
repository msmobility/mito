package de.tum.bgu.msm.util.uec;

import com.pb.common.calculator2.IndexValues;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class DMU<T> implements com.pb.common.calculator2.VariableTable {

    protected transient Logger logger = Logger.getLogger(DMU.class);

    private Map<Integer, String> fieldByIndex;
    private final IndexValues dmuIndex;

    public DMU () {
        fieldByIndex = new HashMap<>();
        dmuIndex = new IndexValues();
    }

    @Override
    public int getIndexValue(String variableName) {
        fieldByIndex.put(fieldByIndex.size(), variableName);
        return fieldByIndex.size()-1;
    }

    @Override
    public int getAssignmentIndexValue(String variableName) {
        return 0;
    }

    @Override
    public double getValueForIndex(int variableIndex) {
        return getValueForIndex(variableIndex, 0);
    }

    @Override
    public double getValueForIndex(int variableIndex, int arrayIndex) {
        String field = fieldByIndex.get(variableIndex);
        try {
            Method method = this.getClass().getMethod(field);
            return Double.parseDouble(String.valueOf(method.invoke(this, null)));
        } catch (NoSuchMethodException e) {
            logger.error("Could not find defined field in DMU class");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            logger.error("Error in method loading");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            logger.error("Error in method loading");
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void setValue(String variableName, double variableValue) {
    }

    @Override
    public void setValue(int variableIndex, double variableValue) {

    }

    public IndexValues getDmuIndexValues() {
        return dmuIndex;
    }

    protected abstract void setup(T object);
}
