package de.tum.bgu.msm.util.js;

import org.apache.log4j.Logger;

import javax.script.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Skeleton class for java script calculations
 */
public abstract class JavaScriptCalculator<T> {

    protected static final Logger logger = Logger.getLogger(JavaScriptCalculator.class);

    private CompiledScript compiledScript;
    protected LoggableBindings bindings = new LoggableBindings();


    protected JavaScriptCalculator(Reader reader) {
        logger.debug("Reading script...");
        String script = readScript(reader);
        logger.debug("Compiling script: " + script);
        compileScript(script);
        bindings.put("logger", logger);
    }

    private String readScript(Reader reader) {
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder scriptBuilder = new StringBuilder();
        try {
            String line = bufferedReader.readLine();
            while (line != null) {
                scriptBuilder.append(line + "\n");
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            logger.fatal("Error in reading script!", e);
        }
        return scriptBuilder.toString();
    }

    private void compileScript(String script) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        Compilable compileEngine = (Compilable) engine;
        try {
            compiledScript = compileEngine.compile(script);
        } catch (ScriptException e) {
            logger.fatal("Error in input script!", e);
            e.printStackTrace();
        }
    }

    public T calculate() {
        try {
            bindings.logValues();
            return (T) compiledScript.eval(bindings);
        } catch (ScriptException e) {
            e.printStackTrace();
            return null;
        }
    }
}
