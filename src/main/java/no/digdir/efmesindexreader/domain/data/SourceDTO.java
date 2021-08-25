package no.digdir.efmesindexreader.domain.data;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
   // @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    @JsonSetter
    @JsonAlias("@timestamp")
    public void setTimestamp(String timestamp) {
        DateTimeFormatter sourceDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime dateTime = LocalDateTime.parse(timestamp, sourceDateTime);
        DateTimeFormatter targetFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime utc = LocalDateTime.parse(dateTime.atZone(ZoneId.of("UTC")).format(targetFormat));
        this.timestamp = LocalDateTime.parse(dateTime.atZone(ZoneId.of("UTC")).format(targetFormat));
    }

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