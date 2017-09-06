package de.tum.bgu.msm.modules;

import org.junit.Test;

import javax.script.*;
import java.io.InputStreamReader;
import java.io.Reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JavaScriptCalculatorTest {

    private ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
    private Compilable compileEngine = (Compilable) engine;

    @Test
    public void calculagorTest() {

        int x = 0;
        int y = 0;

        Reader reader = new InputStreamReader(getClass().getResourceAsStream("TripDistributionCalculator.js"));
        try {
            CompiledScript cscript = compileEngine.compile(reader);
            for (int i = 0; i < 10; i++) {
                Bindings bindings = new SimpleBindings();
                bindings.put("x", x + i);
                bindings.put("y", y + i);
                Object result = cscript.eval(bindings);
                assertEquals(i * 2, (double) result, 0);
            }
        } catch (ScriptException e) {
            e.printStackTrace();
            fail();
        }
    }
}
