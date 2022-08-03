package com.ef.mediaroutingengine.routing.dto;

import com.ef.cim.objectmodel.CCUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * An AgentReservedRequest object is used to call the Bot-Frameworks's Agent-Reserved API as Request Body.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AgentReservedRequest {
    /**
     * ID of the JMS-topic on which an Agent is reserved.
     */
    String topicId;
    /**
     * Agent that has been reserved for this topic.
     */
    CCUser agent;
}
