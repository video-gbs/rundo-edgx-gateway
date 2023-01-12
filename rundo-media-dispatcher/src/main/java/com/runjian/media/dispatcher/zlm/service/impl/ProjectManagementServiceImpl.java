package com.runjian.media.dispatcher.zlm.service.impl;

import com.runjian.media.dispatcher.zlm.dto.PlatformAccountRsp;
import com.runjian.media.dispatcher.zlm.mapper.ProjectManagementMapper;
import com.runjian.media.dispatcher.zlm.service.ProjectManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProjectManagementServiceImpl implements ProjectManagementService {

    @Autowired
    ProjectManagementMapper projectManagementMapper;

    @Override
    public PlatformAccountRsp getOnePlatformAccount(String platformId) {

        return projectManagementMapper.getOne(platformId);
    }


}
