package APIResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "projectId",
        "projectName",
        "jiraProjectKey",
        "vulnerabilities",
        "vulnerabilitiesCount",
        "yaraScannerResult",
        "totalVulnerabilities",
        "startDateTime",
        "endDateTime",
        "totalCodeFiles",
        "scannedFiles",
        "totalCodeLines",
        "createdBy",
        "status",
        "riskLevel",
        "risk",
        "scanUpdates",
        "dependencyScannerResult",
        "scanCodeToolKitResult"
        })
@Generated("jsonschema2pojo")
public class SingleScanResponse {
        @JsonProperty("id")
        private String id;
        @JsonProperty("projectId")
        private String projectId;
        @JsonProperty("projectName")
        private Object projectName;
        @JsonProperty("jiraProjectKey")
        private Object jiraProjectKey;
        @JsonProperty("vulnerabilities")
        private List<Vulnerability> vulnerabilities = null;
        @JsonProperty("vulnerabilitiesCount")
        private Integer vulnerabilitiesCount;
        @JsonProperty("yaraScannerResult")
        private Object yaraScannerResult;
        @JsonProperty("totalVulnerabilities")
        private Integer totalVulnerabilities;
        @JsonProperty("startDateTime")
        private String startDateTime;
        @JsonProperty("endDateTime")
        private String endDateTime;
        @JsonProperty("totalCodeFiles")
        private Integer totalCodeFiles;
        @JsonProperty("scannedFiles")
        private Integer scannedFiles;
        @JsonProperty("totalCodeLines")
        private Integer totalCodeLines;
        @JsonProperty("createdBy")
        private Object createdBy;
        @JsonProperty("status")
        private String status;
        @JsonProperty("riskLevel")
        private Integer riskLevel;
        @JsonProperty("risk")
        private String risk;
        @JsonProperty("scanUpdates")
        private List<String> scanUpdates = null;
        @JsonProperty("dependencyScannerResult")
        private Object dependencyScannerResult;
        @JsonProperty("scanCodeToolKitResult")
        private Object scanCodeToolKitResult;
        @JsonIgnore
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();
        @JsonProperty("id")
        public String getId() {
            return id;
        }
        @JsonProperty("id")
        public void setId(String id) {
            this.id = id;
        }
        @JsonProperty("projectId")
        public String getProjectId() {
            return projectId;
        }
        @JsonProperty("projectId")
        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }
        @JsonProperty("projectName")
        public Object getProjectName() {
            return projectName;
        }
        @JsonProperty("projectName")
        public void setProjectName(Object projectName) {
            this.projectName = projectName;
        }
        @JsonProperty("jiraProjectKey")
        public Object getJiraProjectKey() {
            return jiraProjectKey;
        }
        @JsonProperty("jiraProjectKey")
        public void setJiraProjectKey(Object jiraProjectKey) {
            this.jiraProjectKey = jiraProjectKey;
        }
        @JsonProperty("vulnerabilities")
        public List<Vulnerability> getVulnerabilities() {
            return vulnerabilities;
        }
        @JsonProperty("vulnerabilities")
        public void setVulnerabilities(List<Vulnerability> vulnerabilities) {
            this.vulnerabilities = vulnerabilities;
        }
        @JsonProperty("vulnerabilitiesCount")
        public Integer getVulnerabilitiesCount() {
            return vulnerabilitiesCount;
        }
        @JsonProperty("vulnerabilitiesCount")
        public void setVulnerabilitiesCount(Integer vulnerabilitiesCount) {
            this.vulnerabilitiesCount = vulnerabilitiesCount;
        }
        @JsonProperty("yaraScannerResult")
        public Object getYaraScannerResult() {
            return yaraScannerResult;
        }
        @JsonProperty("yaraScannerResult")
        public void setYaraScannerResult(Object yaraScannerResult) {
            this.yaraScannerResult = yaraScannerResult;
        }
        @JsonProperty("totalVulnerabilities")
        public Integer getTotalVulnerabilities() {
            return totalVulnerabilities;
        }
        @JsonProperty("totalVulnerabilities")
        public void setTotalVulnerabilities(Integer totalVulnerabilities) {
            this.totalVulnerabilities = totalVulnerabilities;
        }
        @JsonProperty("startDateTime")
        public String getStartDateTime() {
            return startDateTime;
        }
        @JsonProperty("startDateTime")
        public void setStartDateTime(String startDateTime) {
            this.startDateTime = startDateTime;
        }
        @JsonProperty("endDateTime")
        public String getEndDateTime() {
            return endDateTime;
        }
        @JsonProperty("endDateTime")
        public void setEndDateTime(String endDateTime) {
            this.endDateTime = endDateTime;
        }
        @JsonProperty("totalCodeFiles")
        public Integer getTotalCodeFiles() {
            return totalCodeFiles;
        }
        @JsonProperty("totalCodeFiles")
        public void setTotalCodeFiles(Integer totalCodeFiles) {
            this.totalCodeFiles = totalCodeFiles;
        }
        @JsonProperty("scannedFiles")
        public Integer getScannedFiles() {
            return scannedFiles;
        }
        @JsonProperty("scannedFiles")
        public void setScannedFiles(Integer scannedFiles) {
            this.scannedFiles = scannedFiles;
        }
        @JsonProperty("totalCodeLines")
        public Integer getTotalCodeLines() {
            return totalCodeLines;
        }
        @JsonProperty("totalCodeLines")
        public void setTotalCodeLines(Integer totalCodeLines) {
            this.totalCodeLines = totalCodeLines;
        }
        @JsonProperty("createdBy")
        public Object getCreatedBy() {
            return createdBy;
        }
        @JsonProperty("createdBy")
        public void setCreatedBy(Object createdBy) {
            this.createdBy = createdBy;
        }
        @JsonProperty("status")
        public String getStatus() {
            return status;
        }
        @JsonProperty("status")
        public void setStatus(String status) {
            this.status = status;
        }
        @JsonProperty("riskLevel")
        public Integer getRiskLevel() {
            return riskLevel;
        }
        @JsonProperty("riskLevel")
        public void setRiskLevel(Integer riskLevel) {
            this.riskLevel = riskLevel;
        }
        @JsonProperty("risk")
        public String getRisk() {
            return risk;
        }
        @JsonProperty("risk")
        public void setRisk(String risk) {
            this.risk = risk;
        }
        @JsonProperty("scanUpdates")
        public List<String> getScanUpdates() {
            return scanUpdates;
        }
        @JsonProperty("scanUpdates")
        public void setScanUpdates(List<String> scanUpdates) {
            this.scanUpdates = scanUpdates;
        }
        @JsonProperty("dependencyScannerResult")
        public Object getDependencyScannerResult() {
            return dependencyScannerResult;
        }
        @JsonProperty("dependencyScannerResult")
        public void setDependencyScannerResult(Object dependencyScannerResult) {
            this.dependencyScannerResult = dependencyScannerResult;
        }
        @JsonProperty("scanCodeToolKitResult")
        public Object getScanCodeToolKitResult() {
            return scanCodeToolKitResult;
        }
        @JsonProperty("scanCodeToolKitResult")
        public void setScanCodeToolKitResult(Object scanCodeToolKitResult) {
            this.scanCodeToolKitResult = scanCodeToolKitResult;
        }
        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }
        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

}
