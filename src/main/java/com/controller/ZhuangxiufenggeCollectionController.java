
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 装修风格收藏
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/zhuangxiufenggeCollection")
public class ZhuangxiufenggeCollectionController {
    private static final Logger logger = LoggerFactory.getLogger(ZhuangxiufenggeCollectionController.class);

    private static final String TABLE_NAME = "zhuangxiufenggeCollection";

    @Autowired
    private ZhuangxiufenggeCollectionService zhuangxiufenggeCollectionService;


    @Autowired
    private TokenService tokenService;

    @Autowired
    private DictionaryService dictionaryService;//字典
    @Autowired
    private GongchenganpaiService gongchenganpaiService;//工程安排
    @Autowired
    private HetongService hetongService;//合同
    @Autowired
    private ShouhouService shouhouService;//售后服务
    @Autowired
    private YonghuService yonghuService;//用户
    @Autowired
    private YusuanService yusuanService;//预算
    @Autowired
    private ZhuangxiufenggeService zhuangxiufenggeService;//装修风格
    @Autowired
    private ZhuangxiufenggeLiuyanService zhuangxiufenggeLiuyanService;//装修风格留言
    @Autowired
    private ZhucaiService zhucaiService;//主材
    @Autowired
    private ZhucaiCollectionService zhucaiCollectionService;//主材收藏
    @Autowired
    private ZhucaiLiuyanService zhucaiLiuyanService;//主材留言
    @Autowired
    private UsersService usersService;//管理员


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        CommonUtil.checkMap(params);
        PageUtils page = zhuangxiufenggeCollectionService.queryPage(params);

