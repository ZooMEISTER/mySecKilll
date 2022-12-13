package cn.tycoding.controller;

import cn.tycoding.dto.Exposer;
import cn.tycoding.dto.SeckillExecution;
import cn.tycoding.dto.SeckillResult;
import cn.tycoding.entity.Seckill;
import cn.tycoding.entity.UserBean;
import cn.tycoding.enums.SeckillStatEnum;
import cn.tycoding.exception.RepeatKillException;
import cn.tycoding.exception.SeckillCloseException;
import cn.tycoding.exception.SeckillException;
import cn.tycoding.mapper.SeckillMapper;
import cn.tycoding.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.jws.WebParam;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

/**
 * 秒杀商品的控制层
 */
@Controller
@RequestMapping("/seckill")
public class SeckillController{

    private final String key = "seckill";
    private final String key_userlist = "usertable";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private SeckillMapper seckillMapper;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //映射到登录界面
    @RequestMapping("/login")
    public String secKillLogin(){
        return "page/seckill_login";
    }

    //映射到尝试登陆界面
    @RequestMapping("/try_login")
    public String try_SecKillLogin(@RequestParam("username") String phone,
                                   @RequestParam("password") String pwd,
                                   Model model) {
        //用户号码和密码
        System.out.println("phone" + phone + "  pwd" + pwd);

        //遍历数据库，检查是否存在当前用户
        redisTemplate.delete(key_userlist);  //先清空redis缓存
        List<UserBean> userList = seckillService.findAllUsers();
        //System.out.println(userList.get(0).getUsername());

        int user_no = -1;
        for(int i = 0;i < userList.size();++i){
//            System.out.println("user phone: " + userList.get(i).getUsername());
//            System.out.println("     phone: " + phone);
            if(phone.equals(userList.get(i).getUsername())){
                user_no = i;
                break;
            }
        }
        if(user_no == -1){
            //用户不存在
            model.addAttribute("err","usernotexist");
            System.out.println("用户不存在");
        }
        else{
            //用户存在
            if(pwd.equals(userList.get(user_no).getPassword())){
                //密码正确

                System.out.println("密码正确 phone: " + userList.get(user_no).getUsername() + " pwd: " + userList.get(user_no).getPassword() + " " + userList.get(user_no).getUserType());


                redisTemplate.delete(key);  //先清空redis缓存
                List<Seckill> list = seckillService.findAll();
                model.addAttribute("list", list);
                model.addAttribute("userphone", phone);
                model.addAttribute("userpass", pwd);
                //model.addAttribute("useridentity", userList.get(user_no).getUserType());
                //cookie操作放到秒杀商品页的js里了

                if(userList.get(user_no).getUserType().equals("user")){
                    return "page/seckill";  //跳转到秒杀商品页
                }
                else if(userList.get(user_no).getUserType().equals("admin")){
                    return "page/seckill_admin";
                }
                else{
                    System.out.println("err : unknow identity");
                    return "page/seckill_login";
                }

            }
            else{
                //密码错误
                model.addAttribute("err","wrongpassword");
                System.out.println("密码错误");
            }
        }



//        List<Seckill> list = seckillService.findAll();
//        model.addAttribute("list", list);
        return "page/seckill_login";
        //return "page/seckill_login";
    }

    //映射到注册界面
    @RequestMapping("/register")
    public String secKillRegister(){
        return "page/seckill_register";
    }

