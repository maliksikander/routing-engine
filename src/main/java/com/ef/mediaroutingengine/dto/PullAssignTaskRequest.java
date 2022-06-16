package com.ef.mediaroutingengine.dto;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.TaskState;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Pull Assign Task API request body DTO.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PullAssignTaskRequest {
    @NotNull
    private UUID agentId;
    @NotNull
    private String mrdId;
    @NotNull
    private TaskState taskState;
    private ChannelSession channelSession;
}
