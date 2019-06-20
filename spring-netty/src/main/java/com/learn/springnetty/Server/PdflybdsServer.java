package com.learn.springnetty.Server;

import com.learn.springnetty.Model.Pdflybds;
import com.learn.springnetty.Model.PdflybdsExample;

import java.util.List;

/**
 * @author haochen
 * @date 2019/6/20 16:52
 */
public interface PdflybdsServer {
    public List<Pdflybds> getInfo(PdflybdsExample example);
}
