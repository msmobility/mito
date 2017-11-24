package de.tum.bgu.msm.data.jobTypes.maryland;

import de.tum.bgu.msm.data.jobTypes.Category;
import de.tum.bgu.msm.data.jobTypes.JobType;

public enum MarylandJobType implements JobType {

    IND (Category.INDUSTRY),
    OFF (Category.OFFICE),
    RET (Category.RETAIL),
    OTH (Category.OTHER);

    private final Category category;

    MarylandJobType(Category category) {
        this.category = category;
    }

    @Override
    public Category getCategory() {
        return category;
    }
}
