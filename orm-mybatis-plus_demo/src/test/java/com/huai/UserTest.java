package com.huai;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huai.entity.User;
import com.huai.service.UserService;
import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

// @Slf4j：Lombok 生成 private static final Logger log = LoggerFactory.getLogger(UserTest.class);
// @SpringBootTest：启动完整 Spring 容器（全量装配 classpath 上所有 starter），这样 @Autowired 才有容器可注入
@Slf4j
@SpringBootTest
public class UserTest {

    // 注入的是接口 UserService，容器里匹配到的实际对象是打了 @Service 的 UserServiceImpl
    // （它 extends ServiceImpl<Mapper, 实体>，IService 全家桶方法 save/saveBatch/list/count/page 都从这条继承链来）
    @Autowired
    private UserService userService;

    // 单条插入
    @Test
    public void insertTest() {
        // Hutool 工具：生成 32 位无横杠随机串，当密码加密的盐
        String salt = IdUtil.fastSimpleUUID();
        // User.builder()：@Builder 生成的链式构建，一行设完所有字段
        // 密码存的是 md5(明文+盐)，登录时取盐重算比对——库里永远不存明文
        User testSave3 = User.builder().name("testSave3").password(SecureUtil.md5("123456" + salt)).salt(salt).email("testSave3@xkcoding.com").phoneNumber("17300000003").status(1).lastLoginTime(new DateTime()).build();
        // IService 内置插入，等价 SQL：INSERT INTO orm_user (name, password, ...) VALUES (...)
        // create_time / last_update_time 不用手动设——实体上的 @TableField(fill=...) + CommonFieldHandler 自动填充
        boolean isSaved = userService.save(testSave3);
        // 断言：save 返回 false 不抛异常，没这行的话插入失败测试照样绿灯（没断言的测试只是执行，不是验证）
        Assert.assertTrue(isSaved);
        // DEBUG 级日志打印主键回显：插入前 id 是 null，MP 会把数据库自增主键塞回实体
        // 注意默认日志级别是 INFO，要在 yml 配 logging.level.com.huai=debug 才能看到这行输出
        log.debug("【测试id回显#testSave3.getId()】= {}", testSave3.getId());
    }

    // 批量插入
    @Test
    public void testSaveBatch() {
        ArrayList<User> users = new ArrayList<>();

        // 循环攒 6 个用户对象（id 都不设，等数据库自增分配）
        for (int i = 4; i < 10; i++) {
            String salt = IdUtil.fastSimpleUUID();
            String nameStr = "testSave" + i;
            User user = User.builder().name(nameStr).password(SecureUtil.md5("123456" + salt)).salt(salt).email(nameStr + "@xkcoding.com").phoneNumber("1730000000" + i).status(1).lastLoginTime(new DateTime()).build();
            users.add(user);
        }
        // IService 批量插入：一条链把整个 List 插进去（内部按批次提交，默认一批 1000 条），比循环调 save 快得多
        boolean isSaveBatch = userService.saveBatch(users);
        Assert.assertTrue(isSaveBatch);
        // Stream 流水线（Java 8）：stream() 开流 → map(User::getId) 把每个 User 转成它的 id（:: 是方法引用，等价 user -> user.getId()）
        // → forEach 逐个打印，等价 for(User u : users) System.out.println(u.getId());
        // 这里打印的目的：验证批量插入后主键有没有回填到每个对象上
        users.stream().map(User::getId).forEach(System.out::println);
    }

    // 测试MyBatis-Plus 查询全部
    @Test
    public void findAllUser() {
        // list(wrapper)：IService 内置查询。QueryWrapper 是"条件构造器"，专门拼 WHERE；
        // 空的 new QueryWrapper<>() = 不带任何条件 = 查全表，等价 SQL：SELECT * FROM orm_user
        List<User> list = userService.list(new QueryWrapper<>());
        // Hutool 判空工具断言查回非空（列表里至少有前面测试插的数据）
        Assert.assertTrue(CollUtil.isNotEmpty(list));
        // 打印整个列表：{} 会被 list 的 toString() 填充，能读出 User(id=.., name=..) 是因为实体上有 @Data 生成了 toString()
        log.debug("【list】= {}", list);
    }

    // 测试MyBatis-Plus 分页排序查询
    @Test
    public void testFindUserPage() {
        // count(wrapper)：条件计数，返回 long（所以变量要用 long 接）
        // wrapper 这次不是空的：.eq("status", 1).like("name", "test") 链式拼条件，AND 连接
        // 等价 SQL：SELECT COUNT(*) FROM orm_user WHERE status = 1 AND name LIKE '%test%'
        long count = userService.count(new QueryWrapper<User>().eq("status", 1).like("name", "test"));
        // Page 对象是"分页参数单"：构造参数 (current, size) = 第 1 页、每页 5 条
        Page<User> userPage = new Page<>(1, 5);
        // 设置排序：按 id 倒序（ORDER BY id DESC）
        // 注意：setDesc 是老版本 API，3.4+ 推荐写法是 userPage.addOrder(OrderItem.desc("id"))；若这里爆红就换新写法
        userPage.setDesc("id");
        // page(分页参数, 条件)：IService 内置分页查询，幕后实际执行两条 SQL：
        //   ① SELECT COUNT(*) FROM orm_user            → 算总数，填进返回对象的 total
        //   ② SELECT * FROM orm_user ORDER BY id DESC LIMIT 0, 5  → 取当前页数据
        // LIMIT 是分页插件（PaginationInnerInterceptor）拼上去的——没配插件就是假分页，全表查回
        // 返回类型写 IPage（接口）、实际对象是 Page（实现），和 UserService/UserServiceImpl 是同款"接口接、实现给"的关系
        IPage<User> page = userService.page(userPage, new QueryWrapper<User>());

        // getSize() 是"每页容量"（自己设的 5），不是本页实际条数；实际条数看 page.getRecords().size()
        Assert.assertEquals(5, page.getSize());
        // getTotal() 是 MP 幕后那条 COUNT(*) 的结果
        // 小心口径陷阱：上面手数的 count 带了 status/like 条件，这里的 page 查询是空 wrapper（全表），
        // 两个数字含义不同——当前数据恰好全表都满足条件才相等，换个数据集这断言就会假失败
        Assert.assertEquals(count, page.getTotal());
        // getRecords()：当前页的数据行（List<User>），分页结果的"正文"在 这里取
        log.debug("【page.getRecords()】= {}", page.getRecords());
    }
}