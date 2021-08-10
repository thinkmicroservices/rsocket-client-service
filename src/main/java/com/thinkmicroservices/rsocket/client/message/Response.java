
package com.thinkmicroservices.rsocket.client.message;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 *
 * @author cwoodward
 */
@Data
@SuperBuilder
@Jacksonized
public class Response extends AbstractMessage {
    protected String requestUuid;
    protected String requestMessage;
    protected String responseMessage;
    @Builder.Default
    protected MessageType type=MessageType.RESPONSE;
}
