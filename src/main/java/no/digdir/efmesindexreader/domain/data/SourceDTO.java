package no.digdir.efmesindexreader.domain.data;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SourceDTO {
    private String status;
    private String loglevel;
    private String description;
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
        //this.timestamp = timestamp.replace("T", " ");
        this.timestamp = timestamp.replace("Z", "");
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
    @JsonProperty("HOSTNAME")
    private String hostname;
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
    private String host;
}