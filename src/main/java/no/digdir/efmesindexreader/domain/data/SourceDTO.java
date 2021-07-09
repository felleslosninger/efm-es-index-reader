package no.digdir.efmesindexreader.domain.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SourceDTO {
    private String status;
    private String loglevel;
    private String description;
    private String receiver;
    private String sender;
    private String direction;
    @JsonProperty("buildinfo_version")
    private String buildVersion;
    @JsonProperty("@timestamp")
    private LocalDateTime dateTime;
    private Long orgnr;
    @JsonProperty("process_identifier")
    private String processIdentifier;
    @JsonProperty("sender_org_number")
    private Long senderOrgNumber;
    @JsonProperty("HOSTNAME")
    private String hostname;
    @JsonProperty("message_id")
    private String messageId;
    @JsonProperty("conversation_id")
    private String conversationId;
    @JsonProperty("receiver_org_number")
    private String receiverOrgNumber;
    private String appname;
    @JsonProperty("service_identifier")
    private String serviceIdentifier;
    private String message;
    private String host;
}