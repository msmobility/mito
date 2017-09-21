package de.tum.bgu.msm.util.js;

import org.apache.log4j.Logger;

import javax.script.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
Skeleton class for java script calculations
 */
public abstract class JavaScriptCalculator<T> {

    private static final Logger logger = Logger.getLogger(JavaScriptCalculator.class);

    private CompiledScript compiledScript;
    protected LoggableBindings bindings = new LoggableBindings();


    protected JavaScriptCalculator(Reader reader) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder scriptBuilder = new StringBuilder();
        String line = null;
        try {
            line = bufferedReader.readLine();
        } catch (IOException e) {
            logger.fatal("Error in reading script!", e);
        }
        while (line != null) {
            scriptBuilder.append(line);
        }
        logger.debug("Compiling script: " + scriptBuilder.toString());
        Compilable compileEngine = (Compilable) engine;
        try {
            compiledScript = compileEngine.compile(scriptBuilder.toString());
        } catch (ScriptException e) {
            logger.fatal("Error in input script!", e);
            e.printStackTrace();
        }
    }

    public T calculate(boolean log) {
        try {
            bindings.logValues();
            bindings.put("log", log);
            return (T) compiledScript.eval(bindings);
        } catch (ScriptException e) {
            e.printStackTrace();
            return null;
        }
    }
}
