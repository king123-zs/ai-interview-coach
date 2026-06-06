package com.example.aiinterviewcoach.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiinterviewcoach.common.ResultCode;
import com.example.aiinterviewcoach.dto.CreateKnowledgeBaseRequest;
import com.example.aiinterviewcoach.dto.UpdateKnowledgeBaseRequest;
import com.example.aiinterviewcoach.entity.KnowledgeBase;
import com.example.aiinterviewcoach.exception.BusinessException;
import com.example.aiinterviewcoach.mapper.KnowledgeBaseMapper;
import com.example.aiinterviewcoach.service.KnowledgeBaseService;
import com.example.aiinterviewcoach.vo.KnowledgeBaseVO;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    public KnowledgeBaseServiceImpl(KnowledgeBaseMapper knowledgeBaseMapper) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
    }

    @Override
    public Long create(CreateKnowledgeBaseRequest request) {
        LocalDateTime now = LocalDateTime.now();

        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setName(request.getName());
        knowledgeBase.setDescription(request.getDescription());
        knowledgeBase.setCreatedAt(now);
        knowledgeBase.setUpdatedAt(now);

        knowledgeBaseMapper.insert(knowledgeBase);
        return knowledgeBase.getId();
    }

    @Override
    public List<KnowledgeBaseVO> list() {
        LambdaQueryWrapper<KnowledgeBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(KnowledgeBase::getCreatedAt);
        return knowledgeBaseMapper.selectList(queryWrapper)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public KnowledgeBaseVO getById(Long id) {
        return toVO(getExistingKnowledgeBase(id));
    }

    @Override
    public void update(Long id, UpdateKnowledgeBaseRequest request) {
        KnowledgeBase knowledgeBase = getExistingKnowledgeBase(id);
        knowledgeBase.setName(request.getName());
        knowledgeBase.setDescription(request.getDescription());
        knowledgeBase.setUpdatedAt(LocalDateTime.now());

        knowledgeBaseMapper.updateById(knowledgeBase);
    }

    @Override
    public void delete(Long id) {
        getExistingKnowledgeBase(id);
        knowledgeBaseMapper.deleteById(id);
    }

    private KnowledgeBase getExistingKnowledgeBase(Long id) {
        KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectById(id);
        if (knowledgeBase == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return knowledgeBase;
    }

    private KnowledgeBaseVO toVO(KnowledgeBase knowledgeBase) {
        KnowledgeBaseVO vo = new KnowledgeBaseVO();
        vo.setId(knowledgeBase.getId());
        vo.setName(knowledgeBase.getName());
        vo.setDescription(knowledgeBase.getDescription());
        vo.setCreatedAt(knowledgeBase.getCreatedAt());
        vo.setUpdatedAt(knowledgeBase.getUpdatedAt());
        return vo;
    }
}
