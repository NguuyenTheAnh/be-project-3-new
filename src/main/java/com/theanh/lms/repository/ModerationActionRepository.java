package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.ModerationAction;
import org.springframework.stereotype.Repository;

@Repository
public interface ModerationActionRepository extends BaseRepository<ModerationAction, Long> {
}
