package de.tum.bgu.msm.data.jobTypes.maryland;

import de.tum.bgu.msm.data.jobTypes.Category;
import de.tum.bgu.msm.data.jobTypes.JobType;

public enum MarylandJobType implements JobType {

    INDUSTRY (Category.INDUSTRY),
    OFFICE (Category.OFFICE),
    RETAIL (Category.RETAIL),
    OTHER (Category.OTHER);

    private final Category category;

    MarylandJobType(Category category) {
        this.category = category;
    }

    @Override
    public Category getCategory() {
        return category;
    }
}