    //映射到尝试注册界面
    @RequestMapping("/try_register")
    @Transactional
    public String try_SecKillRegister(@RequestParam("username") String regPhone,
                                      @RequestParam("password") String regPassword,
                                      @RequestParam("usertype") String regIdentity,
                                      @RequestParam("age") int regAge,
                                      @RequestParam("idcardno") String regIdCardNo,
                                      @RequestParam("workcondition") String regWorkCondition,
                                      Model model) {
        //用户号码和密码
        System.out.println("phone" + regPhone + "  pwd" + regPassword + "  id:" + regIdentity + "  age:" + regAge + "  idcard:" + regIdCardNo + "  idcard:" + regIdCardNo);

        //往数据库中用户的注册信息
        redisTemplate.delete(key_userlist);  //先清空redis缓存
        List<UserBean> userList = seckillService.findAllUsers(); //获取目前的所有用户信息
        System.out.println(userList); //

        boolean isUserExist = false;
        for(int i = 0;i < userList.size();++i){
            if(regPhone.equals(userList.get(i).getUsername())){
                isUserExist = true;
                break;
            }
        }
        if(!isUserExist){ //若该用户当前不存在
            //注册的数据库操作
            int t = seckillMapper.insertUser(regPhone, regPassword, regIdentity, regAge, regIdCardNo, regWorkCondition);
            redisTemplate.delete(key);  //先清空redis缓存
            List<Seckill> list = seckillService.findAll();
            model.addAttribute("list", list);
            model.addAttribute("userphone", regPhone);
            model.addAttribute("userpass", regPassword);
            //cookie操作放到秒杀商品页里了
            if(regIdentity.equals("user")){
                return "page/seckill";     //跳转到秒杀商品页
            }
            else if(regIdentity.equals("admin")){
                return "page/seckill_admin";
            }
        }

        //该用户已存在
        System.out.println("用户已存在");
        model.addAttribute("err","useralreadyexist");

        return "page/seckill_login";
    }

//    @RequestMapping("/page_admin")
//    public String goto_Page_Admin(){
//
//    }

    //映射到秒杀活动删除
    @RequestMapping("/{seckillId}/delSeckillEvent") // /{userphone}
    public String delSeckillEvent(@PathVariable("seckillId") Long seckillId,
//                                  @PathVariable("userphone") String userphone,
                                  Model model){
        System.out.println("删除活动" + seckillId);

        Long delEventNo = seckillId;
        //删除数据库里的秒杀活动
        int t = seckillMapper.delSeckillEvent(delEventNo);



        redisTemplate.delete(key);  //先清空redis缓存
        List<Seckill> list = seckillService.findAll();
        model.addAttribute("list", list);
//        model.addAttribute("userphone", userphone);
        return "page/seckill_admin";
    }

    //跳转到添加新活动页面
    @RequestMapping("addNewEventPage")
    public String addNewSeckillEventPage(){
        return "/page/seckill_addNewEvent";
    }

