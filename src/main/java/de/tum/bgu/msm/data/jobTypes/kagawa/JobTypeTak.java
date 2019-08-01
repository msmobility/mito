package de.tum.bgu.msm.data.jobTypes.kagawa;

import de.tum.bgu.msm.data.jobTypes.Category;
import de.tum.bgu.msm.data.jobTypes.JobType;

public enum JobTypeTak implements JobType {

    AGRI (Category.OTHER),
    MNFT(Category.INDUSTRY),
    SERV(Category.OFFICE);

    private final Category category;

    JobTypeTak(Category category) {
        this.category = category;
    }

    @Override
    public Category getCategory() {
        return category;
    }
}
