package org.cloud.ddw.panzer.utils;

import freemarker.core.Environment;
import freemarker.template.*;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Created by d05660ddw on 2016/11/2.
 */
public class PaginationTag implements TemplateDirectiveModel {

    private static final String ellipseText = "...";    // 默认是...
    private static final int displayEntries = 10;       // 当前显示分页按钮的数目
    private static final String prevText = "Prev";
    private static final String nextText = "Next";

    private boolean prevShow = false;                   // 显示前一个
    private boolean nextShow = false;                   // 显示后一个
    private int totalRecord;                            // 总记录数
    private int currentPage;                            // 当前页码,第几页
    private int pageSize;                               // 每页显示的记录数,每页显示多少条数据
    private int edgeEntries = 0;	                    // edge number
    private int pageNumber;                             // 最大页码


    private int getStart(int currentPage, int pageNumber) {
        int ne_half = (int) Math.ceil((double)displayEntries /2);
        int upper_limit = pageNumber - displayEntries;
        int first = 1;
        if(currentPage > ne_half) {
            first = Math.max(Math.min(currentPage-ne_half, upper_limit), 1);
        }
        return first;
    }

    private int getEnd(int currentPage, int pageNumber) {
        int ne_half = (int) Math.ceil((double)displayEntries /2);
        int last = 1;
        if(currentPage>ne_half) {
            last = Math.min(currentPage+ne_half, pageNumber);
        } else {
            last = Math.min(displayEntries, pageNumber);
        }
        return last;
    }

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
            throws TemplateException, IOException {

        int page_c = 1;
        SimpleScalar scalar = (SimpleScalar) params.get("page_c");
        if (scalar != null) {
            page_c = Integer.parseInt(scalar.getAsString());
        } else {
            throw new TemplateException("@paginate 'page_c' attribute is not defined", env);
        }

        int page_t = 1;
        scalar = (SimpleScalar) params.get("page_t");
        if (scalar != null) {
            page_t = Integer.parseInt(scalar.getAsString());
        } else {
            throw new TemplateException("@paginate 'page_t' attribute is not defined", env);
        }

        int page_s = 1;
        scalar = (SimpleScalar) params.get("page_s");
        if (scalar != null) {
            page_s = Integer.parseInt(scalar.getAsString());
        } else {
            throw new TemplateException("@paginate 'page_s' attribute is not defined", env);
        }

        String url = params.get("url").toString();
        String suffix = null;
        if(params.get("suffix")!=null) {
            suffix = params.get("suffix").toString();
        }

        Writer out = env.getOut();

        System.out.println(page_c+","+page_t+","+page_s);

        currentPage = page_c;
        totalRecord = page_t;
        pageSize = page_s;

        pageNumber = (int) Math.ceil((double)totalRecord/pageSize) + 1;
        currentPage = currentPage<1 ? 1 : (currentPage<pageNumber?currentPage:pageNumber);

        int start = getStart(currentPage, pageNumber);
        int end = getEnd(currentPage, pageNumber);

        //init
        out.append("<div id=\"pages\"><ul>");

        // Generate "Previous"-Link
        if((prevText!=null)&&(currentPage > 1 || prevShow)){
            out.append("<li class=\"prev\">")
                    .append("<a href=\"")
                    .append(url)
                    .append("?page=")
                    .append(String.valueOf(currentPage));
            if(suffix!=null){
                out.append(suffix);
            }
            out.append("\">")
                    .append(prevText)
                    .append("</a></li>");
        }

        // Generate starting points
        if (start > 1 && edgeEntries > 1) {
            int last = Math.min(edgeEntries, start);
            for(int i=1; i<last; i++) {
                out.append("<li>")
                        .append("<a href=\"")
                        .append(url)
                        .append("?page=")
                        .append(String.valueOf(i));
                if(suffix!=null){
                    out.append(suffix);
                }
                out.append("\">")
                        .append(String.valueOf(i))
                        .append("</a></li>");
            }
            if((edgeEntries < start)&&(ellipseText != null)){
                out.append("<span>")
                        .append(ellipseText)
                        .append("</span>");
            }
        }

        // Generate interval links
        for(int i=start; i<end; i++) {
            if(i==currentPage) {
                out.append("<li class=\"current\"><a>")
                        .append(String.valueOf(i))
                        .append("</a></li>");
            } else {
                out.append("<li>")
                        .append("<a href=\"")
                        .append(url)
                        .append("?page=")
                        .append(String.valueOf(i));
                if(suffix!=null){
                    out.append(suffix);
                }
                out.append("\">")
                        .append(String.valueOf(i))
                        .append("</a></li>");
            }
        }

        // Generate ending points
        if((end < pageNumber)&&(edgeEntries>0)){
            if(((pageNumber-edgeEntries)> end)&&(ellipseText != null)){
                out.append("<span>")
                        .append(ellipseText)
                        .append("</span>");
            }
            int first = Math.max(pageNumber-edgeEntries, end);
            for(int i=first; i<pageNumber; i++) {
                out.append("<li>")
                        .append("<a href=\"")
                        .append(url)
                        .append("?page=")
                        .append(String.valueOf(i));
                if(suffix!=null){
                    out.append(suffix);
                }
                out.append("\">")
                        .append(String.valueOf(i))
                        .append("</a></li>");
            }
        }

        // Generate "Next"-Link
        if((nextText!=null)&&(currentPage <(pageNumber-1) || nextShow)) {
            out.append("<li class=\"next\">")
                    .append("<a href=\"")
                    .append(url)
                    .append("?page=")
                    .append(String.valueOf(currentPage+1));
            if(suffix!=null){
                out.append(suffix);
            }
            out.append("\">")
                    .append(nextText)
                    .append("</a></li>");
        }

        out.append("</ul></div>");
        if (body != null) {
            body.render(env.getOut());
        }
    }
}
