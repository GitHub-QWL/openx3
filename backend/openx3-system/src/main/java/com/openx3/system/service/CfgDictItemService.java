package com.openx3.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.exception.BusinessException;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.cfg.DictItemCreateRequest;
import com.openx3.system.domain.dto.cfg.DictItemUpdateRequest;
import com.openx3.system.entity.cfg.CfgDict;
import com.openx3.system.entity.cfg.CfgDictItem;
import com.openx3.system.mapper.CfgDictItemMapper;
import com.openx3.system.mapper.CfgDictMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 字典项管理服务（cfg_dict_item）
 */
@Service
@RequiredArgsConstructor
public class CfgDictItemService extends IamAdminService {

    private final CfgDictMapper dictMapper;
    private final CfgDictItemMapper itemMapper;

    public Page<CfgDictItem> page(PageRequest req, String dictId) {
        checkAdmin();
        Page<CfgDictItem> page = new Page<>(req.getPageNo(), req.getPageSize());
        return itemMapper.selectPage(page, new LambdaQueryWrapper<CfgDictItem>()
                .eq(CfgDictItem::getDictId, dictId)
                .eq(CfgDictItem::getDelFlag, 0)
                .orderByAsc(CfgDictItem::getSortNo));
    }

    public CfgDictItem get(String id) {
        checkAdmin();
        CfgDictItem it = itemMapper.selectById(id);
        if (it == null || it.getDelFlag() != null && it.getDelFlag() != 0) {
            throw new BusinessException(404, "字典项不存在");
        }
        return it;
    }

    public String create(DictItemCreateRequest req) {
        checkAdmin();
        CfgDict dict = dictMapper.selectById(req.getDictId());
        if (dict == null || dict.getDelFlag() != null && dict.getDelFlag() != 0) {
            throw new BusinessException(400, "dictId不存在");
        }

        long cnt = itemMapper.selectCount(new LambdaQueryWrapper<CfgDictItem>()
                .eq(CfgDictItem::getDictId, req.getDictId())
                .eq(CfgDictItem::getItemValue, req.getItemValue())
                .eq(CfgDictItem::getDelFlag, 0));
        if (cnt > 0) throw new BusinessException(400, "itemValue已存在");

        CfgDictItem it = new CfgDictItem();
        it.setDictId(req.getDictId());
        it.setItemValue(req.getItemValue());
        it.setItemLabel(req.getItemLabel());
        it.setSortNo(req.getSortNo());
        it.setStatus(req.getStatus());
        itemMapper.insert(it);
        return it.getId();
    }

    public void update(DictItemUpdateRequest req) {
        checkAdmin();
        CfgDictItem it = itemMapper.selectById(req.getId());
        if (it == null || it.getDelFlag() != null && it.getDelFlag() != 0) {
            throw new BusinessException(404, "字典项不存在");
        }
        it.setItemLabel(req.getItemLabel());
        it.setSortNo(req.getSortNo());
        it.setStatus(req.getStatus());
        itemMapper.updateById(it);
    }

    public void delete(String id) {
        checkAdmin();
        itemMapper.deleteById(id);
    }
}

