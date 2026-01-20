package com.openx3.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.exception.BusinessException;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.cfg.ParamUpsertRequest;
import com.openx3.system.entity.cfg.CfgParam;
import com.openx3.system.mapper.CfgParamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 参数管理服务（cfg_param）
 */
@Service
@RequiredArgsConstructor
public class CfgParamService extends IamAdminService {

    private final CfgParamMapper paramMapper;

    public Page<CfgParam> page(PageRequest req, String keyword) {
        checkAdmin();
        Page<CfgParam> page = new Page<>(req.getPageNo(), req.getPageSize());
        LambdaQueryWrapper<CfgParam> qw = new LambdaQueryWrapper<CfgParam>()
                .eq(CfgParam::getDelFlag, 0);
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(CfgParam::getParamCode, keyword).or().like(CfgParam::getParamName, keyword));
        }
        return paramMapper.selectPage(page, qw);
    }

    public CfgParam get(String id) {
        checkAdmin();
        CfgParam p = paramMapper.selectById(id);
        if (p == null || p.getDelFlag() != null && p.getDelFlag() != 0) {
            throw new BusinessException(404, "参数不存在");
        }
        return p;
    }

    public String upsert(ParamUpsertRequest req) {
        checkAdmin();
        CfgParam p = paramMapper.selectOne(new LambdaQueryWrapper<CfgParam>()
                .eq(CfgParam::getParamCode, req.getParamCode())
                .eq(CfgParam::getDelFlag, 0));
        if (p == null) {
            p = new CfgParam();
            p.setParamCode(req.getParamCode());
            p.setParamName(req.getParamName());
            p.setParamValue(req.getParamValue());
            p.setRemark(req.getRemark());
            paramMapper.insert(p);
            return p.getId();
        }
        if (req.getParamName() != null) p.setParamName(req.getParamName());
        if (req.getParamValue() != null) p.setParamValue(req.getParamValue());
        if (req.getRemark() != null) p.setRemark(req.getRemark());
        paramMapper.updateById(p);
        return p.getId();
    }

    public void delete(String id) {
        checkAdmin();
        paramMapper.deleteById(id);
    }
}

