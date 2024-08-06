package de.tum.bgu.msm.scenarios.tengos;

import de.tum.bgu.msm.data.jobTypes.Category;
import de.tum.bgu.msm.data.jobTypes.JobType;

public enum MunichJobTypeTengos implements JobType {
    AGRI(Category.OTHER),
    MNFT(Category.INDUSTRY),
    UTIL(Category.INDUSTRY),
    CONS(Category.INDUSTRY),
    RETL(Category.RETAIL),
    TRNS(Category.OTHER),
    FINC(Category.OFFICE),
    RLST(Category.OFFICE),
    ADMN(Category.OFFICE),
    SERV(Category.OFFICE),
    NURSINGHOME(Category.OFFICE);

    private final Category category;

    MunichJobTypeTengos(Category category) {
        this.category = category;
    }

    @Override
    public Category getCategory() {
        return category;
    }

}
