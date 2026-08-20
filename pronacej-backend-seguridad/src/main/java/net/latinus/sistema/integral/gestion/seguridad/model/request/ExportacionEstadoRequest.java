package net.latinus.sistema.integral.gestion.seguridad.model.request;


public class ExportacionEstadoRequest {
    private String jobId;

    public ExportacionEstadoRequest() {
    }

    public String getJobId() {
        return this.jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ExportacionEstadoRequest)) {
            return false;
        }
        ExportacionEstadoRequest other = (ExportacionEstadoRequest)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$jobId = this.getJobId();
        String other$jobId = other.getJobId();
        return !(this$jobId == null ? other$jobId != null : !this$jobId.equals(other$jobId));
    }

    protected boolean canEqual(Object other) {
        return other instanceof ExportacionEstadoRequest;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $jobId = this.getJobId();
        result = result * 59 + ($jobId == null ? 43 : $jobId.hashCode());
        return result;
    }

    public String toString() {
        return "ExportacionEstadoRequest(jobId=" + this.getJobId() + ")";
    }
}
