package com.ef.mediaroutingengine.routing.dto;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.task.TaskMediaState;
import com.ef.cim.objectmodel.task.TaskType;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Assign agent request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AssignAgentRequest {
    @NotBlank
    private String agent;
    @NotNull
    private ChannelSession requestSession;
    @NotBlank
    private List<ChannelSession> channelSessions;
    @NotNull
    private TaskMediaState state;
    @NotNull
    private TaskType type;
    private boolean offerToAgent;
}
