package com.ngiveu.cloud.tabe.controller;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.ngiveu.cloud.common.constant.CommonConstant;
import com.ngiveu.cloud.common.util.Query;
import com.ngiveu.cloud.common.vo.UserVO;
import com.ngiveu.cloud.common.web.BaseController;
import com.ngiveu.cloud.tabe.entity.Article;
import com.ngiveu.cloud.tabe.service.IArticleService;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author gaz
 * @since 2018-04-09
 */
@RestController
@RequestMapping("/article")
public class ArticleController extends BaseController {
    @Autowired
    private IArticleService articleService;
    
    @Autowired
    private RestTemplate restTemplate;

    /**
    * 通过ID查询
    *
    * @param id ID
    * @return Article
    */
    @GetMapping("/{id}")
    public Article get(@PathVariable Integer id) {
        return articleService.selectById(id);
    }


    /**
    * 分页查询信息
    *
    * @param params 分页对象
    * @return 分页对象
    */
    @SuppressWarnings("unchecked")
	@RequestMapping("/page")
    public Page<Article> page(@RequestParam Map<String, Object> params) {
        params.put(CommonConstant.DEL_FLAG, CommonConstant.STATUS_NORMAL);
        Page<Article> page = articleService.selectPage(new Query<>(params), new EntityWrapper<>());
        
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        for (Article item : page.getRecords()) {
        	NameValuePair nvp = new BasicNameValuePair("userIds", item.getArticleUserId().toString());
        	nvps.add(nvp);
        }
        
        List<UserVO> userVOs = null;
        try {
			URI uri = new URIBuilder("http://cloud-upms-service/user/listUsersByIds").addParameters(nvps).build();
			userVOs = (List<UserVO>) this.restTemplate.getForObject(uri, List.class);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
        if (userVOs != null) {
        	for (Article item : page.getRecords()) {
        		for (UserVO vo : userVOs) {
        			if (vo.getUserId().equals(item.getArticleUserId())) {
        				// TODO 待创建VO
        				break ;
        			}
        		}
        	}
        }
        return page;
    }

    /**
     * 添加
     * @param  article  实体
     * @return success/false
     */
    @PostMapping
    public Boolean add(@RequestBody Article article) {
        return articleService.insert(article);
    }

    /**
     * 删除
     * @param id ID
     * @return success/false
     */
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Integer id) {
        Article article = new Article();
        article.setId(id);
        article.setArticleUpdateTime(new Date());
        article.setDelFlag(CommonConstant.STATUS_DEL);
        return articleService.updateById(article);
    }

    /**
     * 编辑
     * @param  article  实体
     * @return success/false
     */
    @PutMapping
    public Boolean edit(@RequestBody Article article) {
    	// 先判断是否存在记录
    	Article articlePO = null;
    	if ((articlePO = this.articleService.selectById(article.getId())) != null) {
    		articlePO.setArticleTitle(article.getArticleTitle()); // 标题
    		articlePO.setArticleContent(article.getArticleContent()); // 内容
    		articlePO.setArticleChildCategoryId(article.getArticleChildCategoryId()); // 类别
    		articlePO.setArticleTags(article.getArticleTags());	// 标签
    		articlePO.setArticleUpdateTime(new Date());
    		return articleService.updateById(articlePO);
    	} else {
    		return false;
    	}
    }
    
    /**
     * 获取文章数量
     * 
     * @return
     * @author gaz
     */
    @GetMapping("/count")
    public int count() {
    	return this.articleService.selectCount(null);
    }
}
