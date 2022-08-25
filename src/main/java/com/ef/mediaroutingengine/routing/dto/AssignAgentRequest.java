package com.ef.mediaroutingengine.routing.dto;

import com.ef.cim.objectmodel.ChannelSession;
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
    private String conversation;
    @NotBlank
    private String agent;
    @NotNull
    private ChannelSession channelSession;
}
