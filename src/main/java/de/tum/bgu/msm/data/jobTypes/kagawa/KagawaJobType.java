package de.tum.bgu.msm.data.jobTypes.kagawa;

import de.tum.bgu.msm.data.jobTypes.Category;
import de.tum.bgu.msm.data.jobTypes.JobType;

public enum KagawaJobType implements JobType {

    AGR (Category.OTHER),
    IND(Category.INDUSTRY),
    SRV(Category.OFFICE);

    private final Category category;

    KagawaJobType(Category category) {
        this.category = category;
    }

    @Override
    public Category getCategory() {
        return category;
    }
}
