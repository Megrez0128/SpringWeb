package com.zulong.web.service;

import com.zulong.web.entity.Flow;
import com.zulong.web.entity.FlowSummary;

import java.util.List;

public interface FlowService
{

    public Flow findFlowByRecordId(int record_id);
    public Flow createFlow( String graph_data, String blackboard, int extra_meta_id,int group_id, String name, String des);

    public Flow saveFlow(String name, String des, int flow_id, int extra_meta_id, String graphData, String blackboard);

    public int deleteFlow(int flow_id);
    public List<Flow> getFlowList(int group_id);
    public List<FlowSummary> getFlowSummaryList(int group_id);
    public Flow getFlowDetails(int flow_id, int version);
    public Flow getFlowDetailsByID(int record_id);
    public Flow cloneFlow(int record_id, String name, String des);

    public Flow commitFlow(int record_id, String commit_message);
    public List<Flow> getHistoryFlowList(int flow_id);
    // NewestFlow是最新版本，NewVersionFlow是已经commit的最新版本
    public Flow getNewestFlow(int flow_id);
    public Flow getNewVersionFlow(int flow_id);
}
