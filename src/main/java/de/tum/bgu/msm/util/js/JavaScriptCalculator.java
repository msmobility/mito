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

    protected CompiledScript compiledScript;
    protected Invocable invocable;


    protected JavaScriptCalculator(Reader reader) {
        logger.debug("Reading script...");
        String script = readScript(reader);
        logger.debug("Compiling script: " + script);
        compileScript(script);
    }

    private String readScript(Reader reader) {
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder scriptBuilder = new StringBuilder();
        try {
            String line = bufferedReader.readLine();
            while (line != null) {
                scriptBuilder.append(line).append("\n");
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
            compiledScript.eval();
            invocable = (Invocable) compiledScript.getEngine();
        } catch (ScriptException e) {
            logger.fatal("Error in input script!", e);
            e.printStackTrace();
        }
    }

    protected final T calculate(String function, Object... args) {
        try {
            return (T) invocable.invokeFunction(function, args);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
