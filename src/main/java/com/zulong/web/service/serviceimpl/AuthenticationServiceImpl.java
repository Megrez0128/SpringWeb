package com.zulong.web.service.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zulong.web.service.AuthenticationService;
import com.zulong.web.dao.GroupFlowDao;
import com.zulong.web.dao.ExtraMetaDao;
import com.zulong.web.dao.InstanceDao;
import com.zulong.web.dao.AdministrationDao;

import com.zulong.web.entity.Instance;

@Service
public class AuthenticationServiceImpl implements AuthenticationService{
        
    @Autowired
    private GroupFlowDao groupFlowDao;
    
    @Autowired
    private ExtraMetaDao extraMetaDao;
    
    @Autowired
    private InstanceDao instanceDao;
    
    @Autowired
    private AdministrationDao administrationDao;
    @Override
    public boolean isUserInGroup(String user_id, Integer group_id) {
        //查询Administration表，判断user_id和group_id是否存在于该表中
        boolean is_in = administrationDao.isUserInGroup(user_id, group_id);
        if(is_in){
            return true;
        }
        return false;
    }

    @Override
    public boolean hasMetaPermission(Integer group_id, Integer meta_id) {
        //查询extra_meta表，判断meta_id和group_id是否存在于该表中
        boolean is_in = extraMetaDao.hasMetaPermission(group_id, meta_id);
        if(is_in){
            return true;
        }
        return false;
    }

    @Override
    public boolean hasFlowPermission(Integer group_id, Integer flow_id) {
        //查询group_flow表，判断flow_id和group_id是否存在于该表中
        boolean is_in = groupFlowDao.hasFlowPermission(group_id, flow_id);
        if(is_in){
            return true;
        }
        return false;
    }

    @Override
    public boolean hasInstancePermission(Integer group_id, Integer instance_id) {
        //先查询instance表，找到Instance_id对应的flow_id
        Instance instance = instanceDao.findInstanceByID(instance_id);
        if(instance == null){
            return false;
        }

        //再查询group_flow表，判断flow_id和group_id是否存在于该表中
        boolean is_in = groupFlowDao.hasFlowPermission(group_id, instance.getFlow_id());
        if(is_in){
            return true;
        }
        return false;
    }
}