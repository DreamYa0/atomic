# 项⽬背景

atomic的诞⽣主要⽤于解决，写测试代码⼯作量⼤且效率低下、依赖隔离难、测试⻔槛⾼、⽆法实现⾃动化、测试结果⽆法统⼀展示等因素。从根本上提升测试质量和测试效率

# 项⽬名称由来

atomic 中⽂名：原⼦，寓意⽤来解决测试的最⼩单元，⽤于攻克单测⼀系列痛点问题（哎~ 其实是想了半天没有想到⽐较⾼⼤上的名字，刚好看到atomic这个单词，所有就⽤了这个名字）

# 单元测试、集成测试使⽤说明

## 准备工作

开始使用单测框架之前我们需要在项目的pom文件中添加两个maven依赖，一个是测试框架本身的maven依赖，一个是测试用例生成的maven插件

### 单测框架坐标

```xml
	<dependency>
      <groupId>com.atomic</groupId>
      <artifactId>atomic</artifactId>
      <version>1.0.0-SNAPSHOT</version>
  </dependency>
```

### 测试用例生成插件坐标

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.atomic.plugin</groupId>
            <artifactId>unittest-generator</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <configuration>
                <!-- springboot 项目需要指定Application类 -->
                <!--<applicationName>com.demo.test.TestApplication</applicationName>-->
                <!-- 待测试的物理地址 -->
                <!--<classDirectory>/Users/dreamyao/Documents/forum/src/main/java/com/zbj/forum/web/controller</classDirectory>-->
                <classPath>
                    <!-- 具体某个被测试类的具体文件地址 -->
                    <param>/Users/dreamyao/Documents/forum/src/main/java/com/zbj/forum/service/impl/UserServiceImpl.java</param>
                </classPath>
                <!-- 生成测试类和excel的物理地址，到src/test为止 -->
                <testDirectory>${user.dir}/src/test</testDirectory>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 注意事项

使⽤插件⽣成测试⽤例时需要先调整IDEA⼀项设置

