package de.tum.bgu.msm.util.concurrent;

import com.pb.sawdust.calculator.Function1;
import com.pb.sawdust.util.concurrent.ForkJoinPoolFactory;
import com.pb.sawdust.util.concurrent.IteratorAction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class ConcurrentFunctionExecutor {

    private final List<ConcurrentFunction> functions = new ArrayList<>();

    public void addFunction(ConcurrentFunction function) {
        this.functions.add(function);
    }

    public void execute() {
        Function1<ConcurrentFunction, Void> concurrentFunction = function -> {
            function.execute();
            return null;
        };

        Iterator<ConcurrentFunction> concurrentFunctionIterator = functions.listIterator();
        IteratorAction<ConcurrentFunction> itTask = new IteratorAction<>(concurrentFunctionIterator, concurrentFunction);
        ForkJoinPool pool = ForkJoinPoolFactory.getForkJoinPool();
        pool.execute(itTask);
        itTask.waitForCompletion();
    }
}
