package shiwenping.com.wechats;

import com.lidroid.xutils.db.annotation.Table;

/**
 * Created by bilinshengshi on 16/5/23.
 */
/*
 * HaiChecker
 * 16/5/23
 *
 */


@Table(name = "Page")
public class Page {
    private int id;
    private String name;
    private String number;
    private String wegroup;

    public String getWegroup() {
        return wegroup;
    }

    public void setWegroup(String wegroup) {
        this.wegroup = wegroup;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }


}
