package com.zulong.web.dao;

import com.zulong.web.entity.Group;
import com.zulong.web.entity.User;
import org.springframework.cache.annotation.CacheConfig;

import java.util.List;

public interface GroupDao {
    public boolean insertGroup(Group group);
    public List<User> getAllUsers(int group_id);
    public Group getGroupDetails(int group_id);
    public boolean findGroup(int group_id);
    public int getCurrGroupID();
}
