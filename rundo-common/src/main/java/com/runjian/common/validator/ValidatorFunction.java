package com.runjian.common.validator;

import com.runjian.common.config.exception.BusinessException;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author Miracle
 * @date 2022/4/21 9:12
 */
public interface ValidatorFunction {

    void validEvent(ValidationResult result, Object data, StringRedisTemplate redisTemplate) throws BusinessException;
}
