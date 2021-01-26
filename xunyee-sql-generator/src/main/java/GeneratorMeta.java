import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.converts.PostgreSqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

/**
 * <p>
 * 代码生成器演示
 * </p>
 */
public class GeneratorMeta {

    /**
     * <p>
     * MySQL 生成演示
     * </p>
     */
    public static void main(String[] args) {
        AutoGenerator mpg = new AutoGenerator();
        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = System.getProperty("user.dir");
        gc.setOutputDir(projectPath + "/xunyee-sql-generator/src/main/java");
        gc.setSwagger2(true);
        gc.setOpen(false);
        gc.setFileOverride(true);
        gc.setActiveRecord(true);// 不需要ActiveRecord特性的请改为false
        gc.setEnableCache(false);// XML 二级缓存
        gc.setBaseResultMap(true);// XML ResultMap
        gc.setBaseColumnList(false);// XML columList
        gc.setIdType(IdType.AUTO);
        gc.setAuthor("tobi");
        mpg.setGlobalConfig(gc);

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setDbType(DbType.POSTGRE_SQL);
        dsc.setDriverName("org.postgresql.Driver");
        dsc.setUsername("vlkdj");
        dsc.setPassword("weiling@qinghai118");
        dsc.setSchemaName("meta");
        dsc.setUrl("jdbc:postgresql://115.29.163.237:5432/vlkdj?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8");

        dsc.setTypeConvert(new PostgreSqlTypeConvert(){
            @Override
            public IColumnType processTypeConvert(GlobalConfig config, String fieldType) {

                if (fieldType.toLowerCase().contains("date")){
                    return DbColumnType.LOCAL_DATE;
                }

                if (fieldType.toLowerCase().contains("timestamp")){
                    return DbColumnType.DATE;
                }

                return super.processTypeConvert(config, fieldType);
            }
        });
        mpg.setDataSource(dsc);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setInclude("person","brand","meta_brand_person");
        strategy.setNaming(NamingStrategy.underline_to_camel);// 表名生成策略
        strategy.setColumnNaming(NamingStrategy.no_change);
        strategy.setEntityLombokModel(true);
        mpg.setStrategy(strategy);

        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setParent("com.vlinkage.ant");
        pc.setModuleName("meta");
        pc.setMapper("mapper");
        pc.setXml("sqlxml");
        mpg.setPackageInfo(pc);

        //自定义模板配置，可以 copy 源码 mybatis-plus/src/main/resources/templates 下面内容修改，
        //放置自己项目的 src/main/resources/templates 目录下, 默认名称一下可以不配置，也可以自定义模板名称
        TemplateConfig tc = new TemplateConfig();
        tc.setController(null);
        tc.setService(null);
        tc.setServiceImpl(null);
        //如上任何一个模块如果设置 空 OR Null 将不生成该模块。
        mpg.setTemplate(tc);

        // 执行生成
        mpg.execute();
    }
}
