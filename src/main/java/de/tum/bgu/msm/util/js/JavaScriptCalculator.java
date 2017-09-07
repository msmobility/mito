package de.tum.bgu.msm.util.js;

import de.tum.bgu.msm.util.Calculator;

import javax.script.*;
import java.io.FileNotFoundException;
import java.io.Reader;

public abstract class JavaScriptCalculator<T> implements Calculator<T> {

    private final CompiledScript script;
    protected final Bindings bindings = new SimpleBindings();

    protected JavaScriptCalculator(Reader reader) throws ScriptException, FileNotFoundException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        Compilable compileEngine = (Compilable) engine;
        script = compileEngine.compile(reader);
    }

    @Override
    public double calculate(boolean log, T object) {
        bindObject(object);
        try {
            return (double) script.eval(bindings);
        } catch (ScriptException e) {
            e.printStackTrace();
            return 0;
        }
    }

    protected abstract void bindObject(T object);
}
