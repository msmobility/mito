package de.tum.bgu.msm.util.js;

import javax.script.*;
import java.io.FileNotFoundException;
import java.io.Reader;

/**
Skeleton class for java script calculations
 */
public abstract class JavaScriptCalculator<T> {

    private final CompiledScript compiledScript;
    protected Bindings bindings = new SimpleBindings();

    protected JavaScriptCalculator(Reader reader) throws ScriptException, FileNotFoundException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        Compilable compileEngine = (Compilable) engine;
        compiledScript = compileEngine.compile(reader);
    }

    protected JavaScriptCalculator(String script) throws ScriptException, FileNotFoundException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        Compilable compileEngine = (Compilable) engine;
        compiledScript = compileEngine.compile(script);
    }

    public T calculate(boolean log) {
        try {
            bindings.put("log", log);
            return (T) compiledScript.eval(bindings);
        } catch (ScriptException e) {
            e.printStackTrace();
            return null;
        }
    }
}
