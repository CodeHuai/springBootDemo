package com.huai;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huai.domain.OrderVO;
import com.huai.entity.User;
import com.huai.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest
public class UserTest {
    @Autowired
    private UserMapper userMapper;

    @Test
    public void insertBatch() {
        ArrayList<User> userArrayList = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            User user = new User();
            user.setName("小小" + i);
            user.setAge(i);
            user.setGender(i % 2);

            userArrayList.add(user);
        }

        userMapper.insert(userArrayList);
    }

    // 分页查询（不带参数）
    @Test
    public void fetchUserPage() {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();

        Page<User> userPage = new Page<>(1, 10);

        userPage = userMapper.selectPage(userPage, userQueryWrapper);

        System.out.println(userPage);
    }

    // 携带条件进行分页查询
    @Test
    public void fetUserByParams() {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();

        userQueryWrapper.gt("age", 20).lt("age", 40);

        Page<User> userPage = new Page<>(1, 50);

        userPage = userMapper.selectPage(userPage, userQueryWrapper);

        log.debug("【userPage】：{}", userPage.getRecords());
    }

    @Test
    public void selectOrdersVo() {
        List<OrderVO> uorderVoList = userMapper.selectOrdersVo();
        log.debug("uorderVoList: {}", uorderVoList);
    }
}
