package com.zulong.web.dao;

import com.zulong.web.entity.Flow;

import java.util.List;

public interface FlowDao {
    Flow findByFlowID(int record_id);
    boolean insertFlow(Flow flow);
    public List<Flow> getFlowList();
    public Flow getFlowDetails(int flow_id, int version);
    public int deleteFlow(int record_id);
    public Flow cloneFlow(int record_id, String name, String des);
}
