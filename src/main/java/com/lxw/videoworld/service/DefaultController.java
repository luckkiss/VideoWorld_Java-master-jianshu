package com.lxw.videoworld.service;

import com.lxw.videoworld.config.Constants;
import com.lxw.videoworld.dao.*;
import com.lxw.videoworld.domain.*;
import com.lxw.videoworld.spider.DiaoSiSearchProcessor;
import com.lxw.videoworld.spider.ZhongziSearchPipeline;
import com.lxw.videoworld.spider.ZhongziSearchProcessor;
import com.lxw.videoworld.utils.ErrorUtil;
import com.lxw.videoworld.utils.ResponseUtil;
import com.lxw.videoworld.version.ApiVersion;
import org.apache.http.util.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import us.codecraft.webmagic.Spider;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.lxw.videoworld.service.WebSocketController.session;

/**
 * Created by Zion on 2017/6/3.
 */

@Controller
@RequestMapping("/{version}/")
public class DefaultController {
    private static final int BANNER_LIMIT = 5;
    @Autowired
    private ConfigDao configDao;
    @Autowired
    private MpdySourceDetailDao mpdySourceDetailDao;
    @Autowired
    private PhdySourceDetailDao phdySourceDetailDao;
    @Autowired
    private YgdySourceDetailDao ygdySourceDetailDao;
    @Autowired
    private SearchDao searchDao;
    @Autowired
    private ZhongziSearchPipeline zhongziSearchPipeline;
    @Autowired
    private FeedbackDao feedbackDao;
    @Autowired
    private UserInfoDao userInfoDao;
//    @Autowired
//    private PhdyHotDao phdyHotDao;
//    @Autowired
//    private PhdyNewDao phdyNewDao;
//    @Autowired
//    private YgdyClassicalDao ygdyClassicalDao;
//    @Autowired
//    private YgdyHotDao ygdyHotDao;

    @RequestMapping(value = "config")
    @ApiVersion(1)
    @ResponseBody
    public String getConfig(HttpServletRequest request) {

//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        try {
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String id = request.getParameter("id");
        String response = "";
        if (TextUtils.isEmpty(id)) {
            response = ResponseUtil.formatResponse(ErrorUtil.CODE_ERROR_PARAM, ErrorUtil.MESSAGE_ERROR_PARAM);
            return response;
        }
        Config config = configDao.findOneById(id);
        if (config != null) {
            response = ResponseUtil.formatResponse(config);
        } else {
            response = ResponseUtil.formatResponse(ErrorUtil.CODE_ERROR_NO_DATA, ErrorUtil.MESSAGE_ERROR_NO_DATA);
        }
        return response;
    }

    @RequestMapping(value = "list")
    @ApiVersion(1)
    @ResponseBody
    public String getList(HttpServletRequest request) {
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String sourceType = request.getParameter("sourceType");
        String category = request.getParameter("category");
        String type = request.getParameter("type");
        String startStr = request.getParameter("start");
        String limitStr = request.getParameter("limit");
        String response = "";
        int start, limit;
        if (TextUtils.isEmpty(sourceType) || TextUtils.isEmpty(category) || TextUtils.isEmpty(startStr) || TextUtils.isEmpty(limitStr)) {
            response = ResponseUtil.formatResponse(ErrorUtil.CODE_ERROR_PARAM, ErrorUtil.MESSAGE_ERROR_PARAM);
            return response;
        }
        try {
            start = Integer.valueOf(startStr);
            limit = Integer.valueOf(limitStr);
        } catch (Exception e) {
            e.printStackTrace();
            response = ResponseUtil.formatResponse(ErrorUtil.CODE_ERROR_PARAM, ErrorUtil.MESSAGE_ERROR_PARAM);
            return response;
        }
        Map<String, Object> map = new HashMap<>();
        List<SourceDetail> list = new ArrayList<>();
        switch (sourceType) {
            case Constants.SOURCE_TYPE_1:
                if (!TextUtils.isEmpty(type) && type.equals(Constants.TYPE_0)) {
                    list = phdySourceDetailDao.getDYRecord(start, limit);
                } else {
                    list = phdySourceDetailDao.getRecordByType(start, limit, category, type);
                }
                break;
            case Constants.SOURCE_TYPE_2:
                list = mpdySourceDetailDao.getRecordByType(start, limit, category, type);
                break;
            case Constants.SOURCE_TYPE_3:
                list = ygdySourceDetailDao.getRecordByType(start, limit, category, type);
                break;
            default:
                response = ResponseUtil.formatResponse(ErrorUtil.CODE_ERROR_PARAM, ErrorUtil.MESSAGE_ERROR_PARAM);
                return response;
        }
        if (list != null) {
            map.put("list", list);
            response = ResponseUtil.formatResponse(map);
        } else {
            response = ResponseUtil.formatResponse(ErrorUtil.CODE_ERROR_NO_DATA, ErrorUtil.MESSAGE_ERROR_NO_DATA);
        }
        return response;
    }

