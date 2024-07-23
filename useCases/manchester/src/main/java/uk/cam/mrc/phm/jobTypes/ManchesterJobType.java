package uk.cam.mrc.phm.jobTypes;

import de.tum.bgu.msm.data.jobTypes.Category;
import de.tum.bgu.msm.data.jobTypes.JobType;

public enum ManchesterJobType implements JobType{
    TOT (Category.OTHER);

    private final Category category;

    ManchesterJobType(Category category) {
        this.category = category;
    }

    @Override
    public Category getCategory() {
        return category;
    }
}
