package dbcache.test;

import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2015/1/18.
 */
//@javax.persistence.Entity
//@Table(name = "subEntity")
public class SubEntity extends Entity {


    private String subName;

    @Transient
    private Set<Integer> stranges = new HashSet<Integer>();




    public Set<Integer> getStranges() {
        return stranges;
    }

    public void setStranges(Set<Integer> stranges) {
        this.stranges = stranges;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }
}
