package app.allclear.common;

import java.util.TimeZone;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;

/** Include this in your Dropwizard Configuration to configure where and how log files are kept.
 * Might be a simple log file or daily rolling logfiles.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 */

@ApiModel(value="configuration for log files")
public class LogFileConfiguration {
    private boolean rolling = true;
    @JsonProperty("rolling")
    @ApiModelProperty(value="Daily rolling log files or just one logfile?")
    public boolean isRolling() {
        return rolling;
    }

    private String logDir = "./logs";
    @Valid
    @NotEmpty
    @JsonProperty("logDir")
    @ApiModelProperty(value="Directory in which log files are kept")
    public String getLogDir() {
        return logDir;
    }

    private String logFilenamePattern = "yyyy_mm_dd.stderrout.log";
    @Valid
    @NotEmpty
    @JsonProperty("logFilenamePattern")
    @ApiModelProperty(value="Pattern for name of rolling log files including the pattern \"yyyy_mm_dd\", or name of the one logfile")
    public String getLogFilenamePattern() {
        return logFilenamePattern;
    }

    /** Full pathname of the logfile or logfile pattern. */
    @JsonIgnore
    public String getLogPathnamePattern() {
        final String dir = logDir.endsWith("/") ? logDir : logDir + "/";
        return dir+logFilenamePattern;
    }

    private int retainDays = 90;
    @JsonProperty("retainDays")
    @ApiModelProperty(value="How many days of daily rolling log files to retain")
    public int getRetainDays() {
        return retainDays;
    }

    private String timeZoneId = "GMT";
    @JsonProperty("timeZoneId")
    @ApiModelProperty(value="TimeZone in which daily rolling logfiles should roll over")
    public String getTimeZoneId() {
        return timeZoneId;
    }
    @JsonIgnore
    public TimeZone getTimeZone() {
        return Strings.isNullOrEmpty(timeZoneId) ? null : TimeZone.getTimeZone(timeZoneId);
    }
}
