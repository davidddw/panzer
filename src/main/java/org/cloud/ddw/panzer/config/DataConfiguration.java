package org.cloud.ddw.panzer.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.github.pagehelper.PageHelper;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by d05660ddw on 2016/11/2.
 */
@Configuration
@MapperScan(basePackages = {"org.cloud.ddw.panzer.mapper"})
@PropertySource("file:/etc/panzer/jdbc.properties")
public class DataConfiguration {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    Environment environment;

    @Bean(name = "datasource")
    public DruidDataSource dataSource() throws SQLException {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(environment.getRequiredProperty("jdbc.driver"));
        dataSource.setUrl(environment.getRequiredProperty("jdbc.url"));
        dataSource.setUsername(environment.getRequiredProperty("jdbc.username"));
        dataSource.setPassword(environment.getRequiredProperty("jdbc.password"));
        //初始化连接大小
        dataSource.setInitialSize(environment.getRequiredProperty("jdbc.pool.initialSize", Integer.class));
        //连接池最大数量
        dataSource.setMaxActive(environment.getRequiredProperty("jdbc.pool.maxActive", Integer.class));
        //连接池最小空闲
        dataSource.setMinIdle(environment.getRequiredProperty("jdbc.pool.minIdle", Integer.class));
        //连接时最大等待时间，单位毫秒。配置了maxWait之后， 缺省启用公平锁，并发效率会有所下降，
        //如果需要可以通过配置useUnfairLock属性为true使用非公平锁
        dataSource.setMaxWaitThreadCount(environment.getRequiredProperty("jdbc.pool.maxWait", Integer.class));
        //是否缓存preparedStatement，也就是PSCache。
        //在mysql5.5以下的版本中没有PSCache功能，建议关闭掉。 5.5及以上版本有PSCache，建议开启。
        dataSource.setPoolPreparedStatements(false);
        //要启用PSCache，必须配置大于0，当大于0时， poolPreparedStatements自动触发修改为true。
        //在Druid中，不会存在Oracle下PSCache占用内存过多的问题，可以把这个数值配置大一些，比如说100
        dataSource.setMaxOpenPreparedStatements(-1);
        //timeBetweenEvictionRunsMillis间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        //minEvictableIdleTimeMillis一个连接在池中最小空闲的时间，单位是毫秒
        dataSource.setMinEvictableIdleTimeMillis(300000);
        //用来检测连接是否有效的sql，要求是一个查询语句。
        dataSource.setValidationQuery("SELECT 'x'");
        //配置为true，不影响性能，并且保证安全性。
        dataSource.setTestWhileIdle(true);
        //申请连接时执行validationQuery检测连接是否有效， 做了这个z配置会降低性能。
        dataSource.setTestOnBorrow(false);
        //归还连接时执行validationQuery检测连接是否有效， 做了这个配置会降低性能
        dataSource.setTestOnReturn(false);
        //监控统计用的filter:stat 日志用的filter:log4j 防御sql注入的filter:wall
        dataSource.setFilters("stat");
        return dataSource;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        //final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setConfigLocation(new ClassPathResource("/mybatis-config.xml"));
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionFactory.setMapperLocations(resolver.getResources("mappers/*.xml"));
        sessionFactory.setTypeAliasesPackage("org.cloud.ddw.panzer.entity");
        sessionFactory.setTransactionFactory(springManagedTransactionFactory());
        sessionFactory.setPlugins(new Interceptor[]{pageHelper()});
        return sessionFactory.getObject();
    }

    @Bean
    public PageHelper pageHelper() {
        PageHelper myPageHelper = new PageHelper();
        Properties properties = new Properties();
        properties.put("dialect", "mysql");
        //设置为true时，会将RowBounds第一个参数offset当成pageNum页码使用
        properties.put("offsetAsPageNum", true);
        //设置为true时，使用RowBounds分页会进行count查询
        properties.put("rowBoundsWithCount", true);
        //设置为true时，如果pageSize=0或者RowBounds.limit = 0就会查询出全部的结果
        properties.put("pageSizeZero", true);
        properties.put("params", "pageNum=start;pageSize=limit;");
        properties.put("supportMethodsArguments", true);
        //支持通过Mapper接口参数来传递分页参数
        properties.put("reasonable", false);
        //always总是返回PageInfo类型,check检查返回类型是否为PageInfo,none返回Page
        properties.put("returnPageInfo", "check");
        myPageHelper.setProperties(properties);
        return myPageHelper;
    }

    @Bean(name = "transactionManager")
    public DataSourceTransactionManager dataSourceTransactionManager() throws PropertyVetoException, SQLException {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public SpringManagedTransactionFactory springManagedTransactionFactory() {
        return new SpringManagedTransactionFactory();
    }

}
