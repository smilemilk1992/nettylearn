package com.learn.springnetty.Server.Impl;

import com.learn.springnetty.Dao.PdflybdsMapper;
import com.learn.springnetty.Model.Pdflybds;
import com.learn.springnetty.Model.PdflybdsExample;
import com.learn.springnetty.Server.PdflybdsServer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author haochen
 * @date 2019/6/20 16:53
 */
@Service("pdflydbsImpl")
public class PdflydbsImpl implements PdflybdsServer {

    @Resource
    private PdflybdsMapper pdflybdsMapper;
    @Override
    public List<Pdflybds> getInfo(PdflybdsExample example) {
        return pdflybdsMapper.selectByExample(example);
    }
}