![image-20211019154551021](http://dreamyao.oss-cn-chengdu.aliyuncs.com/2021-10-19-074557.png)

⽣成测试⽤例的插件需要解析项⽬源码，⽆法解析到 **import \*** 具体导⼊了什么，如果在⽣成时插件提示有 **import \*** 时请先按照上图的⽅式设置**IDEA** 然后在删除 **import \*** 后从新导⼊

### maven插件中的字段说明

- applicationName 当前需要测试的model中的SpringBoot启动类，如果为spring项目此行可以注释掉，生成好测试代码后手动在测试代码上添加 @ContextConfiguration(locations = {"/application.xml"}) /application.xml需替换为各种项目对应的spring启动文件
- classDirectory 被测类的物理地址，只能到包路径不能到具体哪个类
- classPath 具体某个被测试类的文件地址
- testDirectory 需要生成测试代码到哪个物理地址，默认地址为 ${user.dir}/src/test 不需要改动

**==注意==**：classDirectory 和 testDirectory  只能选择一个

示例：

#### 运行Maven插件生成测试用例

<img src="http://dreamyao.oss-cn-chengdu.aliyuncs.com/2021-10-19-062808.png" alt="image-20211019142808205" style="zoom:50%;" />

双击maven插件运行，就可以生成对应测试用例了

#### 被测试类内容示例：

```java
@Service
public class UserServiceImpl extends Observable implements IUserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户注册
     *
     * @param user
     */
    @Override
    public void save(User user) {
        User queryUser = null;
        try {
            queryUser = userMapper.getUserByUserName(user.getUserName());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        if (queryUser != null) {
            throw new CRUDException(ExceptionCode.DATA_CONVERT_ERROR, "此用户名已被注册!");
        }
        user.setLastVisit(new Date());
        user.setLastIp(UserContext.getRequest().getRemoteAddr());
        userMapper.register(user);
    }

    /**
     * 用户删除
     *
     * @param id
     */
    @Override
    public void delete(Integer id) {
        User user = this.get(id);
        if (user != null) {
            userMapper.delete(id);
        } else {
            throw new CRUDException(ExceptionCode.HAVE_NOT_DATA, "删除的用户不存在");
        }
    }

    /**
     * 根据用户ID获取用户信息
     *
     * @param id t_user表主键Id
     * @return
     */
    @Override
    public User get(Integer id) throws NullPointerException {
        User user = userMapper.get(id);
        return user;
    }

    @Override
    public PageList findPage(BaseQuery baseQuery) {
        return null;
    }

    @Override
    public List<User> getAll() {
        return null;
    }

    /**
     * 更新用户信息(包括密码修好、用户锁定、用户解锁)
     *
     * @param user
     */
    @Override
    public void update(User user) {
        String userName = user.getUserName();
        User queryUser = userMapper.getUserByUserName(userName);
        if (queryUser == null) {
            throw new CRUDException(ExceptionCode.HAVE_NOT_DATA, "更新的用户不存在");
        }
        userMapper.update(user);
    }

    /**
     * 根据用户名获取用户信息
     *
     * @param userName
     * @return
     */
    @Override
    public User getUserByUserName(String userName) {
        User user = userMapper.getUserByUserName(userName);
        if (user == null) {
            throw new CRUDException(ExceptionCode.HAVE_NOT_DATA, "用户名不存在");
        }
        return user;
    }

    /**
     * 查询所有用户信息
     *
     * @return
     */
    @Override
    public List<User> getAllUsers() {
        List<User> userList = userMapper.getAllUsers();
        if (userList != null || userList.size() > 0) {
            return userList;
        } else {
            throw new CRUDException(ExceptionCode.HAVE_NOT_DATA, "数据库中没有用户!");
        }
    }

    /**
     * 用户登录
     *
     * @param user
     */
    @Override
    public User login(User user) {
        User u = userMapper.login(user);
        if (u != null) {
            if (u.getLocked() == 1) {
                throw new CRUDException(ExceptionCode.USER_IS_LOCKED, "用户已被锁定！");
            }
            // 每登录一次增加5点积分
            u.setCredit(u.getCredit() + 5);
            // 更新访问时间
            u.setLastVisit(new Date());
            // 更新最后访问的IP地址
            u.setLastIp(UserContext.getRequest().getRemoteAddr());
            userMapper.updateCredit(u);
            // 返回增加积分后的登录信息
            User queryUser = userMapper.getUserByUserName(user.getUserName());

            //通知观察者,把日志写入数据库中
            setChanged();
            notifyObservers(queryUser);
        }
        return u;
    }

    /**
     * 用户更新密码
     *
     * @param user
     */
    @Override
    public void updatePassword(User user) {
        String userName = user.getUserName();
        String password = user.getPassword();
        User queryUser = userMapper.getUserByUserName(userName);
        if (queryUser == null) {
            throw new CRUDException(ExceptionCode.HAVE_NOT_DATA, "用户更新密码失败！");
        }
        if (!UserContext.getLoginUser().getUserName().equals(user.getUserName()) &&
                UserContext.getLoginUser().getUserType() != 2) {
            throw new CRUDException(ExceptionCode.HANDLE_NOT_ALLOWE, "操作不被允许！");
        }
        userMapper.updatePassword(userName, password);
    }

}
```



#### 生成的测试文件结果示例：

<img src="http://dreamyao.oss-cn-chengdu.aliyuncs.com/2021-10-19-064026.png" alt="image-20211019144026175" style="zoom: 67%;" />



被测试类的类名会作为测试类和excel测试数据存放的包名，被测试类中的没个方法会对应生成一个测试类及excel文件

#### 生成好的测试类示例：

```java
public class TestUpdate extends BaseTestCase<UserServiceImpl> {

    @Override
    public void beforeTest(Map<String, Object> context) {

    }

    /**
    * moke录制标签,加入了之后,会自动录制mybatis执行情况,之后可以改为replay模式会重放
    * @Mode(TestMethodMode.REC)
    * @AutoAssert( checkMode = CheckMode.REC)注解实现智能化断言录制
    * @AutoAssert( checkMode = CheckMode.REPLAY)注解实现智能化断言回放
    * @AutoTest( autoTestMode = AutoTestMode.XXXXX)注解实现自动化测试
    * @Test( dataProvider = Data.SINGLE(测试用例串行执行),Data.PARALLEL(测试用例并行执行))
    */
    @Test(dataProvider = Data.SINGLE,enabled = false)
    public void testCase(Map<String, Object> context, Object result) {

    }
}
```

#### 生成好的excel文件内容示例：

| caseName     | XXX测试（用例标题） |
| ------------ | ------------------- |
| assertResult | Y                   |
| autotest     | N                   |
| id           |                     |
| userName     |                     |
| password     |                     |
| userType     |                     |
| locked       |                     |
| credit       |                     |
| lastVisit    |                     |
| lastIp       |                     |

被测试类UserServiceImpl update方法的入参是User对象，User对象内容如下所示：

```java
public class User implements Serializable {
    private static final long serialVersionUID = -1118105041735759174L;
    /**
     * 用户ID
     */
    private Integer id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 1:普通用户 2：管理员
     */
    private Integer userType;

    /**
     * 0:未锁定 1锁定
     */
    private Integer locked;

    /**
     * 积分
     */
    private Integer credit;

    /**
     * 最后访问时间
     */
    private Date lastVisit;

    /**
     * 最后访问IP
     */
    private String lastIp;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public Integer getLocked() {
        return locked;
    }

    public void setLocked(Integer locked) {
        this.locked = locked;
    }

    public Integer getCredit() {
        return credit;
    }

    public void setCredit(Integer credit) {
        this.credit = credit;
    }

    public Date getLastVisit() {
        return lastVisit;
    }

    public void setLastVisit(Date lastVisit) {
        this.lastVisit = lastVisit;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

}
```

和生产的excel文件中的入参字段是刚好对应的上的

##### 必填字段说明

- caseName 用例名称：用例编写人员可以根据用例设计的用途来进行命名
- assertResult 当此字段的值为 Y 时，说明当前列的用例为正常流程测试用例需要被测方法正确返回的 如：success字段返回true，当此字段值为 N 时说明当前列为异常流程用例 如success字段返回false
- autotest 是否启用单测框架自动化用例生成功能，对基本类型的入参字段的值进行自动生成，然后经排列组合产生用例，通常此方式推进用于 assertResult 为 N 的情况，也就是异常流程的测试

### 非必填字段说明

- testOnly 对应某列用例此字段为 Y 时 此列用例会被执行，其他用例不会执行，通常用于调试时，只需要执行某条用例而不需要运行所以用例时
- threadCount 用于多线程测试，如果值为10，则表明启用10个线程并发执行当前例用例，通常用于测试代码是否是线程安全的

### Excel中对应值，可选填写关键字

- foreach 对某个字段进行循环赋值，格式 foreach0:100 例如入参于分页参数时，当前页参数可以使用此方式
- 把sql语句作为某字段的值填入Excel 如：格式：sql:dataBaseName:select XXX  dataBaseName对应数据库名称

示例：

#### Excel中其他关键字用法

**${card} -→** **生成身份证号**

**${phone} -→** **生成手机证号**

**${email} -→** **生成邮箱号码**

**${now()} -→** **生成当前时间**

**${now()-1D} -→** **生成当前时间前一天的时间**

**${now()-2D} -→** **生成当前时间前两天的时间**

**${now()-3D} -→** **生成当前时间前三天的时间**

**${now()-1M} -→** **生成当前时间前一月的时间**

**${now()-2M} -→** **生成当前时间前二月的时间**

**${now()-3M} -→** **生成当前时间前三月的时间**

#### 使用示例：

| **caseName** |                       |
| ------------ | --------------------- |
| idCard       | **${card}**           |
| phone        | **${phone}**          |
| email        | **${email}**          |
| taskId       | **random:int:5:1000** |
| day          | **${now()}**          |
| beforeDay    | **${now()-1D}**       |
| beforeMonth  | **${now()-1M}**       |

#### 生成结果：

```json
{
    "phone": "13400323004",
    "idCard": "610404194207112923",
    "beforeDay": "2017-10-11 16:00:37",
    "beforeMonth": "2017-09-12 16:00:37",
    "day": "2017-10-12 16:00:37",
    "email": "fm01gjl7m7@gmail.com",
    "taskId": 297
}
```

#### 产生随机数据

1、产生int数据：对应需要产生int数据字段的值填写 random:int:1:10 其中1:10表示产生数据的长度范围

2、产生String数据：对应需要产生int数据字段的值填写 random:String:1:10 其中1:10表示产生数据的长度范围

3、产生long数据：对应需要产生int数据字段的值填写 random:long:1:10 其中1:10表示产生数据的长度范围

4、产生float数据：对应需要产生int数据字段的值填写 random:float:1:10 其中1:10表示产生数据的长度范围

5、产生double数据：对应需要产生int数据字段的值填写 random:double:1:10 其中1:10表示产生数据的长度范围

## ⽤例**Java**类设计示例

```java
@Transactional // 添加测试用例执行完成后测试数据回滚功能
@ContextConfiguration(locations = {"/applicationContext.xml"}) // 当前项目spring的启动xml文件
public class TestUpdate extends BaseTestCase<UserServiceImpl> {

    @Override
    public void beforeTest(Map<String, Object> context) {

        // Map<String, Object> context context中的数据为excel中解析出来的数据，且还未执行测试调用，key对应的是excel第一列标题列
        // value 对应的是excel填写的值

        // excel中第二列数据的 CASE_INDEX 编号为 1 第三列数据的 CASE_INDEX 编号为 2 第一列数据最为后续所有列数据的标题列
        if ("1".equals(context.get(Constants.CASE_INDEX).toString())) {
            // 当为excel中的第⼀列⼊参数据时
            // 对 HttpServletRequest 的 getAttribute ⽅法进⾏mock
            MockUtils.mockProxy(request, "getAttribute", "pkucestestkey",
                                "accessKey");
        } else if ("2".equals(context.get(Constants.CASE_INDEX).toString())) {
            // 当为excel中的第⼆列⼊参数据时
        }

        // 在beforeTest方法中对 context的内容可以进行任何操作，如删除某个值，增加某个值，修改某个值
        // 如：可以从数据库、文件、其他接口调用返回的结果中获取值，并放入到context中给当前这个接口作为入参使用

    }

    /**
    * moke录制标签,加入了之后,会自动录制mybatis执行情况,之后可以改为replay模式会重放
    * @Mode(TestMethodMode.REC)
    * @AutoAssert( checkMode = CheckMode.REC)注解实现智能化断言录制
    * @AutoAssert( checkMode = CheckMode.REPLAY)注解实现智能化断言回放
    * @AutoTest( autoTestMode = AutoTestMode.XXXXX)注解实现自动化测试
    * @Test( dataProvider = Data.SINGLE(测试用例串行执行),Data.PARALLEL(测试用例并行执行))
    */
    @Test(dataProvider = Data.SINGLE,enabled = true)
    public void testCase(Map<String, Object> context, Object result) {

        // Map<String, Object> context 这里的context和 beforeTest方法中的context是有些区别的，除了包含beforeTest方法中context的所有内容之外
        // 框架在执行过程中产生的很多中间数据会被放入 testCase 方法的context中
        
        // Object result 为 我们调用接口的返回结果对象 当前这个测试类，测试的是 UserServiceImpl 的update方法，这个方法无返回值
        // 这里对应的 result就没有返回值

        if ("1".equals(context.get(Constants.CASE_INDEX).toString())) {
            // 执行第一个条测试用户的返回结果断言

        } else if ("2".equals(context.get(Constants.CASE_INDEX).toString())) {
            // 执行第二个条测试用户的返回结果断言
        }
    }
}
```

### 数据初始化使⽤说明

当在执⾏测试之前想对数据库进⾏测试前的测试数据准备时，可以在测试类中注⼊对应的mapper，然后在beforeTest⽅法中使⽤对应的mapper为数据库初始化对应的测试数据。

### 断⾔说明

断⾔⽅式可以有两种选择⽅式，⼀种是在Java类中的testCase⽅法中写对应的断⾔代码，如上图所示，另⼀种是在对应的测试⽤例的excel中填写断⾔，excel中有⼀个名称为exceptResult的sheet

简单的断言可以再excel中进行断言，本人更推进在测试类的testCase方法中进行详细的断言编写

#### 返回结果对象Json字符串如下

```json
{
    "code":"1000",
    "data":{
        "result":[
            {
                "symbol":"BTC",
                "quantity":"xxxx",
                "price":"1000"
            }
        ],
        "timestamp":0
    },
    "msg":"success"
}
```

#### excel中的断⾔填写示例如下

<img src="http://dreamyao.oss-cn-chengdu.aliyuncs.com/2021-10-19-073321.png" alt="image-20211019153320896" style="zoom: 50%;" />

#### 数据库断⾔说明

数据库断⾔和数据库初始化使⽤⽅式类似，在对应的测试类中注⼊对应的mapper，然后在testCase⽅法中调⽤对应的mapper进⾏数据库的操作然后进⾏数据库断⾔

### 测试类中的**beforeTest(Map<String,Object> context)**使⽤说明

此⽅法中的context参数中的内容就excel中每⼀列的内容，在真正调⽤被测试⽅法之前会先执⾏beforeTest⽅法，故可以在beforeTest⽅法中任意对context中的数据进⾏处理，⽐如添加、删除、或者执⾏数据库查询后把查询的结果放⼊context中，等等操作

ps：只有想不到没有做不到

## Mock工具

### **SpringBean**、普通类**Mock**

```java
@Transactional // 添加测试用例执行完成后测试数据回滚功能
@ContextConfiguration(locations = {"/applicationContext.xml"}) // 当前项目spring的启动xml文件
public class TestDelete extends BaseTestCase<UserServiceImpl> {

    @Autowired
    private ITopicService topicService;

    @Override
    public void beforeTest(Map<String, Object> context) {

        // mock Bean
        MockUtils.mock(topicService, "findOneTopicById", "mock返回值",
                       "mock⼊参");

    }

    /**
    * moke录制标签,加入了之后,会自动录制mybatis执行情况,之后可以改为replay模式会重放
    * @Mode(TestMethodMode.REC)
    * @AutoAssert( checkMode = CheckMode.REC)注解实现智能化断言录制
    * @AutoAssert( checkMode = CheckMode.REPLAY)注解实现智能化断言回放
    * @AutoTest( autoTestMode = AutoTestMode.XXXXX)注解实现自动化测试
    * @Test( dataProvider = Data.SINGLE(测试用例串行执行),Data.PARALLEL(测试用例并行执行))
    */
    @Test(dataProvider = Data.SINGLE,enabled = true)
    public void testCase(Map<String, Object> context, Object result) {

    }
}
```

### 动态代理类、接⼝**Mock**，如**mybatis**的**mapper**

```java
@Transactional // 添加测试用例执行完成后测试数据回滚功能
@ContextConfiguration(locations = {"/applicationContext.xml"}) // 当前项目spring的启动xml文件
public class TestDelete extends BaseTestCase<UserServiceImpl> {

    @Capturing
    private HttpSession session;
    @Autowired
    private UserMapper userMapper;

    @Override
    public void beforeTest(Map<String, Object> context) {

        // 动态代理类、接⼝Mock，如mybatis的mapper
        MockUtils.mock(session, "getAttribute", 1602578701956947969L,
                       "userNo");

        MockUtils.mock(userMapper, "getUserByUserName", "mock返回值",
                       "mock⼊参");

    }

    /**
    * moke录制标签,加入了之后,会自动录制mybatis执行情况,之后可以改为replay模式会重放
    * @Mode(TestMethodMode.REC)
    * @AutoAssert( checkMode = CheckMode.REC)注解实现智能化断言录制
    * @AutoAssert( checkMode = CheckMode.REPLAY)注解实现智能化断言回放
    * @AutoTest( autoTestMode = AutoTestMode.XXXXX)注解实现自动化测试
    * @Test( dataProvider = Data.SINGLE(测试用例串行执行),Data.PARALLEL(测试用例并行执行))
    */
    @Test(dataProvider = Data.SINGLE,enabled = true)
    public void testCase(Map<String, Object> context, Object result) {

    }
}
```

### ⼯具类、⽆法实列化的类、静态⽅法**Mock**

```java
@Transactional // 添加测试用例执行完成后测试数据回滚功能
@ContextConfiguration(locations = {"/applicationContext.xml"}) // 当前项目spring的启动xml文件
public class TestDelete extends BaseTestCase<UserServiceImpl> {

    @Override
    public void beforeTest(Map<String, Object> context) {

        // mock⼯具类
        MockUtils.mock(CheckDataUtil.class, "updateUserCheck", "mock返回值", 
                       "mock⼊ 参");

    }

    /**
    * moke录制标签,加入了之后,会自动录制mybatis执行情况,之后可以改为replay模式会重放
    * @Mode(TestMethodMode.REC)
    * @AutoAssert( checkMode = CheckMode.REC)注解实现智能化断言录制
    * @AutoAssert( checkMode = CheckMode.REPLAY)注解实现智能化断言回放
    * @AutoTest( autoTestMode = AutoTestMode.XXXXX)注解实现自动化测试
    * @Test( dataProvider = Data.SINGLE(测试用例串行执行),Data.PARALLEL(测试用例并行执行))
    */
    @Test(dataProvider = Data.SINGLE,enabled = false)
    public void testCase(Map<String, Object> context, Object result) {

    }
}
```

## **Mybatis**调⽤执⾏录制与回放功能

要实现mybatis调⽤的录制与回放功能⾸先需要配置mybatis插件，配置⽅式如下：

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <!-- other setting : -->
    <settings>
        <!-- 给予被嵌套的resultMap以字段-属性的映射⽀持 -->
        <setting name="autoMappingBehavior" value="FULL" />
        <!-- 数据库超过30秒仍未响应则超时 -->
        <setting name="defaultStatementTimeout" value="10" />
    </settings> <plugins>
    <!-- mybatis mapper调⽤录制插件 -->
    <plugin interceptor="com.atomic.tools.mock.mybatis.UnitTestFilter4Mybatis"></plugin>
</plugins> 
    <mappers></mappers>
</configuration>
```

然后需要在测试类的testCase⽅法上加上 @Mode(TestMethodMode.REC)

添加上录制注解后执⾏⼀下测试⽤例，执⾏完后就会把mybatis调⽤结果录制下来，然后在把注解值改为：@Mode(TestMethodMode.REPLAY) 下次在执⾏测试⽤例时就不会真正去调⽤数据库了。

# dubbo接⼝测试使⽤说明
TODO
