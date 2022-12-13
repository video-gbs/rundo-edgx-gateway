package com.runjian.common.validator;


import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.exception.BusinessException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

/**
 * @author Miracle
 * @date 2019/7/10 21:53
 */

@Service
public class ValidatorService implements InitializingBean {

    private Validator validator;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 通用校验调用
     * @param request
     * @throws BusinessException
     */
    public void validateRequest(Object request) throws BusinessException {
        if (Objects.isNull(request)){
            throw new BusinessException(BusinessErrorEnums.VALID_BIND_EXCEPTION_ERROR, "待校验的对象为空对象");
        }
        // 判断是否是集合
        if (request instanceof Collection){
            validateRequestList((Collection<Object>) request);
        }
        ValidationResult validate = validate(request);
        if (validate.isHasErrors()){
            throw new BusinessException(BusinessErrorEnums.VALID_BIND_EXCEPTION_ERROR, validate.getErrMsg());
        }
    }


    /**
     * 通用数组校验
     * @param requestList
     * @throws BusinessException
     */
    public void validateRequestList(Collection<Object> requestList) throws BusinessException {
        ValidationResult results = new ValidationResult();
        for (Object bean : requestList){
            ValidationResult beanResult = validate(bean);
            if (beanResult.isHasErrors()){
                results.getErrorMsgMap().putAll(beanResult.getErrorMsgMap());
            }
        }
        if (!results.getErrorMsgMap().isEmpty()){
            results.setHasErrors(true);
            throw new BusinessException(BusinessErrorEnums.VALID_BIND_EXCEPTION_ERROR, results.getErrMsg());
        }
    }

    private ValidationResult validate(Object bean) throws BusinessException {
        // 创建校验信息类
        ValidationResult result = new ValidationResult();
        // 进行校验
        Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(bean);
        // 判断是否有错误
        if (!constraintViolationSet.isEmpty()){
            result.setHasErrors(true);
            // 遍历错误，将错误信息导入到校验信息类实例中
            constraintViolationSet.forEach(constraintViolation -> {
                String errMsg = constraintViolation.getMessage();
                String propertyName = constraintViolation.getPropertyPath().toString();
                result.getErrorMsgMap().put(propertyName, errMsg);
            });
        }
        if (bean instanceof ValidatorFunction){
            ((ValidatorFunction)bean).validEvent(result, bean, redisTemplate);
        }
        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 将hibernate validator通过工厂的初始化方式使其实例化
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
}
