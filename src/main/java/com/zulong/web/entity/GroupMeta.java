package com.zulong.web.entity;

/**
 * todo:暂时废弃
 * 标记Extra Meta：每个组有自己的扩展Meta，通过组来维护meta版本
 * extra_meta_id表示在该关系中对应的extra meta的id
 * group_id为对应的组id
 */
public class GroupMeta {
    public String extra_meta_id;
    public int group_id;
}