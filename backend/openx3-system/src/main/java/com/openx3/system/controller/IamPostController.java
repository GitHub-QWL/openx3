package com.openx3.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.common.R;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.post.PostCreateRequest;
import com.openx3.system.domain.dto.post.PostUpdateRequest;
import com.openx3.system.entity.iam.SysPost;
import com.openx3.system.service.IamPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 岗位管理接口（sys_post）
 * 说明：岗位用于承载角色（sys_post_role），解决“一人多职/兼职”场景。
 */
@RestController
@RequestMapping("/api/iam/posts")
@RequiredArgsConstructor
public class IamPostController {

    private final IamPostService service;

    @GetMapping("/page")
    public R<Page<SysPost>> page(@Validated PageRequest req,
                                @RequestParam(required = false) String tenantId,
                                @RequestParam(required = false) String keyword) {
        return R.success(service.page(req, tenantId, keyword));
    }

    @GetMapping("/{id}")
    public R<SysPost> get(@PathVariable String id) {
        return R.success(service.get(id));
    }

    @PostMapping
    public R<String> create(@RequestBody @Validated PostCreateRequest req) {
        return R.success(service.create(req));
    }

    @PutMapping
    public R<String> update(@RequestBody @Validated PostUpdateRequest req) {
        service.update(req);
        return R.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable String id) {
        service.delete(id);
        return R.success("删除成功");
    }
}

