package de.tum.bgu.msm.util.concurrent;

import de.tum.bgu.msm.util.MitoUtil;

import java.util.Random;

public abstract class RandomizableConcurrentFunction implements ConcurrentFunction {

    protected final Random random;

    protected RandomizableConcurrentFunction() {
        this.random = new Random(MitoUtil.getRandomObject().nextLong());
    }

    @Override
    public abstract void execute();
}
