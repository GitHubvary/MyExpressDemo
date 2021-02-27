package com.example.demo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.mapper.OrderInfoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Test
    void contextLoads() {
        System.out.println(orderInfoMapper.pageAdminOrderVO(new Page<>(1,10),"",0).getRecords());
        System.out.println(orderInfoMapper.pageAdminOrderVO(new Page<>(1,10),"",0).getTotal());
    }

}