    @RequestMapping(value = "detail")
    @ApiVersion(1)
    @ResponseBody
    public String getDetail(HttpServletRequest request) {
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String sourceType = request.getParameter("sourceType");
        String url = request.getParameter("url");
        String response = "";
        if (TextUtils.isEmpty(sourceType) || TextUtils.isEmpty(url)) {
            response = ResponseUtil.formatResponse(ErrorUtil.CODE_ERROR_PARAM, ErrorUtil.MESSAGE_ERROR_PARAM);
            return response;
        }
        SourceDetail detail = new SourceDetail();
        switch (sourceType) {
            case Constants.SOURCE_TYPE_1:
                detail = phdySourceDetailDao.findOneById(url);
                break;
            case Constants.SOURCE_TYPE_2:
                detail = mpdySourceDetailDao.findOneById(url);
                break;
            case Constants.SOURCE_TYPE_3:
                detail = ygdySourceDetailDao.findOneById(url);
                break;
            default:
                response = ResponseUtil.formatResponse(ErrorUtil.CODE_ERROR_PARAM, ErrorUtil.MESSAGE_ERROR_PARAM);
                return response;
        }
        if (detail != null) {
            response = ResponseUtil.formatResponse(detail);
        } else {
            response = ResponseUtil.formatResponse(ErrorUtil.CODE_ERROR_NO_DATA, ErrorUtil.MESSAGE_ERROR_NO_DATA);
        }
        return response;
    }

    /*已请求，，，，根据 url 和关键字来搜索种子   已处理*/
    @RequestMapping(value = "search")
    @ApiVersion(1)
    @ResponseBody
    public String getSearch(HttpServletRequest request) {
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String uid = request.getParameter("uid");
        String url = request.getParameter("url");
        String keyword = request.getParameter("keyword");
        String searchType = request.getParameter("searchType");
//        uid = "uid";
//        url = "http://www.diaosisou.net/list/%E7%BE%8E%E5%9B%BD%E9%98%9F%E9%95%BF3/1";
//        keyword = "keyword";
//        searchType = "2";
        String response = "";
        if (TextUtils.isEmpty(uid) || TextUtils.isEmpty(url) || TextUtils.isEmpty(keyword) || TextUtils.isEmpty(searchType)) {
            response = ResponseUtil.formatResponse(ErrorUtil.CODE_ERROR_PARAM, ErrorUtil.MESSAGE_ERROR_PARAM);
            return response;
        }
        switch (searchType) {
            case Constants.SEARCH_TYPE_1:
                Spider.create(new ZhongziSearchProcessor(uid, keyword)).thread(1)
                        .addUrl(url)
                        .addPipeline(zhongziSearchPipeline)
                        .run();
                break;
            case Constants.SEARCH_TYPE_2:
                Spider.create(new DiaoSiSearchProcessor(uid, keyword)).thread(1)
                        .addUrl(url)
                        .addPipeline(zhongziSearchPipeline)
                        .run();
                break;
            default:
                response = ResponseUtil.formatResponse(ErrorUtil.CODE_ERROR_PARAM, ErrorUtil.MESSAGE_ERROR_PARAM);
                return response;
        }
        response = ResponseUtil.formatResponse(ErrorUtil.CODE_SUCCESS, ErrorUtil.MESSAGE_SUCCESS);
        return response;
    }

    /*已请求，，，，根据 url 和关键字来搜索种子   已处理*/
    @RequestMapping(value = "searchResult")
    @ApiVersion(1)
    @ResponseBody
    public String getSearchResult(HttpServletRequest request) {
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String uid = request.getParameter("uid");
        String url = request.getParameter("url");
//        uid = "uid";
//        url = "http://www.diaosisou.net/list/%E7%BE%8E%E5%9B%BD%E9%98%9F%E9%95%BF3/1";
        String response = "";
        if (TextUtils.isEmpty(uid) || TextUtils.isEmpty(url)) {
            response = ResponseUtil.formatResponse(ErrorUtil.CODE_ERROR_PARAM, ErrorUtil.MESSAGE_ERROR_PARAM);
            return response;
        }
        List<Search> list = searchDao.getRecordByParams(uid, url);
        if (list != null && list.size() > 0) {
            response = ResponseUtil.formatResponse(list.get(0));
        } else {
            response = ResponseUtil.formatResponse(ErrorUtil.CODE_ERROR_NO_DATA, ErrorUtil.MESSAGE_ERROR_EMPTY);
        }
        return response;
    }

