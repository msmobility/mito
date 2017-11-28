package de.tum.bgu.msm.data.jobTypes.munich;

import de.tum.bgu.msm.data.jobTypes.Category;
import de.tum.bgu.msm.data.jobTypes.JobType;

public enum MunichJobType implements JobType{
    AGRI (Category.OTHER),
    MNFT(Category.INDUSTRY),
    UTIL(Category.INDUSTRY),
    CONS(Category.INDUSTRY),
    RETL(Category.RETAIL),
    TRNS(Category.OTHER),
    FINC(Category.OFFICE),
    RLST(Category.OFFICE),
    ADMN(Category.OFFICE),
    SERV(Category.OFFICE);

    private final Category category;

    MunichJobType(Category category) {
        this.category = category;
    }

    @Override
    public Category getCategory() {
        return category;
    }
}
