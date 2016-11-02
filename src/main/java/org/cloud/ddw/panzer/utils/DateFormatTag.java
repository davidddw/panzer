package org.cloud.ddw.panzer.utils;

import freemarker.core.Environment;
import freemarker.template.*;
import org.apache.commons.lang3.time.DateUtils;

import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Created by d05660ddw on 2016/11/2.
 */
public class DateFormatTag  implements TemplateDirectiveModel {
    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
            throws TemplateException, IOException {
        String text = "";
        // 标签上的text属性上的值
        if (params.get("date") != null) {
            text = ((SimpleScalar) params.get("date")).getAsString();
        }
        Writer out = env.getOut();

        out.append(getOffsetTime(text));
        if (body != null) {
            body.render(env.getOut());
        }
    }

    private String getOffsetTime(String newTime) {
        try {
            Date lastPost = DateUtils.parseDate(newTime, new String[]{"yyyy-MM-dd HH:mm:ss.SSS"});
            Date now = new Date();
            long duration = (now.getTime() - lastPost.getTime()) / 1000;
            if (duration < 60) {
                return duration + "秒前";
            } else if (duration < 3600) {
                return duration / 60 + "分钟前";
            } else if (duration < 86400) {
                return duration / 3600 + "小时前";
            } else {
                Date zeroTime = DateUtils.truncate(now, Calendar.DATE);
                long offset_day = (zeroTime.getTime() - lastPost.getTime()) / 1000 / 3600 / 24;
                if (offset_day < 1) {
                    return "昨天";
                } else if (offset_day < 2) {
                    return "前天";
                } else if (offset_day < 365) {
                    return offset_day + "天前";
                } else {
                    return offset_day / 365 + "年前";
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }
}
