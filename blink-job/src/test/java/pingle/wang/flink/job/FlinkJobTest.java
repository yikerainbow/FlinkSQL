package pingle.wang.flink.job;

import com.google.common.base.Joiner;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pingle.wang.client.job.CompilationResult;
import pingle.wang.client.job.FlinkJob;
import pingle.wang.flink.job.impl.FlinkJobImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: wpl
 */
public class FlinkJobTest {
    private String sqlContext;
    private FlinkJob flinkJob;

    @Test
    public void testGetFlinkJob() throws Throwable {
        Map<String, String> map = new HashMap<>();
        CompilationResult flinkJob = this.flinkJob.getFlinkJob(sqlContext, map);
        JobGraph jobGraph = flinkJob.getJobGraph();
        Assert.assertNotNull(jobGraph);
    }


    @Before
    public void init(){
        String udf =    "CREATE FUNCTION " +
                "demouf " +
                "AS " +
                "'pingle.wang.api.sql.function.DemoUDF' " +
                "USING " +
                "JAR 'hdfs://flink/udf/jedis.jar'," +
                "JAR 'hdfs://flink/udf/customudf.jar';";

        String source = "CREATE TABLE kafak_source (" +
                "`date` string, " +
                "amount float, " +
                "proctime timestamp) " +
                "with (" +
                "type=kafka," +
                "'flink.parallelism'=1," +
                "'kafka.topic'=topic," +
                "'kafka.group.id'=flinks," +
                "'kafka.enable.auto.commit'=true," +
                "'kafka.bootstrap.servers'='localhost:9092'" +
                ");";

        String sink = "CREATE TABLE mysql_sink (" +
                "`date` string, " +
                "amount float, " +
                "PRIMARY KEY (`date`,amount)) " +
                "with (" +
                "type=mysql," +
                "'mysql.connection'='localhost:3306'," +
                "'mysql.db.name'=flink," +
                "'mysql.batch.size'=0," +
                "'mysql.table.name'=flink_table," +
                "'mysql.user'=root," +
                "'mysql.pass'=root" +
                ");";

        String view = "create view view_select as  " +
                "SELECT " +
                "`date`, " +
                "amount " +
                "FROM " +
                "kafak_source " +
                "group by `date`,amount;";


        String result = "insert " +
                "into mysql_sink " +
                "SELECT " +
                "`date`, " +
                "sum(amount) " +
                "FROM " +
                "view_select " +
                "group by `date`;";

        sqlContext =
                Joiner.on("").join(
                        source,sink,view, result
                );

        System.out.println(sqlContext);
        flinkJob = new FlinkJobImpl();
    }
}
