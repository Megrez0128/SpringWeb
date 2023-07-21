package com.zulong.web.dao.daoimpl;

import com.zulong.web.entity.Group;
import com.zulong.web.dao.GroupDao;
import com.zulong.web.entity.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;
import com.zulong.web.log.LoggerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@Service("GroupDaoImpl")
public class GroupDaoImpl implements GroupDao {

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    public GroupDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

//    @CacheEvict(value = "groupCache", allEntries = true)
//    @CachePut(value = "groupCache", key="#group_id")
    @Override
    public boolean insertGroup(Group group) {
        String sql = "insert into pack_group(group_id, group_name)values(?, ?)";
        Object[] params = {group.getGroup_id(), group.getGroup_name()};
        boolean flag = jdbcTemplate.update(sql, params) > 0;
        if(!flag){
            LoggerManager.logger().warn("[com.zulong.web.dao.daoimpl]GroupDaoImpl.insertGroup@insertion failed");
        }
        return flag;
    }

    public List<User> getAllUsers(int group_id){
        String sql = "select * from administration where group_id=?";
        Object[] params = {group_id};
        List<User> userList = jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(User.class));
        return userList;
    }

//    @Cacheable(value = "groupCache", key="#group_id", condition="#group_id > 0")
    public Group getGroupDetails(int group_id){
        String choose = "select count(*) from pack_group where group_id=?";
        int count = jdbcTemplate.queryForObject(choose, new Object[]{group_id}, Integer.class);
        if(count > 0) {
            String sql = "select * from pack_group where group_id=?";
            Object[] params = {group_id};
            Group group = jdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<>(Group.class));
            return group;
        }
        else return null;
    }

//    @Cacheable(value = "groupCache", key="#group_id", condition="#group_id > 0")
    public boolean findGroup(int group_id) {
        String sql = "select count(*) from pack_group where group_id=?";
        int count = jdbcTemplate.queryForObject(sql, new Object[]{group_id}, Integer.class);
        return count > 0;
    }

    @Override
    public int getCurrGroupID() {
        String sql = "SELECT MAX(group_id) FROM pack_group ";
        Integer maxGroupId = jdbcTemplate.queryForObject(sql, Integer.class);
        return maxGroupId != null ? maxGroupId : -1;
    }
    
}