    //添加新的秒杀活动
    @RequestMapping("addNewEvent")
    public String addNewSckillEvent(@RequestParam("eventName") String eventName,
                                    @RequestParam("eventOriPrice") int eventOriPrice,
                                    @RequestParam("eventDiscountPrice") int eventDiscountPrice,
                                    @RequestParam("eventStockCount") int eventStockCount,
                                    @RequestParam("eventStartTime") String eventStartTime,
                                    @RequestParam("eventEndTime") String eventEndTime,
                                    @RequestParam("eventAgeLimit") int eventAgeLimit,
                                    @RequestParam("eventWorkLimit") int eventWorkLimit,
                                    Model model){
        System.out.println("add new seckill event");
        System.out.println(eventName + " " + eventOriPrice + " " + eventDiscountPrice + " " + eventStockCount + " " + eventStartTime + " " + eventEndTime);

        redisTemplate.delete(key);  //先清空redis缓存
        List<Seckill> list = seckillService.findAll();
        int newEventNo = list.size() + 1;

        //重构时间的string
        String newStartTime = "";
        String newEndTime = "";

        newStartTime = newStartTime.concat(eventStartTime.substring(0,10));
        newStartTime = newStartTime.concat(" ");
        newStartTime = newStartTime.concat(eventStartTime.substring(11,16));
        newStartTime = newStartTime.concat(":00");

        newEndTime = newEndTime.concat(eventEndTime.substring(0,10));
        newEndTime = newEndTime.concat(" ");
        newEndTime = newEndTime.concat(eventEndTime.substring(11,16));
        newEndTime = newEndTime.concat(":00");

//        System.out.println("newStartTime:" + newStartTime);
//        System.out.println("newEndTime:" + newEndTime);

        //string转timestamp
        Timestamp newStartTime_TS = new Timestamp(System.currentTimeMillis());
        Timestamp newEndTime_TS = new Timestamp(System.currentTimeMillis());
        try {
            newStartTime_TS = Timestamp.valueOf(newStartTime);
            newEndTime_TS = Timestamp.valueOf(newEndTime);
            System.out.println(newStartTime_TS);
            System.out.println(newEndTime_TS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int t = seckillMapper.addNewSeckillEvent(newEventNo,eventName,eventOriPrice,eventDiscountPrice,eventStockCount,newStartTime_TS, newEndTime_TS,eventAgeLimit,eventWorkLimit);


        redisTemplate.delete(key);  //先清空redis缓存
        list = seckillService.findAll();
        model.addAttribute("list", list);
        return "page/seckill_admin";
    }











    @RequestMapping("/list")
    public String findSeckillList(Model model) {
        List<Seckill> list = seckillService.findAll();
        model.addAttribute("list", list);
        return "page/seckill";
    }

    @ResponseBody
    @RequestMapping("/findById")
    public Seckill findById(@RequestParam("id") Long id) {
        return seckillService.findById(id);
    }

    @RequestMapping("/{seckillId}/detail/{userphone}")
    public String detail(@PathVariable("seckillId") Long seckillId, @PathVariable("userphone") Long userphone, Model model) {
        if (seckillId == null) {
            return "page/seckill";
        }
        Seckill seckill = seckillService.findById(seckillId);
        UserBean loggedUser = seckillMapper.findUserByPhone(userphone);
        loggedUser.printSelf();
        model.addAttribute("seckill", seckill);

        if(loggedUser.getUserAge() < seckill.getAgeLimit()){
            System.out.println("年龄资格不符");
            return "page/errorPage";
        }
        else if(seckill.getWorkLimit() == 1 && loggedUser.getUserWorkcondition() != 1){
            System.out.println("工作资格不符");
            return "page/errorPage";
        }
        else{
            return "page/seckill_detail";
        }

        //return "page/errorPage";
    }

    @ResponseBody
    @RequestMapping(value = "/{seckillId}/exposer",
            method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    public SeckillResult<Exposer> exposer(@PathVariable("seckillId") Long seckillId) {
        SeckillResult<Exposer> result;
        try {
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            result = new SeckillResult<Exposer>(true, exposer);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result = new SeckillResult<Exposer>(false, e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/{seckillId}/{md5}/execution",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
                                                   @PathVariable("md5") String md5,
                                                   @RequestParam("money") BigDecimal money,
                                                   @CookieValue(value = "killPhone", required = false) Long userPhone) {
        if (userPhone == null) {
            return new SeckillResult<SeckillExecution>(false, "未注册");
        }
        try {
            SeckillExecution execution = seckillService.executeSeckill(seckillId, money, userPhone, md5);
            return new SeckillResult<SeckillExecution>(true, execution);
        } catch (RepeatKillException e) {
            SeckillExecution seckillExecution = new SeckillExecution(seckillId, SeckillStatEnum.REPEAT_KILL);
            return new SeckillResult<SeckillExecution>(true, seckillExecution);
        } catch (SeckillCloseException e) {
            SeckillExecution seckillExecution = new SeckillExecution(seckillId, SeckillStatEnum.END);
            return new SeckillResult<SeckillExecution>(true, seckillExecution);
        } catch (SeckillException e) {
            SeckillExecution seckillExecution = new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
            return new SeckillResult<SeckillExecution>(true, seckillExecution);
        }
    }

    @ResponseBody
    @GetMapping(value = "/time/now")
    public SeckillResult<Long> time() {
        Date now = new Date();
        return new SeckillResult(true, now.getTime());
    }
}