        //字典表数据转换
        List<ZhuangxiufenggeCollectionView> list =(List<ZhuangxiufenggeCollectionView>)page.getList();
        for(ZhuangxiufenggeCollectionView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        ZhuangxiufenggeCollectionEntity zhuangxiufenggeCollection = zhuangxiufenggeCollectionService.selectById(id);
        if(zhuangxiufenggeCollection !=null){
            //entity转view
            ZhuangxiufenggeCollectionView view = new ZhuangxiufenggeCollectionView();
            BeanUtils.copyProperties( zhuangxiufenggeCollection , view );//把实体数据重构到view中
            //级联表 装修风格
            //级联表
            ZhuangxiufenggeEntity zhuangxiufengge = zhuangxiufenggeService.selectById(zhuangxiufenggeCollection.getZhuangxiufenggeId());
            if(zhuangxiufengge != null){
            BeanUtils.copyProperties( zhuangxiufengge , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段,当前表的级联注册表
            view.setZhuangxiufenggeId(zhuangxiufengge.getId());
            }
            //级联表 用户
            //级联表
            YonghuEntity yonghu = yonghuService.selectById(zhuangxiufenggeCollection.getYonghuId());
            if(yonghu != null){
            BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段,当前表的级联注册表
            view.setYonghuId(yonghu.getId());
            }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody ZhuangxiufenggeCollectionEntity zhuangxiufenggeCollection, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,zhuangxiufenggeCollection:{}",this.getClass().getName(),zhuangxiufenggeCollection.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("用户".equals(role))
            zhuangxiufenggeCollection.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        Wrapper<ZhuangxiufenggeCollectionEntity> queryWrapper = new EntityWrapper<ZhuangxiufenggeCollectionEntity>()
            .eq("zhuangxiufengge_id", zhuangxiufenggeCollection.getZhuangxiufenggeId())
            .eq("yonghu_id", zhuangxiufenggeCollection.getYonghuId())
            .eq("zhuangxiufengge_collection_types", zhuangxiufenggeCollection.getZhuangxiufenggeCollectionTypes())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        ZhuangxiufenggeCollectionEntity zhuangxiufenggeCollectionEntity = zhuangxiufenggeCollectionService.selectOne(queryWrapper);
        if(zhuangxiufenggeCollectionEntity==null){
            zhuangxiufenggeCollection.setInsertTime(new Date());
            zhuangxiufenggeCollection.setCreateTime(new Date());
            zhuangxiufenggeCollectionService.insert(zhuangxiufenggeCollection);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody ZhuangxiufenggeCollectionEntity zhuangxiufenggeCollection, HttpServletRequest request) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        logger.debug("update方法:,,Controller:{},,zhuangxiufenggeCollection:{}",this.getClass().getName(),zhuangxiufenggeCollection.toString());
        ZhuangxiufenggeCollectionEntity oldZhuangxiufenggeCollectionEntity = zhuangxiufenggeCollectionService.selectById(zhuangxiufenggeCollection.getId());//查询原先数据

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
//        else if("用户".equals(role))
//            zhuangxiufenggeCollection.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

            zhuangxiufenggeCollectionService.updateById(zhuangxiufenggeCollection);//根据id更新
            return R.ok();
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids, HttpServletRequest request){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        List<ZhuangxiufenggeCollectionEntity> oldZhuangxiufenggeCollectionList =zhuangxiufenggeCollectionService.selectBatchIds(Arrays.asList(ids));//要删除的数据
        zhuangxiufenggeCollectionService.deleteBatchIds(Arrays.asList(ids));

        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<ZhuangxiufenggeCollectionEntity> zhuangxiufenggeCollectionList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            ZhuangxiufenggeCollectionEntity zhuangxiufenggeCollectionEntity = new ZhuangxiufenggeCollectionEntity();
//                            zhuangxiufenggeCollectionEntity.setZhuangxiufenggeId(Integer.valueOf(data.get(0)));   //装修风格 要改的
//                            zhuangxiufenggeCollectionEntity.setYonghuId(Integer.valueOf(data.get(0)));   //用户 要改的
//                            zhuangxiufenggeCollectionEntity.setZhuangxiufenggeCollectionTypes(Integer.valueOf(data.get(0)));   //类型 要改的
//                            zhuangxiufenggeCollectionEntity.setInsertTime(date);//时间
//                            zhuangxiufenggeCollectionEntity.setCreateTime(date);//时间
                            zhuangxiufenggeCollectionList.add(zhuangxiufenggeCollectionEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        zhuangxiufenggeCollectionService.insertBatch(zhuangxiufenggeCollectionList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }




    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        CommonUtil.checkMap(params);
        PageUtils page = zhuangxiufenggeCollectionService.queryPage(params);

        //字典表数据转换
        List<ZhuangxiufenggeCollectionView> list =(List<ZhuangxiufenggeCollectionView>)page.getList();
        for(ZhuangxiufenggeCollectionView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段

        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        ZhuangxiufenggeCollectionEntity zhuangxiufenggeCollection = zhuangxiufenggeCollectionService.selectById(id);
            if(zhuangxiufenggeCollection !=null){


                //entity转view
                ZhuangxiufenggeCollectionView view = new ZhuangxiufenggeCollectionView();
                BeanUtils.copyProperties( zhuangxiufenggeCollection , view );//把实体数据重构到view中

                //级联表
                    ZhuangxiufenggeEntity zhuangxiufengge = zhuangxiufenggeService.selectById(zhuangxiufenggeCollection.getZhuangxiufenggeId());
                if(zhuangxiufengge != null){
                    BeanUtils.copyProperties( zhuangxiufengge , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setZhuangxiufenggeId(zhuangxiufengge.getId());
                }
                //级联表
                    YonghuEntity yonghu = yonghuService.selectById(zhuangxiufenggeCollection.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody ZhuangxiufenggeCollectionEntity zhuangxiufenggeCollection, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,zhuangxiufenggeCollection:{}",this.getClass().getName(),zhuangxiufenggeCollection.toString());
        Wrapper<ZhuangxiufenggeCollectionEntity> queryWrapper = new EntityWrapper<ZhuangxiufenggeCollectionEntity>()
            .eq("zhuangxiufengge_id", zhuangxiufenggeCollection.getZhuangxiufenggeId())
            .eq("yonghu_id", zhuangxiufenggeCollection.getYonghuId())
            .eq("zhuangxiufengge_collection_types", zhuangxiufenggeCollection.getZhuangxiufenggeCollectionTypes())
//            .notIn("zhuangxiufengge_collection_types", new Integer[]{102})
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        ZhuangxiufenggeCollectionEntity zhuangxiufenggeCollectionEntity = zhuangxiufenggeCollectionService.selectOne(queryWrapper);
        if(zhuangxiufenggeCollectionEntity==null){
            zhuangxiufenggeCollection.setInsertTime(new Date());
            zhuangxiufenggeCollection.setCreateTime(new Date());
        zhuangxiufenggeCollectionService.insert(zhuangxiufenggeCollection);

            return R.ok();
        }else {
            return R.error(511,"您已经收藏过了");
        }
    }

}

