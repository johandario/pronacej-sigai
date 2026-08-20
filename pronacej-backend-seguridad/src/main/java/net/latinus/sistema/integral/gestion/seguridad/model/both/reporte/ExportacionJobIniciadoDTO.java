package net.latinus.sistema.integral.gestion.seguridad.model.both.reporte;


public class ExportacionJobIniciadoDTO {
    private String jobId;

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
        if (!(o instanceof ExportacionJobIniciadoDTO)) {
            return false;
        }
        ExportacionJobIniciadoDTO other = (ExportacionJobIniciadoDTO)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$jobId = this.getJobId();
        String other$jobId = other.getJobId();
        return !(this$jobId == null ? other$jobId != null : !this$jobId.equals(other$jobId));
    }

    protected boolean canEqual(Object other) {
        return other instanceof ExportacionJobIniciadoDTO;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $jobId = this.getJobId();
        result = result * 59 + ($jobId == null ? 43 : $jobId.hashCode());
        return result;
    }

    public String toString() {
        return "ExportacionJobIniciadoDTO(jobId=" + this.getJobId() + ")";
    }

    public ExportacionJobIniciadoDTO() {
    }

    public ExportacionJobIniciadoDTO(String jobId) {
        this.jobId = jobId;
    }
}
