package com.openx3.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.exception.BusinessException;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.cfg.DictCreateRequest;
import com.openx3.system.domain.dto.cfg.DictUpdateRequest;
import com.openx3.system.entity.cfg.CfgDict;
import com.openx3.system.entity.cfg.CfgDictItem;
import com.openx3.system.mapper.CfgDictItemMapper;
import com.openx3.system.mapper.CfgDictMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 字典管理服务（cfg_dict）
 */
@Service
@RequiredArgsConstructor
public class CfgDictService extends IamAdminService {

    private final CfgDictMapper dictMapper;
    private final CfgDictItemMapper itemMapper;

    public Page<CfgDict> page(PageRequest req, String keyword) {
        checkAdmin();
        Page<CfgDict> page = new Page<>(req.getPageNo(), req.getPageSize());
        LambdaQueryWrapper<CfgDict> qw = new LambdaQueryWrapper<CfgDict>()
                .eq(CfgDict::getDelFlag, 0);
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(CfgDict::getDictCode, keyword).or().like(CfgDict::getDictName, keyword));
        }
        return dictMapper.selectPage(page, qw);
    }

    public CfgDict get(String id) {
        checkAdmin();
        CfgDict d = dictMapper.selectById(id);
        if (d == null || d.getDelFlag() != null && d.getDelFlag() != 0) {
            throw new BusinessException(404, "字典不存在");
        }
        return d;
    }

    public String create(DictCreateRequest req) {
        checkAdmin();
        long cnt = dictMapper.selectCount(new LambdaQueryWrapper<CfgDict>()
                .eq(CfgDict::getDictCode, req.getDictCode())
                .eq(CfgDict::getDelFlag, 0));
        if (cnt > 0) throw new BusinessException(400, "dictCode已存在");

        CfgDict d = new CfgDict();
        d.setDictCode(req.getDictCode());
        d.setDictName(req.getDictName());
        d.setStatus(req.getStatus());
        d.setRemark(req.getRemark());
        dictMapper.insert(d);
        return d.getId();
    }

    public void update(DictUpdateRequest req) {
        checkAdmin();
        CfgDict d = dictMapper.selectById(req.getId());
        if (d == null || d.getDelFlag() != null && d.getDelFlag() != 0) {
            throw new BusinessException(404, "字典不存在");
        }
        d.setDictName(req.getDictName());
        d.setStatus(req.getStatus());
        d.setRemark(req.getRemark());
        dictMapper.updateById(d);
    }

    public void delete(String id) {
        checkAdmin();
        long used = itemMapper.selectCount(new LambdaQueryWrapper<CfgDictItem>()
                .eq(CfgDictItem::getDictId, id)
                .eq(CfgDictItem::getDelFlag, 0));
        if (used > 0) throw new BusinessException(400, "字典存在字典项，禁止删除");
        dictMapper.deleteById(id);
    }
}

