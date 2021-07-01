package com.aliyun.roompaas.app.model;

import com.aliyun.roompaas.app.enums.StudentStatus;
import com.aliyun.roompaas.app.util.ColorUtil;

import java.io.Serializable;

/**
 * @author puke
 * @version 2021/6/4
 */
public class StudentModel implements Serializable {

    public String id;

    public String nick;

    public StudentStatus status;

    public final int color = ColorUtil.randomColor();

    public boolean isTeacher;
}
