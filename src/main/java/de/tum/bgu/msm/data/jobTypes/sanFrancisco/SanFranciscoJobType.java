package de.tum.bgu.msm.data.jobTypes.sanFrancisco;

import de.tum.bgu.msm.data.jobTypes.Category;
import de.tum.bgu.msm.data.jobTypes.JobType;

public enum SanFranciscoJobType implements JobType{
    OTHER (Category.OTHER),
    INDUSTRY(Category.INDUSTRY),
    RETAIL(Category.RETAIL),
    OFFICE(Category.OFFICE);

    private final Category category;

    SanFranciscoJobType(Category category) {
        this.category = category;
    }

    @Override
    public Category getCategory() {
        return category;
    }
}
