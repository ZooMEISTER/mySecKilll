package cn.tycoding.mapper;

import cn.tycoding.entity.Seckill;
import cn.tycoding.entity.UserBean;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

@Mapper
public interface SeckillMapper {

    /**
     * 查询所有秒杀商品的记录信息
     *
     * @return
     */
    List<Seckill> findAll();

    /**
     * 根据主键查询当前秒杀商品的数据
     *
     * @param id
     * @return
     */
    Seckill findById(long id);

    //寻找用户
    List<UserBean> findAllUsers();

    //注册时插入用户
    int insertUser(@Param("regPhone") String regPhone, @Param("regPassword") String regPassword, @Param("regIdentity") String regIdentity, @Param("regAge") int regAge, @Param("regIdCardNo") String regIdCardNo, @Param("regWorkCondition") String regWorkCondition);

    //管理员删除活动
    int delSeckillEvent(@Param("delEventNo") Long delEventNo);

    int addNewSeckillEvent(@Param("newEventNo") int newEventNo, @Param("eventName") String eventName, @Param("eventOriPrice") int eventOriPrice, @Param("eventDiscountPrice") int eventDiscountPrice, @Param("eventStockCount") int eventStockCount, @Param("newStartTime_TS") Timestamp eventStartTime, @Param("newEndTime_TS") Timestamp eventEndTime);

    /**
     * 减库存。
     * 对于Mapper映射接口方法中存在多个参数的要加@Param()注解标识字段名称，不然Mybatis不能识别出来哪个字段相互对应
     *
     * @param seckillId 秒杀商品ID
     * @param killTime  秒杀时间
     * @return 返回此SQL更新的记录数，如果>=1表示更新成功
     */
    int reduceStock(@Param("seckillId") long seckillId, @Param("killTime") Date killTime);
}
