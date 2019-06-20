package com.learn.springnetty.NettyRun;

import com.learn.springnetty.Dao.PdflybdsMapper;
import com.learn.springnetty.Model.Pdflybds;
import com.learn.springnetty.Model.PdflybdsExample;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * @author haochen
 * @date 2019/6/20 16:56
 */
public class run {
    public static void main(String[] args){
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        PdflybdsMapper pdflybdsMapper = (PdflybdsMapper) ac.getBean("pdflybdsMapper");
        try {
            PdflybdsExample pdflybdsExample=new PdflybdsExample();
            pdflybdsExample.or().andIdEqualTo(1);
            List<Pdflybds> x= pdflybdsMapper.selectByExample(pdflybdsExample);
            if(x!=null){
                for (Pdflybds p:x){
                    System.out.println(p);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
