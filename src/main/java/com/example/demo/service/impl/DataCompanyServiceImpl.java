package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.cache.CommonDataCache;
import com.example.demo.common.constant.RedisKeyConstant;
import com.example.demo.domain.bean.DataCompany;
import com.example.demo.mapper.DataCompanyMapper;
import com.example.demo.service.DataCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 * date: 2021/2/24 21:26
 */
@Service
public class DataCompanyServiceImpl extends ServiceImpl<DataCompanyMapper, DataCompany> implements DataCompanyService, ApplicationListener<ApplicationStartedEvent> {
    @Autowired
    private DataCompanyMapper dataCompanyMapper;
    @Resource
    private RedisTemplate<String, DataCompany> redisTemplate;

    @Override
    public List<DataCompany> listAll() {
        return dataCompanyMapper.selectList(null);
    }

    @Override
    public List<DataCompany> listAllByCache() {
        List<DataCompany> list = redisTemplate.opsForList().range(RedisKeyConstant.DATA_COMPANY, 0, -1);
        if(list == null) {
            list = listAll();
        }
        return list;
    }

    @Override
    public DataCompany getByCache(Integer id) {
        return CommonDataCache.dataCompanyCache.get(id);
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        // 数据加载线程池
        ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("data-company-loader"));
        executorService.scheduleWithFixedDelay(() -> {
            redisTemplate.delete(RedisKeyConstant.DATA_COMPANY);
            redisTemplate.opsForList().rightPushAll(RedisKeyConstant.DATA_COMPANY, listAll());
        }, 0, 10, TimeUnit.MINUTES);
    }
}
