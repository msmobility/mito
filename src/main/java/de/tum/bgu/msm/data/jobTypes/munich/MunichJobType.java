package de.tum.bgu.msm.data.jobTypes.munich;

import de.tum.bgu.msm.data.jobTypes.Category;
import de.tum.bgu.msm.data.jobTypes.JobType;

public enum MunichJobType implements JobType{
    Agri (Category.OTHER),
    Mnft(Category.INDUSTRY),
    Util(Category.INDUSTRY),
    Cons(Category.INDUSTRY),
    Retl(Category.RETAIL),
    Trns(Category.OTHER),
    Finc(Category.OFFICE),
    Rlst(Category.OFFICE),
    Admn(Category.OFFICE),
    Serv(Category.OFFICE);

    private final Category category;

    MunichJobType(Category category) {
        this.category = category;
    }

    @Override
    public Category getCategory() {
        return category;
    }
}
