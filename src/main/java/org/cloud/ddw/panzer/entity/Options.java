package org.cloud.ddw.panzer.entity;

import com.baomidou.mybatisplus.annotations.IdType;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by d05660ddw on 2016/11/2.
 */

@Getter
@Setter
@TableName("blog")
public class Options implements Serializable {
    /**
     *
     */

    @TableField(exist = false)
    private static final long serialVersionUID = 5061104505523594100L;

    @TableId(type = IdType.AUTO)
    private long id;
    private String name;
    private String value;
    private String autoload;

    public Options() {
    }

    public Options(String name, String value) {
        this.name = name;
        this.value = value;
        this.autoload = "yes";
    }

}
