package com.lxw.videoworld.task;

import com.lxw.videoworld.dao.PhdyHotDao;
import com.lxw.videoworld.dao.PhdyNewDao;
import com.lxw.videoworld.dao.PhdySourceDao;
import com.lxw.videoworld.spider.*;
import com.lxw.videoworld.utils.URLUtil;
import org.springframework.beans.factory.annotation.Autowired;
import us.codecraft.webmagic.Spider;

import java.util.ArrayList;
import java.util.List;

//
//import com.lxw.videoworld.dao.PhdyHotDao;
//import com.lxw.videoworld.dao.PhdyNewDao;
//import com.lxw.videoworld.dao.PhdySourceDao;
//import com.lxw.videoworld.spider.*;
//import com.lxw.videoworld.utils.URLUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import us.codecraft.webmagic.Spider;
//
//import java.util.ArrayList;
//import java.util.List;
//
//
///**
// * Created by lxw9047 on 2017/4/20.
// */
////@Component("phdySourceTask")
public class PhdySourceTask {

    @Autowired
    private PhdyHotDao phdyHotDao;
    @Autowired
    private PhdyNewDao phdyNewDao;
    @Autowired
    private PhdySourceDao phdySourceDao;
    @Autowired
    private PhdyMenuListPipeline phdyMenuListPipeline;
    @Autowired
    private PhdyHotListPipeline phdyHotListPipeline;
    @Autowired
    private PhdyNewListPipeline phdyNewListPipeline;
    @Autowired
    private PhdySourceDetailPipeline phdySourceDetailPipeline;

    @Autowired
    private PhdyMenuPageProcessor phdyMenuPageProcessor;

    // 每天凌晨4点执行
//    @Scheduled(cron = "0 10,25 02 * * ?")
//    @Scheduled(cron = "0 0 10 * * ?")   //每天上午10:15触发   爬过一次
    public void getPhdySource() {
        try{
            // 清空排行榜
//        phdyHotDao.clear();
            //清空今日更新
//        phdyNewDao.clear();
        }catch (Exception e){
            e.printStackTrace();
        }

        List<String> menuUrlList = new ArrayList<>();
        menuUrlList.add(URLUtil.URL_PHDY_DONGZUO);
        menuUrlList.add(URLUtil.URL_PHDY_XIJU);
        menuUrlList.add(URLUtil.URL_PHDY_AIQING);
        menuUrlList.add(URLUtil.URL_PHDY_KEHUAN);
        menuUrlList.add(URLUtil.URL_PHDY_JUQING);
        menuUrlList.add(URLUtil.URL_PHDY_XUANYI);
        menuUrlList.add(URLUtil.URL_PHDY_WENYI);
        menuUrlList.add(URLUtil.URL_PHDY_ZHANZHENG);
        menuUrlList.add(URLUtil.URL_PHDY_KONGBU);
        menuUrlList.add(URLUtil.URL_PHDY_ZAINAN);
        menuUrlList.add(URLUtil.URL_PHDY_LIANXUJU);
        menuUrlList.add(URLUtil.URL_PHDY_DONGMAN);
        menuUrlList.add(URLUtil.URL_PHDY_ZONGYI);
        menuUrlList.add(URLUtil.URL_PHDY_HUAIJIU);
        // 飘花电影菜单
        Spider.create(phdyMenuPageProcessor).thread(5)
                .addUrl((String[])menuUrlList.toArray(new String[menuUrlList.size()]))
                .run();
        // 飘花电影排行榜

        Spider.create(new PhdyHotListProcessor()).thread(5)
                .addUrl((String[])menuUrlList.toArray(new String[menuUrlList.size()]))
                .addPipeline(phdyMenuListPipeline)
                .addPipeline(phdyHotListPipeline)
                .run();
        // 飘花电影今日更新
        Spider.create(new PhdyNewListProcessor()).thread(1)
                .addUrl(URLUtil.URL_PHDY_DIANYING)
                .addPipeline(phdyMenuListPipeline)
                .addPipeline(phdyNewListPipeline)
                .run();
    }

    // 每天凌晨5点执行
//    @Scheduled(cron = "0 0,30 04 * * ?")
//    @Scheduled(cron = "0 5 10 * * ?")   //每天啊上午10:15触发  1.12 执行一次
    public void getPhdySourceDetail() {
        // 飘花电影详情
        final List<String> urlList = phdySourceDao.findAllUrl();
        if (urlList != null && urlList.size() > 0) {
            Spider.create(new PhdySourceDetailProcessor()).thread(50)
                    .addUrl((String[]) urlList.toArray(new String[urlList.size()]))
                    .addPipeline(phdySourceDetailPipeline)
                    .run();
        }

    }
}
