package no.digdir.efmesindexreader.domain.data;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SourceDTO {
    private String status;
    private String loglevel;
    private String receiver;
    private String sender;
    private String direction;
    @JsonProperty("buildinfo_version")
    private String build_version;
   // @JsonProperty("timestamp")
    private String timestamp;
    @JsonSetter
    @JsonAlias("@timestamp")
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp.replace("Z", "");
        this.timestamp = timestamp.replace(" ", "T");
        String substring = this.timestamp.substring(0, this.timestamp.lastIndexOf("."));
        if(!substring.equals("-1")) {
            this.timestamp = substring;
        }
    }
    private String orgnr;
    @JsonProperty("process_identifier")
    private String process_identifier;
    @JsonProperty("sender_org_number")
    private String sender_org_number;
    @JsonProperty("message_id")
    private String message_id;
    @JsonProperty("conversation_id")
    private String conversation_id;
    @JsonProperty("receiver_org_number")
    private String receiver_org_number;
    private String appname;
    @JsonProperty("service_identifier")
    private String service_identifier;
    private String logger_name;
    private String message;
}