    /*已请求，，，，用户反馈*/
    @RequestMapping(value = "feedback")
    @ApiVersion(1)
    @ResponseBody
    public String addFeedback(HttpServletRequest request) {
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String uid = request.getParameter("uid");
        String feedbackContent = request.getParameter("feedback");
        String response = "";
        if (TextUtils.isEmpty(uid) || TextUtils.isEmpty(feedbackContent)) {
            response = ResponseUtil.formatResponse(ErrorUtil.CODE_ERROR_PARAM, ErrorUtil.MESSAGE_ERROR_PARAM);
            return response;
        }
        Feedback feedback = new Feedback();
        feedback.setUid(uid);
        feedback.setFeedback(feedbackContent);
        feedback.setStatus("1");
        int flag = feedbackDao.add(feedback);
        if (flag == 1) {
            response = ResponseUtil.formatResponse(ErrorUtil.CODE_SUCCESS, ErrorUtil.MESSAGE_SUCCESS);


            if (WebSocketController.session != null) {
                try {
                    WebSocketController.session.getBasicRemote().sendText(feedbackContent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {
            response = ResponseUtil.formatResponse(ErrorUtil.CODE_ERROR_SQL, ErrorUtil.MESSAGE_ERROR_SQL);
        }
        return response;
    }


    /* 获取所有用户反馈 接口 */
    @RequestMapping(value = "getFeedBacks", method = RequestMethod.GET)
    @ApiVersion(1)
    @ResponseBody
    public String getFeedBacks(HttpServletRequest request) {
        String response = "";
        List<Feedback> feedbacks = feedbackDao.findAll();

        if (feedbacks != null) {
            response = ResponseUtil.formatResponse(feedbacks);
        } else {
            response = ResponseUtil.formatResponse(ErrorUtil.CODE_ERROR_SQL, ErrorUtil.MESSAGE_ERROR_SQL);
        }
        return response;
    }


    @RequestMapping(value = "getFeedBacksJsp", method = RequestMethod.GET)
    @ApiVersion(1)
//    public String getFeedBacksJsp(HttpServletRequest request) {
    public String getFeedBacksJsp(Model model) {
        String response = "";
        List<Feedback> feedbacks = feedbackDao.findAll();

        if (feedbacks != null) {
            response = ResponseUtil.formatResponse(feedbacks);
        } else {
            response = ResponseUtil.formatResponse(ErrorUtil.CODE_ERROR_SQL, ErrorUtil.MESSAGE_ERROR_SQL);
        }

        model.addAttribute("list", feedbacks);

        /**
         *      model.addAttribute("list", list);
         return "list";// WEB-INF/jsp/"list".jsp
         */
        return "feedList";
    }


    @RequestMapping(value = "doFeedBackById/{id}/{flag}", method = RequestMethod.GET)
    @ApiVersion(1)
    public String doFeedBackById(Model model, @PathVariable String id, @PathVariable int flag) {

        Feedback feedback = feedbackDao.findOneById(id);

        if (feedback != null) {
            feedback.setStatus(flag + "");
            int code = feedbackDao.update(feedback);
            Logger.getLogger("---------------------").log(Level.ALL, code + "");
        }


        List<Feedback> feedbacks = feedbackDao.findAll();
        model.addAttribute("list", feedbacks);
//        return getFeedBacksJsp(model);
        return "feedList";
    }


    /**
     * http://localhost/v1/doFeedBack/1/0
     *
     * @param id
     * @param flag
     * @return
     */

    @RequestMapping(value = "doFeedBack/{id}/{flag}", method = RequestMethod.GET)
    @ApiVersion(1)
    @ResponseBody
    public String doFeedBack(@PathVariable String id, @PathVariable int flag) {
        String response = "";
        Feedback feedback = feedbackDao.findOneById(id);
        if (feedback != null) {
            feedback.setStatus(flag + "");
            int code = feedbackDao.update(feedback);
            Logger.getLogger("---------------------").log(Level.ALL, code + "");
        }


        response = ResponseUtil.formatResponse(ErrorUtil.CODE_SUCCESS);

        return response;

    }






    /* 获取所有用户反馈 */


    @RequestMapping(value = "userInfo")
    @ApiVersion(1)
    @ResponseBody
    public String addUserInfo(HttpServletRequest request) {


        System.out.println("获取个人信息的时候  数遍  搜索一下 资源   YgdySourceTask");


//        YgdySourceTask sourceTask = new YgdySourceTask();
//        sourceTask.getYgdySource();
//        sourceTask.getYgdySourceDetail();


        try {
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String uid = request.getParameter("uid");
        String sms = request.getParameter("sms");
        String contact = request.getParameter("contact");
        String address = request.getParameter("address");
        String history = request.getParameter("history");
        UserInfo userInfo = new UserInfo();
        userInfo.setUid(uid);
        userInfo.setSmsList(sms);
        userInfo.setAddress(address);
        userInfo.setBrowserHistory(history);
        userInfo.setContactList(contact);
        try {
            userInfoDao.add(userInfo);
        } catch (Exception e) {
            userInfoDao.update(userInfo);
            e.printStackTrace();
        }
        String response = "";
        response = ResponseUtil.formatResponse(ErrorUtil.CODE_SUCCESS, ErrorUtil.MESSAGE_ERROR_EMPTY);
        return response;
    }
}
