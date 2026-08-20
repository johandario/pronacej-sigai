package net.latinus.sistema.integral.gestion.seguridad.model.both.reporte;


public class ExportacionEstadoDTO {
    private String jobId;
    private String estado;
    private int loteActual;
    private int totalLotes;
    private int registrosProcesados;
    private String tokenDescarga;
    private String mensajeError;
    private boolean esPropio;
    private int totalAdolescentesSolicitados;
    private int totalSeccionesSolicitadas;
    private Long tamanoBytes;
    private Long tamanoOriginalBytes;

    public ExportacionEstadoDTO() {
    }

    public String getJobId() {
        return this.jobId;
    }

    public String getEstado() {
        return this.estado;
    }

    public int getLoteActual() {
        return this.loteActual;
    }

    public int getTotalLotes() {
        return this.totalLotes;
    }

    public int getRegistrosProcesados() {
        return this.registrosProcesados;
    }

    public String getTokenDescarga() {
        return this.tokenDescarga;
    }

    public String getMensajeError() {
        return this.mensajeError;
    }

    public boolean isEsPropio() {
        return this.esPropio;
    }

    public int getTotalAdolescentesSolicitados() {
        return this.totalAdolescentesSolicitados;
    }

    public int getTotalSeccionesSolicitadas() {
        return this.totalSeccionesSolicitadas;
    }

    public Long getTamanoBytes() {
        return this.tamanoBytes;
    }

    public Long getTamanoOriginalBytes() {
        return this.tamanoOriginalBytes;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setLoteActual(int loteActual) {
        this.loteActual = loteActual;
    }

    public void setTotalLotes(int totalLotes) {
        this.totalLotes = totalLotes;
    }

    public void setRegistrosProcesados(int registrosProcesados) {
        this.registrosProcesados = registrosProcesados;
    }

    public void setTokenDescarga(String tokenDescarga) {
        this.tokenDescarga = tokenDescarga;
    }

    public void setMensajeError(String mensajeError) {
        this.mensajeError = mensajeError;
    }

    public void setEsPropio(boolean esPropio) {
        this.esPropio = esPropio;
    }

    public void setTotalAdolescentesSolicitados(int totalAdolescentesSolicitados) {
        this.totalAdolescentesSolicitados = totalAdolescentesSolicitados;
    }

    public void setTotalSeccionesSolicitadas(int totalSeccionesSolicitadas) {
        this.totalSeccionesSolicitadas = totalSeccionesSolicitadas;
    }

    public void setTamanoBytes(Long tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }

    public void setTamanoOriginalBytes(Long tamanoOriginalBytes) {
        this.tamanoOriginalBytes = tamanoOriginalBytes;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ExportacionEstadoDTO)) {
            return false;
        }
        ExportacionEstadoDTO other = (ExportacionEstadoDTO)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.getLoteActual() != other.getLoteActual()) {
            return false;
        }
        if (this.getTotalLotes() != other.getTotalLotes()) {
            return false;
        }
        if (this.getRegistrosProcesados() != other.getRegistrosProcesados()) {
            return false;
        }
        if (this.isEsPropio() != other.isEsPropio()) {
            return false;
        }
        if (this.getTotalAdolescentesSolicitados() != other.getTotalAdolescentesSolicitados()) {
            return false;
        }
        if (this.getTotalSeccionesSolicitadas() != other.getTotalSeccionesSolicitadas()) {
            return false;
        }
        Long this$tamanoBytes = this.getTamanoBytes();
        Long other$tamanoBytes = other.getTamanoBytes();
        if (this$tamanoBytes == null ? other$tamanoBytes != null : !((Object)this$tamanoBytes).equals(other$tamanoBytes)) {
            return false;
        }
        Long this$tamanoOriginalBytes = this.getTamanoOriginalBytes();
        Long other$tamanoOriginalBytes = other.getTamanoOriginalBytes();
        if (this$tamanoOriginalBytes == null ? other$tamanoOriginalBytes != null : !((Object)this$tamanoOriginalBytes).equals(other$tamanoOriginalBytes)) {
            return false;
        }
        String this$jobId = this.getJobId();
        String other$jobId = other.getJobId();
        if (this$jobId == null ? other$jobId != null : !this$jobId.equals(other$jobId)) {
            return false;
        }
        String this$estado = this.getEstado();
        String other$estado = other.getEstado();
        if (this$estado == null ? other$estado != null : !this$estado.equals(other$estado)) {
            return false;
        }
        String this$tokenDescarga = this.getTokenDescarga();
        String other$tokenDescarga = other.getTokenDescarga();
        if (this$tokenDescarga == null ? other$tokenDescarga != null : !this$tokenDescarga.equals(other$tokenDescarga)) {
            return false;
        }
        String this$mensajeError = this.getMensajeError();
        String other$mensajeError = other.getMensajeError();
        return !(this$mensajeError == null ? other$mensajeError != null : !this$mensajeError.equals(other$mensajeError));
    }

    protected boolean canEqual(Object other) {
        return other instanceof ExportacionEstadoDTO;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getLoteActual();
        result = result * 59 + this.getTotalLotes();
        result = result * 59 + this.getRegistrosProcesados();
        result = result * 59 + (this.isEsPropio() ? 79 : 97);
        result = result * 59 + this.getTotalAdolescentesSolicitados();
        result = result * 59 + this.getTotalSeccionesSolicitadas();
        Long $tamanoBytes = this.getTamanoBytes();
        result = result * 59 + ($tamanoBytes == null ? 43 : ((Object)$tamanoBytes).hashCode());
        Long $tamanoOriginalBytes = this.getTamanoOriginalBytes();
        result = result * 59 + ($tamanoOriginalBytes == null ? 43 : ((Object)$tamanoOriginalBytes).hashCode());
        String $jobId = this.getJobId();
        result = result * 59 + ($jobId == null ? 43 : $jobId.hashCode());
        String $estado = this.getEstado();
        result = result * 59 + ($estado == null ? 43 : $estado.hashCode());
        String $tokenDescarga = this.getTokenDescarga();
        result = result * 59 + ($tokenDescarga == null ? 43 : $tokenDescarga.hashCode());
        String $mensajeError = this.getMensajeError();
        result = result * 59 + ($mensajeError == null ? 43 : $mensajeError.hashCode());
        return result;
    }

    public String toString() {
        return "ExportacionEstadoDTO(jobId=" + this.getJobId() + ", estado=" + this.getEstado() + ", loteActual=" + this.getLoteActual() + ", totalLotes=" + this.getTotalLotes() + ", registrosProcesados=" + this.getRegistrosProcesados() + ", tokenDescarga=" + this.getTokenDescarga() + ", mensajeError=" + this.getMensajeError() + ", esPropio=" + this.isEsPropio() + ", totalAdolescentesSolicitados=" + this.getTotalAdolescentesSolicitados() + ", totalSeccionesSolicitadas=" + this.getTotalSeccionesSolicitadas() + ", tamanoBytes=" + this.getTamanoBytes() + ", tamanoOriginalBytes=" + this.getTamanoOriginalBytes() + ")";
    }
}
