package de.tum.bgu.msm.util.concurrent;

import de.tum.bgu.msm.util.MitoUtil;

import java.util.Random;

public abstract class RandomizableConcurrentFunction implements ConcurrentFunction {

    protected final Random random;

    protected RandomizableConcurrentFunction(long randomSeed) {
        this.random = new Random(randomSeed);
    }

    @Override
    public abstract void execute();
}
