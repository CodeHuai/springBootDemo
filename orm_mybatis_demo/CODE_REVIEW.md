# orm_mybatis_demo 代码审查清单

> 2026-08-22 模块 CRUD 收尾时的全量审查。按严重程度分四档，改完一项勾一项。
> 定位用的行号以审查当天的代码为准，改动后会漂移，按方法名找。

## 一、会炸 / 会错（优先修，都是一两行的事）

- [ ] **1. `UserServiceImpl.java:62` —— `||` 应为 `&&`（NPE 雷管）**
  `ObjectUtil.isNotNull(id) || StrUtil.isNotBlank(id.toString())`：id 为 null 时左边 false，短路继续算右边，`id.toString()` 直接 NPE。且非 null 时左边恒 true，校验形同虚设。

- [ ] **2. `UserServiceImpl.java:79` —— removeUserById 没有 isNull 前置判断**
  `StrUtil.isBlank(id.toString())`：id=null 直接 NPE；非 null 时数字串永不为 blank，条件恒 true。这个校验等于不存在。对照 `getUserById`（26 行）的结构补。

- [ ] **3. `userMapper.xml:46` —— `status` 的 `<if>` 会被 0 值静默跳过（业务 bug）**
  `test="status != null and status != ''"`：Integer 加 `!= ''`，OGNL 把 `''` 转成 0 比较 → 前端传 status=0（禁用用户）时 if 判 false，**禁用操作无声失效**。0 是表注释里定义的合法值。删掉 `and status != ''`，只留 `status != null`。

- [ ] **4. `ApiResponse.java:42` —— error() 返回的 code 是 200**
  无参 `error()` 里写的是 `Status.OK.getCode()`（复制粘贴忘改），导致 `UserController.java:48` 删除失败时返回 `{"code":200,"msg":"服务器出错了"}`，自相矛盾。改用 `Status.UNKNOWN_ERROR.getCode()`。

- [ ] **5. `UserController.java:34` —— insertUser 丢弃了 service 的 Boolean 返回值**
  失败照样返回"操作成功"+user。学 `removeUserById` 的写法：接住返回值，失败走 error 分支。

## 二、脏数据 / 安全隐患

- [ ] **6. `UserServiceImpl.java:50-52` —— password 没传会把 "null" 加密存库**
  `rawPass` 为 null 时，`null + 字符串` 不报错，拼出字面量 `"null::..."` 再 md5 入库，产生永远对不上的脏密码。insert 前应校验必填项（name / password / email / phoneNumber）。

- [ ] **7. `userMapper.xml:34-39` —— update 放行了 password / salt 字段**
  updateUserInfo 链路没有加密逻辑，前端传什么存什么 = 开了个"明文改密码"接口，还能改盐。真实项目改密码必须是独立接口（还要验旧密码）。学习阶段至少删掉这两个 `<if>`，update 只管 name / email / phone / status。

- [ ] **8. 建表 SQL（furns_ssm.sql）—— `last_update_time` 永远不更新**
  `DEFAULT CURRENT_TIMESTAMP` 只在插入时生效，update 不会刷。改为：
  ```sql
  `last_update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0)
  ```
  然后对存量表执行 `ALTER TABLE orm_user MODIFY ...` 同样内容。

## 三、死代码（功能碰巧对，顺手清理）

- [ ] **9.** `UserServiceImpl.java:26` —— `isBlank(id.toString())` 恒 false，数字串永不为 blank，可删
- [ ] **10.** `UserServiceImpl.java:35 / :44` —— `isNotEmpty(user)` 对 POJO 恒 false（ObjectUtil.isEmpty 只认识字符串/集合/Map/数组），`:35` 的 `||` 结果碰巧对，属运气
- [ ] **11.** `Passworld.java:11` —— 枚举的 `key` 字段从未被使用

## 四、规范建议（不急，下个模块或重构时改进）

- [ ] **12.** `UserController.java:45` 用 GET 做删除 —— 违反 HTTP 语义（预加载/爬虫会误删），应 `@DeleteMapping`。全套 URL 是"动词风格"，REST 风格为名词 URL + HTTP 动词：`GET /api/users/5`、`POST /api/users`、`DELETE /api/users/5`
- [ ] **13.** 物理删除 vs 逻辑删除打架 —— 表里 `status=-1` 设计了逻辑删除，removeUserById 却是物理 delete。二选一：改 DDL 或改 SQL 为 `update ... set status = -1`
- [ ] **14.** 查无此人返回 200 + data:null —— 业务失败与成功没区分，等学全局异常处理时一起治
- [ ] **15.** 拼写：`Passworld` → `Password`。另注意 `ApiResponse.success(String msg)` 与 `success(T data)` 是重载陷阱（T 为 String 时永远走 msg 版本）
- [ ] **16.** MD5+盐 是演示级安全，真实项目用 BCrypt（记个名词即可）

## 修完后的自测（故意喂坏数据）

- [ ] id 传 null / 不带 id 调 removeUserById → 应优雅拒绝，不 500
- [ ] update 传 `status=0` → 库里 status 真的变 0
- [ ] insert 不传 password → 应被校验拦下，而不是库里多一个"null 密码"用户
- [ ] 删除一个不存在的 id → code 应为 500 而不是 200
- [ ] update 一条数据后看 `last_update_time` 有没有跟着变
