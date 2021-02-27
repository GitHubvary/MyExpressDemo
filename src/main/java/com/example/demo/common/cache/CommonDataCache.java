package com.example.demo.common.cache;

import com.example.demo.domain.bean.DataCompany;
import com.example.demo.domain.bean.UserEvaluate;
import com.example.demo.service.DataCompanyService;
import com.example.demo.service.UserEvaluateService;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 * date: 2021/2/24 21:20
 */
@Component
public class CommonDataCache {


    @Autowired
    private DataCompanyService dataCompanyService;

    @Autowired
    private UserEvaluateService userEvaluateService;

    /**
     * 学校数据缓存
     * key: schoolId
     */
    public static LoadingCache<Integer, DataCompany> dataCompanyCache;
    /**
     * 用户评分Score
     * key: 用户ID
     */
    public static LoadingCache<String, String> userScoreCache;

    @PostConstruct
    private void init() {
        dataCompanyCache = Caffeine.newBuilder()
                .maximumSize(35)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build(id -> dataCompanyService.getById(id));

        userScoreCache = Caffeine.newBuilder()
                .maximumSize(35)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build(id -> {
                    UserEvaluate evaluate = userEvaluateService.getById(id);
                    return evaluate.getScore().toPlainString();
                });
    }
}
